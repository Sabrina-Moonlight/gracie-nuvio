package com.nuvio.app.features.streams

import com.nuvio.app.features.addons.AddonRepository
import com.nuvio.app.features.addons.enabledAddons
import com.nuvio.app.features.profiles.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Busca los resultados de UN capítulo en todos los addons de streams instalados,
 * sin tocar el estado de [StreamsRepository] (así se pueden pedir varios capítulos a la vez).
 * Emite la lista de grupos (uno por addon) cada vez que llega la respuesta de un addon.
 * Solo addons instalados: no incluye plugins ni servidores.
 */
internal fun loadEpisodeStreamGroups(
    type: String,
    videoId: String,
    season: Int?,
    episode: Int?,
): Flow<List<AddonStreamGroup>> = flow {
    val streamAddons = AddonRepository.uiState.value.addons.enabledAddons()
        .mapNotNull { addon ->
            val manifest = addon.manifest ?: return@mapNotNull null
            if (!manifest.supportsStream(type, videoId)) return@mapNotNull null
            InstalledStreamAddonTarget(
                addonName = addon.displayTitle.ifBlank { manifest.name },
                addonId = addon.streamAddonInstanceId(manifest.id),
                manifest = manifest,
            )
        }
    if (streamAddons.isEmpty()) {
        emit(emptyList())
        return@flow
    }

    val targets = StreamSearchTargets(
        type = type,
        videoId = videoId,
        season = season,
        episode = episode,
        streamAddons = streamAddons,
        serverSources = emptyList(),
        pluginProviderGroups = emptyList(),
    )
    val groups = linkedMapOf<String, AddonStreamGroup>()
    StreamSearchSessions.search(
        profileId = ProfileRepository.activeProfileId,
        targets = targets,
        forceRefresh = false,
    ).collect { completion ->
        if (completion is StreamLoadCompletion.Addon) {
            groups[completion.group.addonId] = completion.group
            emit(groups.values.toList())
        }
    }
}
