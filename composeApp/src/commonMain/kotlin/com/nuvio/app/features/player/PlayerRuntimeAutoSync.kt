package com.nuvio.app.features.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.nuvio.app.features.autosync.AutoSyncPlayerController
import com.nuvio.app.features.autosync.AutoSyncPreferencesRepository
import com.nuvio.app.features.autosync.AutoSyncSubtitleCandidate
import kotlinx.coroutines.flow.collect

private fun PlayerScreenRuntime.currentAutoSyncCandidates(): List<AutoSyncSubtitleCandidate> =
    // Paso 2: no buscar entre otros subtítulos; ir directo al reconocimiento de voz.
    emptyList()

internal fun PlayerScreenRuntime.configureAutoSyncController(
    controller: PlayerEngineController,
) {
    controller.setAutoSyncSubtitleCandidates(currentAutoSyncCandidates())
    controller.setAutoSyncAppliedListener { subtitleUrl, delayMs ->
        val appliedSubtitle = addonSubtitles.firstOrNull { it.url == subtitleUrl }
        selectedAddonSubtitleId = appliedSubtitle?.selectionKey ?: subtitleUrl
        selectedSubtitleIndex = -1
        useCustomSubtitles = true
        preferredSubtitleSelectionApplied = true
        if (appliedSubtitle != null && isUserExplicitSubtitleSelection) {
            persistAddonSubtitlePreference(appliedSubtitle)
        }

        val appliedDelayMs = delayMs.coerceIn(
            SUBTITLE_DELAY_MIN_MS,
            SUBTITLE_DELAY_MAX_MS,
        )
        subtitleDelayMs = appliedDelayMs
        PlayerTrackPreferenceStorage.saveSubtitleDelayMs(
            playbackSession.videoId,
            appliedDelayMs,
        )
    }
}

@Composable
internal fun PlayerScreenRuntime.BindAutoSyncRuntimeEffects() {
    val activeController = playerController
    DisposableEffect(activeController) {
        // Runs once playerController is set, which Nuvio only does for the live playback key.
        activeController?.let { configureAutoSyncController(it) }
        onDispose { activeController?.setAutoSyncAppliedListener(null) }
    }
    LaunchedEffect(playerController, playbackSnapshot.isLoading, isLoadingAddonSubtitles, addonSubtitles) {
        // Add-ons can finish after Nuvio has completed its first track scan with no subtitle.
        // Retry that automatic pick only for AutoSync, while the user still has no selection.
        if (playerController !is AutoSyncPlayerController || playbackSnapshot.isLoading) return@LaunchedEffect
        if (!AutoSyncPreferencesRepository.preferredSubtitleAutoSyncOnStart.value) return@LaunchedEffect
        if (isLoadingAddonSubtitles || addonSubtitles.isEmpty()) return@LaunchedEffect
        if (!preferredSubtitleSelectionApplied || isUserExplicitSubtitleSelection) return@LaunchedEffect
        if (selectedSubtitleIndex >= 0 || selectedAddonSubtitleId != null) return@LaunchedEffect
        preferredSubtitleSelectionApplied = false
        refreshTracks()
    }
    LaunchedEffect(playerController, activeAddonSubtitleType, activeVideoId) {
        // Sin esto el sync no busca subtítulos en inglés de referencia y el español queda lento.
        val controller = playerController as? AutoSyncPlayerController ?: return@LaunchedEffect
        val type = activeAddonSubtitleType.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        val videoId = activeVideoId?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        controller.setAutoSyncContent(type, videoId)
    }
    LaunchedEffect(playerController, externalSubtitles) {
        val controller = playerController ?: return@LaunchedEffect
        SubtitleRepository.addonSubtitles.collect { _ ->
            // Paso 2: no alimentar candidatos alternativos; el modelo de voz es el camino.
            controller.setAutoSyncSubtitleCandidates(emptyList())
        }
    }
}

/** The user picked an addon subtitle: let AutoSync check it without replacing their choice. */
internal fun PlayerScreenRuntime.attachSelectedAddonSubtitleWithAutoSync(url: String) {
    subtitleAutoSyncState = SubtitleAutoSyncUiState()
    playerController?.setSubtitleUriWithSelectedAutoSync(url)
}

/**
 * The user's own pick for this content: try it first and keep it if it fits; only when it does
 * not, look for another same-language subtitle that does.
 */
internal fun PlayerScreenRuntime.maybeAutoSyncRestoredSubtitleAtStart(url: String): Boolean {
    val controller = playerController ?: return false
    if (controller !is AutoSyncPlayerController) return false
    if (!AutoSyncPreferencesRepository.claimStartupRun(hashCode(), activePlaybackIdentity)) {
        return false
    }
    controller.setSubtitleUriWithRestoredAutoSync(url)
    return true
}

internal fun PlayerScreenRuntime.maybeAutoSyncPreferredSubtitleAtStart(
    subtitle: AddonSubtitle,
): Boolean {
    if (isUserExplicitSubtitleSelection) return false
    val preferredLanguage =
        normalizeLanguageCode(playerSettingsUiState.preferredSubtitleLanguage) ?: return false
    if (
        preferredLanguage.isBlank() ||
        preferredLanguage == SubtitleLanguageOption.NONE ||
        preferredLanguage == SubtitleLanguageOption.FORCED
    ) {
        return false
    }

    val controller = playerController ?: return false
    if (controller !is AutoSyncPlayerController) return false
    if (!AutoSyncPreferencesRepository.claimStartupRun(hashCode(), activePlaybackIdentity)) {
        return false
    }
    controller.setSubtitleUriWithAutoSync(subtitle.url)
    return true
}
