package com.nuvio.app.features.downloads

import android.content.Context
import com.nuvio.app.features.p2p.P2pSettingsRepository
import com.nuvio.app.features.p2p.buildP2pMagnetUri
import com.nuvio.engine.NuvioEngine
import com.nuvio.engine.NuvioEngineConfig
import com.nuvio.engine.NuvioTorrentProfile
import com.nuvio.engine.NuvioUploadMode
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request

internal val downloadHttpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .followRedirects(true)
    .followSslRedirects(true)
    .build()

internal class DownloadHttpException(val statusCode: Int) : IOException("Download failed: HTTP $statusCode")

internal suspend fun transferAndroidDownload(
    item: DownloadItem,
    directory: File,
    validator: String?,
    client: OkHttpClient = downloadHttpClient,
    onHeaders: (totalBytes: Long?, validator: String?) -> Unit,
    onProgress: (downloadedBytes: Long, totalBytes: Long?) -> Unit,
): File = coroutineScope {
    val activeCall = AtomicReference<Call?>()
    val transfer = async(Dispatchers.IO) {
        try {
            require(File(item.fileName).name == item.fileName && item.fileName.isNotBlank())
            check(directory.isDirectory || directory.mkdirs()) { "Cannot create downloads directory" }
            val partial = File(directory, "${item.fileName}.part")
            var offset = partial.takeIf(File::isFile)?.length() ?: 0L
            var restarted = false

            while (true) {
                ensureActive()
                val request = Request.Builder().url(item.sourceUrl).apply {
                    item.sourceHeaders.forEach { (key, value) ->
                        if (!key.equals("Range", true) && !key.equals("If-Range", true) &&
                            !key.equals("Accept-Encoding", true)) header(key, value)
                    }
                    header("Accept-Encoding", "identity")
                    if (offset > 0L) {
                        header("Range", "bytes=$offset-")
                        validator?.let { header("If-Range", it) }
                    }
                }.build()
                val call = client.newCall(request)
                activeCall.set(call)
                ensureActive()
                call.execute().use { response ->
                    if (response.code == 416 && offset > 0L && !restarted) {
                        offset = 0L
                        restarted = true
                        return@use
                    }
                    if (!response.isSuccessful) throw DownloadHttpException(response.code)
                    val range = response.header("Content-Range")?.let(::parseDownloadContentRange)
                    val resumed = response.code == 206
                    if (resumed && (range == null || range.first != offset)) {
                        throw IOException("Server returned an invalid download range")
                    }
                    val startingBytes = if (resumed) offset else 0L
                    val body = response.body ?: throw IOException("Download response is empty")
                    val contentLength = body.contentLength().takeIf { it >= 0L }
                    val totalBytes = range?.second ?: contentLength?.let { startingBytes + it }
                    val responseValidator = response.header("ETag")?.takeUnless { it.startsWith("W/") }
                        ?: response.header("Last-Modified")
                    onHeaders(totalBytes, responseValidator)
                    var downloaded = startingBytes
                    onProgress(downloaded, totalBytes)
                    body.byteStream().use { input ->
                        FileOutputStream(partial, resumed && offset > 0L).use { output ->
                            val buffer = ByteArray(64 * 1024)
                            while (true) {
                                ensureActive()
                                val size = input.read(buffer)
                                if (size == -1) break
                                output.write(buffer, 0, size)
                                downloaded += size
                                onProgress(downloaded, totalBytes)
                            }
                            output.fd.sync()
                        }
                    }
                    ensureActive()
                    if (totalBytes != null && downloaded != totalBytes) {
                        throw IOException("Download ended before all bytes were received")
                    }
                    return@async partial
                }
            }
            @Suppress("UNREACHABLE_CODE")
            partial
        } catch (error: Exception) {
            // Closing a cancelled HTTP call throws IOException from its blocking read.
            ensureActive()
            throw error
        }
    }
    try {
        transfer.await()
    } finally {
        activeCall.get()?.cancel()
    }
}

internal fun parseDownloadContentRange(value: String): Pair<Long, Long?>? {
    val match = Regex("bytes (\\d+)-(\\d+)/(\\d+|\\*)").matchEntire(value.trim()) ?: return null
    val start = match.groupValues[1].toLongOrNull() ?: return null
    val end = match.groupValues[2].toLongOrNull() ?: return null
    val total = match.groupValues[3].toLongOrNull()
    if (end < start || (total != null && end >= total)) return null
    return start to total
}

private const val P2P_DOWNLOAD_STALL_TIMEOUT_MS = 4 * 60 * 1_000L

