package com.nuvio.app.features.player

import android.content.Context

/**
 * Loads [PlaybackDiskCacheSettings]. Disk cache for unfinished continue-watching is kept across
 * launches; only stale/corrupt folders are cleaned. ExoPlayer's memory buffer is Nuvio's own
 * (NuvioExoPlayerPerformanceHelper).
 */
internal object PlaybackBufferAndroid {
    private const val PREFERENCES = "nuvio_playback_buffer_settings"
    private const val BUFFER_MB_KEY = "playback_buffer_mb"

    fun initialize(context: Context) {
        val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        PlaybackDiskCacheSettings.installPersistence(
            load = { if (preferences.contains(BUFFER_MB_KEY)) preferences.getInt(BUFFER_MB_KEY, 0) else null },
            save = { mb -> preferences.edit().putInt(BUFFER_MB_KEY, mb).apply() },
        )
        PlaybackDiskCache.cleanUpStaleOnly(context)
    }
}
