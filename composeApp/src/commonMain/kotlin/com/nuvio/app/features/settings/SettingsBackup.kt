package com.nuvio.app.features.settings

import co.touchlab.kermit.Logger
import com.nuvio.app.core.poster.CustomPosterUrlRepository
import com.nuvio.app.core.poster.CustomPosterUrlStorage
import com.nuvio.app.core.ui.CardDepthStyleRepository
import com.nuvio.app.core.ui.CardDepthStyleStorage
import com.nuvio.app.core.ui.PosterCardStyleRepository
import com.nuvio.app.core.ui.PosterCardStyleStorage
import com.nuvio.app.features.addons.AddonStorage
import com.nuvio.app.features.collection.CollectionMobileSettingsRepository
import com.nuvio.app.features.collection.CollectionMobileSettingsStorage
import com.nuvio.app.features.debrid.DebridSettingsRepository
import com.nuvio.app.features.debrid.DebridSettingsStorage
import com.nuvio.app.features.details.MetaScreenSettingsRepository
import com.nuvio.app.features.details.MetaScreenSettingsStorage
import com.nuvio.app.features.home.HomeRepository
import com.nuvio.app.features.mdblist.MdbListMetadataService
import com.nuvio.app.features.mdblist.MdbListSettingsRepository
import com.nuvio.app.features.mdblist.MdbListSettingsStorage
import com.nuvio.app.features.notifications.EpisodeReleaseNotificationsRepository
import com.nuvio.app.features.player.PlayerSettingsRepository
import com.nuvio.app.features.player.PlayerSettingsStorage
import com.nuvio.app.features.plugins.PluginRepository
import com.nuvio.app.features.profiles.ProfileRepository
import com.nuvio.app.features.streams.StreamBadgeSettingsRepository
import com.nuvio.app.features.streams.StreamBadgeSettingsStorage
import com.nuvio.app.features.tmdb.TmdbSettingsRepository
import com.nuvio.app.features.tmdb.TmdbSettingsStorage
import com.nuvio.app.features.tracking.TrackingSettingsRepository
import com.nuvio.app.features.trakt.TraktAuthStorage
import com.nuvio.app.features.trakt.TraktCommentsSettings
import com.nuvio.app.features.trakt.TraktCommentsStorage
import com.nuvio.app.features.trakt.TraktSettingsStorage
import com.nuvio.app.features.watchprogress.ContinueWatchingPreferencesRepository
import com.nuvio.app.features.watchprogress.ContinueWatchingPreferencesStorage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * Local full-profile settings backup (export / import as a single JSON document).
 *
 * Unlike cloud profile sync, this intentionally includes API keys and auth tokens.
 * The user is expected to store the file securely.
 */
internal object SettingsBackup {
    private val log = Logger.withTag("SettingsBackup")

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    private const val CURRENT_VERSION = 1

    fun exportActiveProfileToJson(): String {
        ensureLoaded()
        val profileId = ProfileRepository.activeProfileId
        val blob = SettingsBackupBlob(
            version = CURRENT_VERSION,
            profileId = profileId,
            features = exportFeatures(profileId),
        )
        return json.encodeToString(SettingsBackupBlob.serializer(), blob)
    }

