package com.nuvio.app.features.autosync

import com.nuvio.app.features.addons.AddonHttpClientProvider
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.URI
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/** Dedicated client for container index range requests, kept apart from Nuvio's shared pool. */
private val containerIndexHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .callTimeout(5, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
}

internal actual suspend fun <T> autoSyncHttpGet(
    url: String,
    headers: Map<String, String>,
    kind: AutoSyncHttpClientKind,
    timeoutMs: Long?,
    block: suspend (AutoSyncHttpResponse) -> T,
): T {
    // Videos de Descargas: archivo local, se lee con el mismo formato de respuesta que HTTP.
    if (url.startsWith("file:", ignoreCase = true)) {
        return withContext(Dispatchers.IO) { withLocalFileRange(url, headers, block) }
    }
    val requestBuilder = Request.Builder().url(url).get()
    headers.forEach { (name, value) -> requestBuilder.header(name, value) }

    val client = when (kind) {
        AutoSyncHttpClientKind.CONTAINER_INDEX -> containerIndexHttpClient
        AutoSyncHttpClientKind.SUBTITLE -> AddonHttpClientProvider.get()
    }
    val call = client.newCall(requestBuilder.build())
    if (timeoutMs != null) call.timeout().timeout(timeoutMs, TimeUnit.MILLISECONDS)

    // OkHttp's async API lets coroutine cancellation interrupt DNS/connect/headers immediately
    // instead of waiting for a blocking execute() to return.
    val response = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) {
                        continuation.resumeWith(Result.failure(cancellationIfCanceled(call, e)))
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (continuation.isActive) {
                        continuation.resumeWith(Result.success(response))
                    } else {
                        response.close()
                    }
                }
            },
        )
    }

    // Cancelling the caller while the body is read cancels the call, which aborts the read.
    val cancelHandle = currentCoroutineContext().job.invokeOnCompletion { cause ->
        if (cause != null) call.cancel()
    }
    return try {
        response.use { current ->
            val stream: InputStream? = current.body?.byteStream()
            val wrapped = AutoSyncHttpResponse(
                status = current.code,
                statusText = current.message,
                finalUrl = current.request.url.toString(),
                headers = current.headers
                    .toMultimap()
                    .mapValues { (_, values) -> values.joinToString(",") }
                    .mapKeys { (name, _) -> name.lowercase() },
                body = object : AutoSyncHttpBody {
                    override suspend fun read(buffer: ByteArray, offset: Int, length: Int): Int =
                        stream?.read(buffer, offset, length) ?: -1
                },
            )
            withContext(Dispatchers.IO) { block(wrapped) }
        }
    } catch (error: IOException) {
        throw cancellationIfCanceled(call, error)
    } finally {
        cancelHandle.dispose()
    }
}

/** Answers a "Range: bytes=a-b" request from a local file, like a server would (206 + Content-Range). */
private suspend fun <T> withLocalFileRange(
    url: String,
    headers: Map<String, String>,
    block: suspend (AutoSyncHttpResponse) -> T,
): T {
    val file = File(URI(url))
    if (!file.isFile) {
        return block(AutoSyncHttpResponse(404, "Not Found", url, emptyMap(), emptyLocalBody))
    }
    val total = file.length()
    val range = headers.entries.firstOrNull { it.key.equals("Range", ignoreCase = true) }?.value
        ?.let { Regex("""bytes=(\d+)-(\d*)""").find(it) }
    val start = range?.groupValues?.get(1)?.toLongOrNull() ?: 0L
    val requestedEnd = range?.groupValues?.get(2)?.toLongOrNull() ?: (total - 1L)
    if (start >= total) {
        return block(
            AutoSyncHttpResponse(416, "Range Not Satisfiable", url, mapOf("content-range" to "bytes */$total"), emptyLocalBody),
        )
    }
    val end = minOf(requestedEnd, total - 1L)
    return RandomAccessFile(file, "r").use { raf ->
        raf.seek(start)
        var remaining = end - start + 1L
        val body = object : AutoSyncHttpBody {
            override suspend fun read(buffer: ByteArray, offset: Int, length: Int): Int {
                if (remaining <= 0L) return -1
                val n = raf.read(buffer, offset, minOf(length.toLong(), remaining).toInt())
                if (n < 0) return -1
                remaining -= n
                return n
            }
        }
        val response = if (range != null) {
            AutoSyncHttpResponse(
                206, "Partial Content", url,
                mapOf("content-range" to "bytes $start-$end/$total", "content-length" to (end - start + 1L).toString()),
                body,
            )
        } else {
            AutoSyncHttpResponse(200, "OK", url, mapOf("content-length" to total.toString()), body)
        }
        block(response)
    }
}

private val emptyLocalBody = object : AutoSyncHttpBody {
    override suspend fun read(buffer: ByteArray, offset: Int, length: Int): Int = -1
}

private fun cancellationIfCanceled(call: Call, error: Throwable): Throwable =
    if (call.isCanceled() && error !is CancellationException) {
        CancellationException("Cancelled AutoSync HTTP request").also { it.initCause(error) }
    } else {
        error
    }

internal actual fun autoSyncIsCharsetSupported(charsetName: String): Boolean =
    try {
        Charset.isSupported(charsetName)
    } catch (_: Exception) {
        false
    }

internal actual fun autoSyncDecodeBytes(
    bytes: ByteArray,
    offset: Int,
    length: Int,
    charsetName: String,
    strict: Boolean,
): String? {
    val charset = try {
        Charset.forName(charsetName)
    } catch (_: Exception) {
        return null
    }
    if (!strict) return String(bytes, offset, length, charset)
    return try {
        charset.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes, offset, length))
            .toString()
    } catch (_: Exception) {
        null
    }
}
