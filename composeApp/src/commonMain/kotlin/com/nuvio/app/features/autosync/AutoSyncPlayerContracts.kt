package com.nuvio.app.features.autosync

internal data class AutoSyncSubtitleCandidate(
    val url: String,
    val language: String,
    val name: String? = null,
)

/**
 * Optional Android AutoSync capability layered beside PlayerEngineController.
 * Other platforms do not need to implement it.
 */
internal interface AutoSyncPlayerController {
    fun setAutoSyncSubtitleCandidates(candidates: List<AutoSyncSubtitleCandidate>)
    /** The title being played (e.g. "movie", "tt0103064"), so references can be looked up for it. */
    fun setAutoSyncContent(type: String, videoId: String) {}
    /** Stops any AutoSync run when the user sets the subtitle delay by hand. */
    fun cancelForManualSubtitleDelay() {}
    fun setSubtitleUriWithAutoSync(url: String)
    fun setSubtitleUriWithSelectedAutoSync(url: String)

    /** A selection restored from an earlier episode: keep it if it fits, else search others. */
    fun setSubtitleUriWithRestoredAutoSync(url: String)
    fun setAutoSyncAppliedListener(
        listener: ((subtitleUrl: String, delayMs: Int) -> Unit)?,
    )
}
