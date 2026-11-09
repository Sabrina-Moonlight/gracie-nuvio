package com.nuvio.app.features.player.seekpreview

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * The time window of a single seek-preview cue, expressed in the **playback** timebase.
 *
 * Seekr sprite sheets hold one frame per cue (a ~10 second grid today), so a preview
 * thumbnail never represents an exact millisecond — it represents the whole window
 * [startMs]..[endMs]. Resolving that window and keeping it in player state is what lets the
 * scrubber move in whole cues instead of promising a precision the sprite sheet cannot back
 * up.
 *
 * The cue times reported by the SDK are on the *preview* timeline; callers must subtract the
 * active sync offset before constructing this so both ends are directly comparable with
 * playback positions.
 */
internal data class SeekPreviewCue(
    val startMs: Long,
    val endMs: Long
) {
    /** Length of the cue window; the effective scrub granularity. */
    val durationMs: Long get() = endMs - startMs

    val isValid: Boolean get() = durationMs > 0L

    /** True when [positionMs] falls inside this window (start-inclusive, end-exclusive). */
    fun contains(positionMs: Long): Boolean = positionMs >= startMs && positionMs < endMs

    /**
     * True when this is the cue whose frame best represents [positionMs] — the one the
     * preview centres on.
     *
     * A cue's frame is captured at its *start*, so the containing cue is not the closest one
     * for positions in the back half of the window: at 18:29 the cue covering 18:20..18:30
     * holds an 18:20 frame, nine seconds stale, while the next cue's 18:30 frame is one second
     * away. Preferring the nearer frame roughly halves the worst-case error and, since the
     * playhead sits between the two, keeps the filmstrip reading symmetrically around it.
     *
     * The window is therefore shifted back by half a cue, but its trailing edge is left at
     * [endMs] rather than pulled in: the last cue of a track has no successor to hand over to,
     * so it has to keep representing the run-out to the end of the media.
     *
     * This approximates the hand-over using *this* cue's length, where [prefersSuccessorFor]
     * decides it exactly using the preceding cue's midpoint. The two agree whenever
     * neighbouring cues are the same length, which is every cue on today's uniform 10s grid.
     * Once the generator writes real keyframe times as cue starts, cue lengths will vary and
     * the two will disagree near a length change — harmlessly, since a cue that fails this
     * check merely falls back to free stepping, but this is the place to revisit if grid
     * locking is to stay exact on a non-uniform grid.
     */
    fun represents(positionMs: Long): Boolean =
        positionMs >= startMs - durationMs / 2 && positionMs < endMs

    /**
     * True when the *next* cue's frame is closer to [positionMs] than this cue's own — i.e.
     * when [positionMs] is past this window's midpoint.
     *
     * Together with the window itself this is the complete input to the preview's re-centring
     * decision, so replaying it is how the host knows a cached frame is still the right one.
     */
    fun prefersSuccessorFor(positionMs: Long): Boolean =
        positionMs - startMs > endMs - positionMs
}

/**
 * How far a scrub target may move to land on a keyframe of the playing file. A keyframe is the
 * one position a player can seek to exactly without decoding forward, so this makes the seek
 * both precise and as fast as it gets. Seekr frames are themselves only within about 3 s of
 * their cue time, so moving by up to this much costs nothing visible.
 */
internal const val SEEK_PREVIEW_KEYFRAME_SNAP_MS = 1_500L
