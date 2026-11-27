package com.nuvio.app.features.autosync

import com.nuvio.app.features.player.SubtitleSyncCue

/**
 * Compact, text-free cue timing export for AutoSync debug reports, so a reported run can be
 * replayed offline against [AutoSyncTimelineRetimer].
 *
 * Line format: `TIMING label=<label> sdh=<0|1> n=<count> data=<cues>` where each cue is
 * `<startDelta>.<duration>` in base 36 (start delta from the previous cue's start), suffixed with
 * `~` when the end was estimated, and cues are comma separated.
 */
internal object AutoSyncTimingDump {
    private const val LINE_PREFIX = "TIMING "

    internal data class Track(
        val label: String,
        val cues: List<SubtitleSyncCue>,
        val estimatedEndStartsMs: Set<Long> = emptySet(),
        val sdh: Boolean = false,
    )

    fun encode(track: Track): String = buildString(track.cues.size * 8 + 64) {
        append(LINE_PREFIX)
        append("label=").append(track.label)
        append(" sdh=").append(if (track.sdh) 1 else 0)
        append(" n=").append(track.cues.size)
        append(" data=")
        var previousStartMs = 0L
        track.cues.forEachIndexed { index, cue ->
            if (index > 0) append(',')
            append((cue.startTimeMs - previousStartMs).toString(36))
            append('.')
            append((cue.endTimeMs - cue.startTimeMs).toString(36))
            if (cue.startTimeMs in track.estimatedEndStartsMs) append('~')
            previousStartMs = cue.startTimeMs
        }
    }
}
