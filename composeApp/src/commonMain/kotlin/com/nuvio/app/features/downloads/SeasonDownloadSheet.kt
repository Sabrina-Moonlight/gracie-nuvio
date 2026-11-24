package com.nuvio.app.features.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.NuvioModalBottomSheet
import com.nuvio.app.core.ui.NuvioToastController
import com.nuvio.app.core.ui.dismissNuvioBottomSheet
import com.nuvio.app.core.ui.nuvioSafeBottomPadding
import com.nuvio.app.features.debrid.DirectDebridPlayableResult
import com.nuvio.app.features.debrid.DirectDebridPlaybackResolver
import com.nuvio.app.features.debrid.toastMessage
import com.nuvio.app.features.details.MetaDetails
import com.nuvio.app.features.details.MetaVideo
import com.nuvio.app.features.streams.StreamItem
import com.nuvio.app.features.streams.loadEpisodeStreamGroups
import com.nuvio.app.features.streams.AddonStreamGroup
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.season_download_no_addons
import nuvio.composeapp.generated.resources.season_download_no_results
import nuvio.composeapp.generated.resources.season_download_searching
import nuvio.composeapp.generated.resources.season_download_show_less
import nuvio.composeapp.generated.resources.season_download_show_more
import nuvio.composeapp.generated.resources.season_download_title
import org.jetbrains.compose.resources.stringResource

private const val CollapsedResultCount = 4
private const val ParallelEpisodeSearches = 3

/**
 * Pantalla para descargar una temporada: pestañas por addon arriba y, por capítulo,
 * los resultados de ese addon. Un toque en un resultado lo descarga.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonDownloadSheet(
    meta: MetaDetails,
    seasonLabel: String,
    episodes: List<MetaVideo>,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val results = remember { mutableStateMapOf<String, List<AddonStreamGroup>>() }
    val loadingIds = remember { mutableStateMapOf<String, Boolean>() }
    val startedKeys = remember { mutableStateMapOf<String, Boolean>() }
    val expandedIds = remember { mutableStateMapOf<String, Boolean>() }
    var selectedAddonId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(meta.id, episodes) {
        episodes.forEach { loadingIds[it.id] = true }
        val semaphore = Semaphore(ParallelEpisodeSearches)
        coroutineScope {
            episodes.forEach { episode ->
                launch {
                    semaphore.withPermit {
                        try {
                            loadEpisodeStreamGroups(
                                type = meta.type,
                                videoId = episode.id,
                                season = episode.season,
                                episode = episode.episode,
                            ).collect { groups -> results[episode.id] = groups }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            // Si un capítulo falla, queda "sin resultados"; los demás siguen.
                        }
                        loadingIds[episode.id] = false
                    }
                }
            }
        }
    }

    val addonTabs = episodes
        .flatMap { results[it.id].orEmpty() }
        .map { it.addonId to it.addonName }
        .distinctBy { it.first }
    val activeAddonId = addonTabs.firstOrNull { it.first == selectedAddonId }?.first
        ?: addonTabs.firstOrNull()?.first
    val anyLoading = loadingIds.values.any { it }

    fun download(episode: MetaVideo, stream: StreamItem, key: String) {
        scope.launch {
            val playable: StreamItem? =
                if (DirectDebridPlaybackResolver.shouldResolveToPlayableStream(stream)) {
                    val resolved = DirectDebridPlaybackResolver.resolveToPlayableStream(
                        stream = stream,
                        season = episode.season,
                        episode = episode.episode,
                    )
                    when (resolved) {
                        is DirectDebridPlayableResult.Success -> resolved.stream
                        else -> {
                            resolved.toastMessage()?.let { NuvioToastController.show(it) }
                            null
                        }
                    }
                } else {
                    stream
                }
            if (playable != null) {
                val result = DownloadsRepository.enqueueFromStream(
                    contentType = meta.type,
                    videoId = episode.id,
                    parentMetaId = meta.id,
                    parentMetaType = meta.type,
                    title = meta.name,
                    logo = meta.logo,
                    poster = meta.poster,
                    background = meta.background,
                    seasonNumber = episode.season,
                    episodeNumber = episode.episode,
                    episodeTitle = episode.title,
                    episodeThumbnail = episode.thumbnail,
                    stream = playable,
                )
                if (result == DownloadEnqueueResult.Started || result == DownloadEnqueueResult.Replaced) {
                    startedKeys[key] = true
                } else {
                    NuvioToastController.show(result.toastMessage())
                }
            }
        }
    }

    NuvioModalBottomSheet(
        onDismissRequest = {
            scope.launch { dismissNuvioBottomSheet(sheetState = sheetState, onDismiss = onDismiss) }
        },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = nuvioSafeBottomPadding(16.dp)),
        ) {
            Text(
                text = stringResource(Res.string.season_download_title, seasonLabel),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            )

            if (addonTabs.isEmpty() && !anyLoading) {
                Text(
                    text = stringResource(Res.string.season_download_no_addons),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    addonTabs.forEach { (addonId, addonName) ->
                        val selected = addonId == activeAddonId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                )
                                .clickable { selectedAddonId = addonId }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = addonName,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp)) {
                    items(episodes, key = { it.id }) { episode ->
                        val group = results[episode.id].orEmpty().firstOrNull { it.addonId == activeAddonId }
                        val downloadable = group?.streams.orEmpty().filter {
                            it.playableDirectUrl != null || it.isAddonDebridCandidate
                        }
                        val expanded = expandedIds[episode.id] == true
                        val visible = if (expanded) downloadable else downloadable.take(CollapsedResultCount)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = "E${episode.episode ?: "?"} · ${episode.title}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            when {
                                downloadable.isEmpty() -> Text(
                                    text = stringResource(
                                        if (group == null && loadingIds[episode.id] != false) {
                                            Res.string.season_download_searching
                                        } else {
                                            Res.string.season_download_no_results
                                        },
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp),
                                )

                                else -> {
                                    visible.forEachIndexed { index, stream ->
                                        val key = "${episode.id}:$activeAddonId:$index"
                                        val started = startedKeys[key] == true
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { download(episode, stream, key) }
                                                .padding(vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = stream.streamLabel,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                                stream.streamSubtitle?.takeIf { it.isNotBlank() }?.let { subtitle ->
                                                    Text(
                                                        text = subtitle,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                }
                                            }
                                            Icon(
                                                imageVector = if (started) Icons.Default.CheckCircle else Icons.Rounded.Download,
                                                contentDescription = null,
                                                tint = if (started) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(start = 12.dp).size(24.dp),
                                            )
                                        }
                                    }
                                    if (downloadable.size > CollapsedResultCount) {
                                        TextButton(onClick = { expandedIds[episode.id] = !expanded }) {
                                            Text(
                                                text = if (expanded) {
                                                    stringResource(Res.string.season_download_show_less)
                                                } else {
                                                    stringResource(
                                                        Res.string.season_download_show_more,
                                                        downloadable.size - CollapsedResultCount,
                                                    )
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