    fun importFromJson(raw: String): SettingsBackupResult {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            return SettingsBackupResult.Error("empty")
        }
        return try {
            val blob = json.decodeFromString(SettingsBackupBlob.serializer(), trimmed)
            if (blob.version < 1) {
                return SettingsBackupResult.Error("unsupported_version")
            }
            ensureLoaded()
            applyFeatures(blob.features)
            log.i { "import — applied backup for profileId=${ProfileRepository.activeProfileId}" }
            SettingsBackupResult.Success
        } catch (error: Exception) {
            log.e(error) { "import — FAILED" }
            SettingsBackupResult.Error("parse_or_apply")
        }
    }

    private fun exportFeatures(profileId: Int): SettingsBackupFeatures {
        return SettingsBackupFeatures(
            themeSettings = ThemeSettingsStorage.exportToSyncPayload(),
            posterCardStyleSettingsPayload = PosterCardStyleStorage.loadPayload().orEmpty().trim(),
            customPosterUrlPattern = CustomPosterUrlStorage.loadPattern().orEmpty().trim(),
            customPosterEnabledScreens = CustomPosterUrlStorage.loadEnabledScreens()
                ?.joinToString(",")
                .orEmpty(),
            cardDepthStyleSettingsPayload = CardDepthStyleStorage.loadPayload().orEmpty().trim(),
            // Full payloads — credentials included on purpose.
            playerSettings = PlayerSettingsStorage.exportToSyncPayload(),
            streamBadgeSettings = StreamBadgeSettingsStorage.exportToSyncPayload(),
            debridSettings = DebridSettingsStorage.exportToSyncPayload(),
            tmdbSettings = TmdbSettingsStorage.exportToSyncPayload(),
            mdbListSettings = MdbListSettingsStorage.exportToSyncPayload(),
            metaScreenSettingsPayload = MetaScreenSettingsStorage.loadPayload().orEmpty().trim(),
            collectionMobileSettingsPayload = CollectionMobileSettingsStorage.loadPayload().orEmpty().trim(),
            continueWatchingSettingsPayload = ContinueWatchingPreferencesStorage.loadPayload().orEmpty().trim(),
            traktSettingsPayload = TraktSettingsStorage.loadPayload().orEmpty().trim(),
            traktCommentsSettings = TraktCommentsStorage.exportToSyncPayload(),
            traktAuthPayload = TraktAuthStorage.loadPayload(profileId).orEmpty(),
            notificationsSettings = NotificationsBackupPayload(
                episodeReleaseAlertsEnabled = EpisodeReleaseNotificationsRepository.uiState.value.isEnabled,
            ),
            addons = AddonsBackupPayload(
                installedUrls = AddonStorage.loadInstalledAddonUrls(profileId),
                enabledStates = AddonStorage.loadAddonEnabledStates(profileId),
            ),
            plugins = exportPlugins(),
        )
    }

    private fun exportPlugins(): PluginsBackupPayload {
        val state = PluginRepository.uiState.value
        return PluginsBackupPayload(
            pluginsEnabled = state.pluginsEnabled,
            groupStreamsByRepository = state.groupStreamsByRepository,
            repositories = state.repositories.mapIndexed { index, repo ->
                PluginRepositoryBackupItem(
                    url = repo.manifestUrl,
                    name = repo.name,
                    sortOrder = index,
                )
            },
            scraperEnabled = state.scrapers.associate { it.id to it.enabled },
        )
    }

    private fun applyFeatures(features: SettingsBackupFeatures) {
        val profileId = ProfileRepository.activeProfileId

        ThemeSettingsStorage.replaceFromSyncPayload(features.themeSettings)
        ThemeSettingsRepository.onProfileChanged()

        PosterCardStyleStorage.savePayload(features.posterCardStyleSettingsPayload)
        PosterCardStyleRepository.onProfileChanged()

        CustomPosterUrlStorage.savePattern(features.customPosterUrlPattern.ifBlank { null })
        val remoteScreenKeys = features.customPosterEnabledScreens
            .takeIf { it.isNotBlank() }
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.toSet()
        CustomPosterUrlStorage.saveEnabledScreens(remoteScreenKeys)
        CustomPosterUrlRepository.onProfileChanged()
        HomeRepository.applyCurrentSettings()

        CardDepthStyleStorage.savePayload(features.cardDepthStyleSettingsPayload)
        CardDepthStyleRepository.onProfileChanged()

        PlayerSettingsStorage.replaceFromSyncPayload(features.playerSettings)
        PlayerSettingsRepository.onProfileChanged()

        StreamBadgeSettingsStorage.replaceFromSyncPayload(features.streamBadgeSettings)
        StreamBadgeSettingsRepository.onProfileChanged()

        DebridSettingsStorage.replaceFromSyncPayload(features.debridSettings)
        DebridSettingsRepository.onProfileChanged()

        TmdbSettingsStorage.replaceFromSyncPayload(features.tmdbSettings)
        TmdbSettingsRepository.onProfileChanged()

        MdbListSettingsStorage.replaceFromSyncPayload(features.mdbListSettings)
        MdbListMetadataService.clearCache()
        MdbListSettingsRepository.onProfileChanged()

        MetaScreenSettingsStorage.savePayload(features.metaScreenSettingsPayload)
        MetaScreenSettingsRepository.onProfileChanged()

        CollectionMobileSettingsStorage.savePayload(features.collectionMobileSettingsPayload)
        CollectionMobileSettingsRepository.onProfileChanged()

        ContinueWatchingPreferencesStorage.savePayload(features.continueWatchingSettingsPayload)
        ContinueWatchingPreferencesRepository.onProfileChanged()

        TraktSettingsStorage.savePayload(features.traktSettingsPayload)
        TrackingSettingsRepository.onProfileChanged()

        TraktCommentsStorage.replaceFromSyncPayload(features.traktCommentsSettings)
        TraktCommentsSettings.onProfileChanged()

        if (features.traktAuthPayload.isNotBlank()) {
            TraktAuthStorage.savePayload(profileId, features.traktAuthPayload)
        }

        EpisodeReleaseNotificationsRepository.applyFromSyncEnabled(
            features.notificationsSettings.episodeReleaseAlertsEnabled,
        )

        AddonStorage.saveInstalledAddonUrls(profileId, features.addons.installedUrls)
        AddonStorage.saveAddonEnabledStates(profileId, features.addons.enabledStates)

        applyPlugins(features.plugins)
    }

    private fun applyPlugins(payload: PluginsBackupPayload) {
        PluginRepository.setPluginsEnabled(payload.pluginsEnabled)
        PluginRepository.setGroupStreamsByRepository(payload.groupStreamsByRepository)

        // Scraper enable flags for scrapers already present on this device.
        // Repository URLs that are missing must be re-added from Settings → Plugins
        // (addRepository is suspend / network-bound).
        payload.scraperEnabled.forEach { (scraperId, enabled) ->
            val current = PluginRepository.uiState.value.scrapers.firstOrNull { it.id == scraperId }
            if (current != null && current.enabled != enabled) {
                PluginRepository.toggleScraper(scraperId, enabled)
            }
        }
    }

    private fun ensureLoaded() {
        ThemeSettingsRepository.ensureLoaded()
        PosterCardStyleRepository.ensureLoaded()
        CustomPosterUrlRepository.ensureLoaded()
        CardDepthStyleRepository.ensureLoaded()
        PlayerSettingsRepository.ensureLoaded()
        StreamBadgeSettingsRepository.ensureLoaded()
        DebridSettingsRepository.ensureLoaded()
        TmdbSettingsRepository.ensureLoaded()
        MdbListSettingsRepository.ensureLoaded()
        MetaScreenSettingsRepository.ensureLoaded()
        CollectionMobileSettingsRepository.ensureLoaded()
        ContinueWatchingPreferencesRepository.ensureLoaded()
        TrackingSettingsRepository.ensureLoaded()
        EpisodeReleaseNotificationsRepository.ensureLoaded()
        PluginRepository.initialize()
    }
}