/**
 * Downloads a torrent with a **dedicated** NuvioEngine instance (separate from playback).
 *
 * Always uses the Fast torrent profile and disables upload on this engine so bandwidth
 * goes to downloading. Playback can use its own engine at the same time.
 */
internal suspend fun transferAndroidP2pDownload(
    context: Context,
    item: DownloadItem,
    directory: File,
    onProgress: (downloadedBytes: Long, totalBytes: Long?) -> Unit,
): File = withContext(Dispatchers.IO) {
    val infoHash = item.p2pInfoHash?.trim()?.takeIf { it.isNotBlank() }
        ?: throw IOException("Missing torrent info hash for P2P download")
    require(File(item.fileName).name == item.fileName && item.fileName.isNotBlank())
    check(directory.isDirectory || directory.mkdirs()) { "Cannot create downloads directory" }

    P2pSettingsRepository.ensureLoaded()
    val settings = P2pSettingsRepository.uiState.value

    val appContext = context.applicationContext
    val stateDirectory = File(appContext.noBackupFilesDir, "nuvio-engine-downloads/state")
    val cacheDirectory = File(appContext.cacheDir, "nuvio-engine-downloads")
    check(stateDirectory.mkdirs() || stateDirectory.isDirectory) {
        "Could not create the download torrent state directory"
    }
    check(cacheDirectory.mkdirs() || cacheDirectory.isDirectory) {
        "Could not create the download torrent cache directory"
    }

    // Fast + no upload + larger memory cache: download-only engine, max speed.
    val diskCacheBytes = settings.cacheSize.bytes.coerceAtLeast(2L * 1024L * 1024L * 1024L)
    val engine = NuvioEngine.create(
        NuvioEngineConfig(
            dataDirectory = stateDirectory,
            cacheDirectory = cacheDirectory,
            memoryCacheCapacityBytes = 64L * 1024L * 1024L,
            diskCacheCapacityBytes = diskCacheBytes,
            torrentProfile = NuvioTorrentProfile.Fast,
            uploadMode = NuvioUploadMode.Disabled,
            streamInactivityTimeoutMilliseconds = 0,
        ),
    )

    var streamId: String? = null
    var torrentId: String? = null
    try {
        val magnet = item.sourceUrl.takeIf { it.startsWith("magnet:", ignoreCase = true) }
            ?: buildP2pMagnetUri(infoHash, item.p2pTrackers)
        torrentId = engine.addMagnet(magnet)
        ensureActive()

        val stream = engine.prepareStream(
            torrentId = torrentId,
            fileIndex = item.p2pFileIdx,
            filenameHint = item.p2pFilenameHint,
        )
        streamId = stream.id
        val localUrl = stream.url

        // El motor es de streaming: solo descarga lo que alguien lee. Por eso se lee el archivo
        // desde el principio y se guarda mientras llega (si solo se esperara, se frena a medias).
        val knownTotal: Long? = stream.fileSize.takeIf { it > 0L }
        val partial = File(directory, "${item.fileName}.part")
        val client = downloadHttpClient.newBuilder()
            .readTimeout(P2P_DOWNLOAD_STALL_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .build()
        val call = client.newCall(
            Request.Builder().url(localUrl).header("Accept-Encoding", "identity").build(),
        )
        // Si se pausa o cancela, se corta la lectura bloqueada al momento.
        val cancelHook = currentCoroutineContext()[kotlinx.coroutines.Job]?.invokeOnCompletion { call.cancel() }
        try {
            call.execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Could not read torrent file (HTTP ${response.code})")
                }
                val body = response.body ?: throw IOException("Empty torrent payload")
                val contentLength = body.contentLength().takeIf { it >= 0L }
                val total = knownTotal ?: contentLength
                var copied = 0L
                body.byteStream().use { input ->
                    FileOutputStream(partial, false).use { output ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            ensureActive()
                            val size = input.read(buffer)
                            if (size == -1) break
                            output.write(buffer, 0, size)
                            copied += size
                            onProgress(copied, total)
                        }
                        output.fd.sync()
                    }
                }
                val expected = contentLength ?: total
                if (expected != null && copied != expected) {
                    throw IOException("Torrent download ended before all bytes were received")
                }
            }
        } finally {
            cancelHook?.dispose()
        }
        partial
    } catch (error: Exception) {
        if (error is kotlinx.coroutines.CancellationException) throw error
        throw IOException(error.message ?: "Torrent download failed", error)
    } finally {
        withContext(NonCancellable) {
            streamId?.let { id -> runCatching { engine.stopStream(id) } }
            torrentId?.let { id -> runCatching { engine.removeTorrent(id) } }
            runCatching { engine.shutdown() }
            runCatching { engine.close() }
        }
    }
}
