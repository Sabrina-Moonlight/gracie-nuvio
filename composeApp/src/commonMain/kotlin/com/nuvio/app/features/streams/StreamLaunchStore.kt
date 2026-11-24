package com.nuvio.app.features.streams

/** Capítulo pendiente dentro de una descarga de temporada (Gracie). */
data class DownloadQueueEpisode(
    val videoId: String,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val title: String?,
    val thumbnail: String?,
)

data class StreamLaunch(
    val profileId: Int,
    val type: String,
    val videoId: String,
    val parentMetaId: String? = null,
    val parentMetaType: String? = null,
    val title: String,
    val logo: String? = null,
    val poster: String? = null,
    val background: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val episodeTitle: String? = null,
    val episodeThumbnail: String? = null,
    val pauseDescription: String? = null,
    val resumePositionMs: Long? = null,
    val resumeProgressFraction: Float? = null,
    val manualSelection: Boolean = false,
    val startFromBeginning: Boolean = false,
    /**
     * Gracie: si no es null, la pantalla de streams está en "modo descarga de temporada":
     * al elegir un resultado se descarga (no se reproduce) y se pasa al siguiente capítulo de esta lista.
     * Lista vacía = es el último capítulo.
     */
    val downloadQueue: List<DownloadQueueEpisode>? = null,
    val downloadPosition: Int = 1,
    val downloadTotal: Int = 1,
)

object StreamLaunchStore {
    private var nextLaunchId = 1L
    private val launches = mutableMapOf<Long, StreamLaunch>()

    fun put(launch: StreamLaunch): Long {
        val launchId = nextLaunchId++
        launches[launchId] = launch
        return launchId
    }

    fun get(launchId: Long): StreamLaunch? = launches[launchId]

    fun remove(launchId: Long) {
        launches.remove(launchId)
    }

    fun clear() {
        nextLaunchId = 1L
        launches.clear()
    }
}