internal sealed class SettingsBackupResult {
    data object Success : SettingsBackupResult()
    data class Error(val reason: String) : SettingsBackupResult()
}

@Serializable
private data class SettingsBackupBlob(
    val version: Int = 1,
    @SerialName("profile_id") val profileId: Int = 0,
    val features: SettingsBackupFeatures = SettingsBackupFeatures(),
)

@Serializable
private data class SettingsBackupFeatures(
    @SerialName("theme_settings") val themeSettings: JsonObject = JsonObject(emptyMap()),
    @SerialName("poster_card_style_settings_payload") val posterCardStyleSettingsPayload: String = "",
    @SerialName("custom_poster_url_pattern") val customPosterUrlPattern: String = "",
    @SerialName("custom_poster_enabled_screens") val customPosterEnabledScreens: String = "",
    @SerialName("card_depth_style_settings_payload") val cardDepthStyleSettingsPayload: String = "",
    @SerialName("player_settings") val playerSettings: JsonObject = JsonObject(emptyMap()),
    @SerialName("stream_badge_settings") val streamBadgeSettings: JsonObject = JsonObject(emptyMap()),
    @SerialName("debrid_settings") val debridSettings: JsonObject = JsonObject(emptyMap()),
    @SerialName("tmdb_settings") val tmdbSettings: JsonObject = JsonObject(emptyMap()),
    @SerialName("mdblist_settings") val mdbListSettings: JsonObject = JsonObject(emptyMap()),
    @SerialName("meta_screen_settings_payload") val metaScreenSettingsPayload: String = "",
    @SerialName("collection_mobile_settings_payload") val collectionMobileSettingsPayload: String = "",
    @SerialName("continue_watching_settings_payload") val continueWatchingSettingsPayload: String = "",
    @SerialName("trakt_settings_payload") val traktSettingsPayload: String = "",
    @SerialName("trakt_comments_settings") val traktCommentsSettings: JsonObject = JsonObject(emptyMap()),
    @SerialName("trakt_auth_payload") val traktAuthPayload: String = "",
    @SerialName("notifications_settings") val notificationsSettings: NotificationsBackupPayload = NotificationsBackupPayload(),
    val addons: AddonsBackupPayload = AddonsBackupPayload(),
    val plugins: PluginsBackupPayload = PluginsBackupPayload(),
)

@Serializable
private data class NotificationsBackupPayload(
    @SerialName("episode_release_alerts_enabled") val episodeReleaseAlertsEnabled: Boolean = false,
)

@Serializable
private data class AddonsBackupPayload(
    @SerialName("installed_urls") val installedUrls: List<String> = emptyList(),
    @SerialName("enabled_states") val enabledStates: Map<String, Boolean> = emptyMap(),
)

@Serializable
private data class PluginsBackupPayload(
    @SerialName("plugins_enabled") val pluginsEnabled: Boolean = true,
    @SerialName("group_streams_by_repository") val groupStreamsByRepository: Boolean = false,
    val repositories: List<PluginRepositoryBackupItem> = emptyList(),
    @SerialName("scraper_enabled") val scraperEnabled: Map<String, Boolean> = emptyMap(),
)

@Serializable
private data class PluginRepositoryBackupItem(
    val url: String = "",
    val name: String = "",
    @SerialName("sort_order") val sortOrder: Int = 0,
)
