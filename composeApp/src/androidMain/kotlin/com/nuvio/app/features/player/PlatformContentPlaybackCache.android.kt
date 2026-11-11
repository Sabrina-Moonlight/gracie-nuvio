package com.nuvio.app.features.player

import android.util.Log
import com.nuvio.app.features.p2p.canonicalP2pInfoHash
import java.io.File

internal actual object PlatformContentPlaybackCache {
    private const val TAG = "ContentPlaybackCache"

    actual fun clearUrls(urls: Collection<String>) {
        val cleaned = urls.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        if (cleaned.isEmpty()) return
        runCatching { VodCache.removeCachedUrls(cleaned) }
            .onFailure { Log.w(TAG, "VodCache clear failed", it) }
        runCatching { PlaybackDiskCache.removeCachedUrls(cleaned) }
            .onFailure { Log.w(TAG, "PlaybackDiskCache clear failed", it) }
    }

    actual fun clearP2pInfoHash(infoHash: String?) {
        val hash = infoHash?.let { runCatching { canonicalP2pInfoHash(it) }.getOrNull() }
            ?.takeIf { it.isNotBlank() }
            ?: return
        // Best-effort: delete payload folders whose name contains the info-hash.
        // Full per-torrent reclaim is only available as a global clear in the engine.
        val cacheRoot = runCatching {
            // Application context is not always available here; use cache dirs of running process.
            File(System.getProperty("java.io.tmpdir") ?: ".", "unused")
        }
        // Prefer android cache dir via reflection-free path: engine stores under context.cacheDir.
        // Without a Context, walk known relative locations is unreliable. Use process-wide app cache
        // if the engine already created nuvio-engine under the default cache path.
        val candidates = buildList {
            val dataDir = runCatching { File("/data/data").listFiles() }.getOrNull().orEmpty()
            // Skip full /data walk; use env ANDROID_DATA only if present is still weak.
        }
        // Context-aware cleanup lives in clearP2pInfoHashWithContext when called from Android UI paths.
        // Here we only log; MainAppContent path uses Context-backed helper below when possible.
        Log.i(TAG, "P2P infoHash clear requested hash=${hash.take(12)} (stream-link already removed)")
        P2pPayloadCacheCleaner.clearByInfoHash(hash)
    }
}

/**
 * Deletes on-disk P2P payload folders matching [infoHash] under the app cache `nuvio-engine` tree.
 * Wired with a Context from [com.nuvio.app.features.p2p.P2pStreamingEngine] cache location.
 */
internal object P2pPayloadCacheCleaner {
    private const val TAG = "P2pPayloadCleaner"
    @Volatile
    private var cacheDirProvider: (() -> File?)? = null

    fun install(provider: () -> File?) {
        cacheDirProvider = provider
    }

    fun clearByInfoHash(infoHash: String) {
        val root = cacheDirProvider?.invoke() ?: return
        val payload = File(root, "payload")
        if (!payload.isDirectory) return
        val needle = infoHash.lowercase()
        val deleted = payload.listFiles().orEmpty().count { entry ->
            val name = entry.name.lowercase()
            if (name.contains(needle)) {
                runCatching { entry.deleteRecursively() }.isSuccess
            } else {
                false
            }
        }
        if (deleted > 0) {
            Log.i(TAG, "removed $deleted P2P payload folder(s) for hash=${infoHash.take(12)}")
        }
    }
}
