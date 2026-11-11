package com.nuvio.app.features.player

import com.nuvio.app.features.streams.BingeGroupCacheRepository
import com.nuvio.app.features.streams.StreamLinkCacheRepository
import com.nuvio.app.features.watchprogress.ContinueWatchingItem

/**
 * Clears playback-related caches for a continue-watching item (stream link, binge group,
 * and platform disk/torrent data when available).
 */
internal object ContentPlaybackCacheCleaner {
    fun clearForContinueWatchingItem(item: ContinueWatchingItem) {
        clearForContent(
            type = item.parentMetaType,
            videoId = item.videoId,
            parentMetaId = item.parentMetaId,
            season = item.seasonNumber,
            episode = item.episodeNumber,
        )
    }

    fun clearForContent(
        type: String,
        videoId: String,
        parentMetaId: String? = null,
        season: Int? = null,
        episode: Int? = null,
    ) {
        val entry = StreamLinkCacheRepository.removeForContent(
            type = type,
            videoId = videoId,
            parentMetaId = parentMetaId,
            season = season,
            episode = episode,
        )
        val bingeInfoHash = parentMetaId?.takeIf { it.isNotBlank() }
            ?.let(BingeGroupCacheRepository::getInfoHash)
        parentMetaId?.takeIf { it.isNotBlank() }?.let(BingeGroupCacheRepository::remove)
        val urls = buildList {
            entry?.url?.takeIf { it.isNotBlank() }?.let(::add)
        }
        PlatformContentPlaybackCache.clearUrls(urls)
        val infoHash = entry?.infoHash?.takeIf { it.isNotBlank() } ?: bingeInfoHash
        PlatformContentPlaybackCache.clearP2pInfoHash(infoHash)
    }
}

/**
 * Platform disk caches (ExoPlayer SimpleCache / P2P payload). No-op on platforms without them.
 */
internal expect object PlatformContentPlaybackCache {
    fun clearUrls(urls: Collection<String>)
    fun clearP2pInfoHash(infoHash: String?)
}
