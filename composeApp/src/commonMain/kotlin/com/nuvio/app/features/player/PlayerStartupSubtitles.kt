package com.nuvio.app.features.player

import com.nuvio.app.features.streams.StreamSubtitle

internal fun String.isLocalSubtitleUri(): Boolean {
    val trimmed = trim()
    val scheme = trimmed.substringBefore(':', missingDelimiterValue = "")
        .lowercase()
    return when {
        scheme == "file" || scheme == "content" || scheme == "android.resource" -> true
        ':' !in trimmed && trimmed.startsWith("/") -> true
        else -> false
    }
}

internal fun StreamSubtitle.toAddonSubtitle(): AddonSubtitle = AddonSubtitle(
    id = url,
    url = url,
    language = language,
    display = name?.takeIf { it.isNotBlank() } ?: language,
    addonName = name,
)

internal fun mergeStreamAndAddonSubtitles(
    addonSubtitles: List<AddonSubtitle>,
    streamSubtitles: List<StreamSubtitle>,
): List<AddonSubtitle> {
    val remoteStreamSubtitles = streamSubtitles.filterNot { it.url.isLocalSubtitleUri() }
    if (remoteStreamSubtitles.isEmpty()) return addonSubtitles
    // Los addons van primero; los subtítulos que trae el stream quedan después.
    return (addonSubtitles + remoteStreamSubtitles.map { it.toAddonSubtitle() })
        .distinctBy { "${it.id}|${it.url}" }
}
