package com.nuvio.app.features.downloads

import com.nuvio.app.features.p2p.P2pStreamRequest
import com.nuvio.app.features.p2p.P2pStreamingEngine
import com.nuvio.app.features.p2p.P2pStreamingState
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.Dispatchers
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

/**
 * Downloads a torrent via the local P2P engine, waits until the selected file is complete,
 * then copies it from the engine's local HTTP URL into the permanent downloads directory.
 */
internal suspend fun transferAndroidP2pDownload(
    item: DownloadItem,
    directory: File,
    onProgress: (downloadedBytes: Long, totalBytes: Long?) -> Unit,
): File = withContext(Dispatchers.IO) {
    val infoHash = item.p2pInfoHash?.trim()?.takeIf { it.isNotBlank() }
        ?: throw IOException("Missing torrent info hash for P2P download")
    require(File(item.fileName).name == item.fileName && item.fileName.isNotBlank())
    check(directory.isDirectory || directory.mkdirs()) { "Cannot create downloads directory" }

    val request = P2pStreamRequest(
        infoHash = infoHash,
        fileIdx = item.p2pFileIdx,
        filename = item.p2pFilenameHint,
        trackers = item.p2pTrackers,
    )

    val localUrl = try {
        P2pStreamingEngine.startStream(request)
    } catch (error: Exception) {
        throw IOException(error.message ?: "Could not start torrent download", error)
    }

    var totalBytes: Long? = null
    try {
        // Wait until the torrent file is fully verified/downloaded.
        while (currentCoroutineContext().isActive) {
            when (val state = P2pStreamingEngine.state.value) {
                is P2pStreamingState.Streaming -> {
                    totalBytes = state.downloadedBytes.takeIf { it > 0L }?.let { downloaded ->
                        if (state.totalProgress in 0.01f..1f) {
                            (downloaded / state.totalProgress.toDouble()).toLong().coerceAtLeast(downloaded)
                        } else {
                            totalBytes
                        }
                    } ?: totalBytes
                    onProgress(state.downloadedBytes.coerceAtLeast(0L), totalBytes)
                    if (state.totalProgress >= 0.999f) break
                    if (state.verifiedBytes > 0L && totalBytes != null && state.verifiedBytes >= totalBytes) break
                }
                is P2pStreamingState.Error -> {
                    throw IOException(state.message.ifBlank { "Torrent download failed" })
                }
                P2pStreamingState.Idle -> {
                    // Engine reset unexpectedly
                    throw IOException("Torrent download stopped unexpectedly")
                }
                is P2pStreamingState.Connecting -> {
                    onProgress(0L, totalBytes)
                }
            }
            delay(1_000L)
        }
        ensureActive()

        // Copy from the local engine HTTP URL into the permanent .part file.
        val partial = File(directory, "${item.fileName}.part")
        val callRequest = Request.Builder().url(localUrl).header("Accept-Encoding", "identity").build()
        downloadHttpClient.newCall(callRequest).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Could not read completed torrent file (HTTP ${response.code})")
            }
            val body = response.body ?: throw IOException("Empty torrent payload")
            val contentLength = body.contentLength().takeIf { it >= 0L }
            var downloaded = 0L
            onProgress(0L, contentLength ?: totalBytes)
            body.byteStream().use { input ->
                FileOutputStream(partial, false).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        ensureActive()
                        val size = input.read(buffer)
                        if (size == -1) break
                        output.write(buffer, 0, size)
                        downloaded += size
                        onProgress(downloaded, contentLength ?: totalBytes)
                    }
                    output.fd.sync()
                }
            }
            if (contentLength != null && downloaded != contentLength) {
                throw IOException("Torrent copy ended before all bytes were received")
            }
        }
        partial
    } finally {
        runCatching { P2pStreamingEngine.stopStream() }
    }
}
