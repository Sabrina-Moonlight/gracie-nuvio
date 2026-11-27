# ELIMINADO-GRACIE.md — Lo que Gracie quitó en la limpieza (v01-v22)

## Regla
Si algo de esta lista reaparece después de usar Update Nuvio, **NO es una novedad de Nuvio**: es viejo, el workflow lo trajo de vuelta. **Quítalo otra vez.**
Solo se devuelve la pieza mínima si el código NUEVO de Nuvio la necesita para compilar ("Unresolved reference"). Si eso pasa, se anota abajo en "Devueltos".
Cómo comprobar: `grep -w NOMBRE` en toda la rama nueva por cada nombre de la lista. Si aparece, mira si el código nuevo de Nuvio lo usa; si nadie lo usa, bórralo.

## NO quitar ni tocar (parece sin uso, pero se usa)
- `layout/exo_player_view.xml` (androidMain/res): nadie lo nombra, pero reemplaza el diseño del PlayerView.
- Campos `@Serializable` guardados en disco: `focusGifEnabled`, `heroBackdropUrl`, `heroVideoUrl`, `titleLogoUrl`, `schemaVersion` (Simkl), `monthLabel`, `cinematicBackground`, `serverRevision`, `tconst`, `streamable`, `retryable`.
- Los 13 `.aar` de `composeApp/libs`.
- `InfusePlayback.kt` (se usa en `MainAppContent.kt` y `AppUrlBridge.kt`).
- `tracking/TrackingAttributedItem.kt` (v18, lo piden 3 modelos).
- `baseline-prof.txt` / `startup-prof.txt` (aún nombran cosas quitadas; no rompen).
- `media_movie` (string): se usa.
- Los guardados de ajustes `setLibassRenderType` (guardado) y `DebridSettingsStorage.save*`: se usan en sincronización/migración.

## Archivos enteros borrados
| Archivo | Ver. | Motivo |
|---|---|---|
| `AppGateOverlay.kt` | v01 | viejo, sin uso (iPhone) |
| `plugins/runtime/wasm/WasmBridge.kt` | v03 | módulo vacío |
| `tracking/TrackingAttribution.kt` (+ test `TrackingAttributionTest.kt`) | v04 | sin uso (la interfaz se devolvió en v18) |
| `autosync/AutoSyncSampledReferenceAligner.kt` | v08 | sin uso |
| `autosync/AutoSyncRetryAction.kt` | v09 | botón de reintento, nunca se mostraba |
| `player/PlayerLibassExtensions.kt` (androidMain) | v17 | sin uso |
| `strings_pip.xml` (values, es, iw, zh-Hant) | v19 | solo tenía `player_picture_in_picture` |
| 13 `.svg` de `composeResources/drawable`: `ic_player_aspect_ratio/audio_filled/episodes/pause/play/source/subtitles`, `library_add_plus`, `mdblist_logo`, `simkl_logo_glyph/wordmark`, `trakt_logo_wordmark`, `trakt_tv_favicon` | v20 | copias; la app usa los `.xml` de androidMain |
| `androidMain/res/values/nuvio_rs_strings.xml` | v21 | aviso "Nuvio RS" sin uso |

## Funciones, clases y propiedades quitadas (nombre · archivo · versión)
- `navigationIdentity` · navigation/Routes.kt · v01
- `externalPlayerUnavailableText` · MainAppContent.kt · v01
- `tabsRouteActive` · MainTabsDestination.kt · v01
- `AuthState.isLoggedIn` · core/auth/AuthModels.kt · v01
- `localizedMovieTypeLabel`, `localizedPlayerPictureInPicture` · core/i18n/LocalizedUiText.kt · v01
- `CustomPosterUrlRepository.isScreenEnabled` · v01
- `P2pStreaming.setEnableUpload` · v01
- `LiveTvPlaybackRegistry.listUrlFor` · v01
- `WatchingState.areEpisodesWatched`, `WatchingEpisodeRef` · watching · v01
- `readResponseBodyLimited` · AddonPlatform.android.kt · v02
- `torboxRequestIdParameterName` (+ su test) · TorboxCloudLibraryProviderApi.kt · v02
- `shouldMirrorWatchedMarkToTrackers` (+ test) · WatchedRepository.kt · v03
- `isoCalendarDateOrNull` · WatchingPolicies.kt · v03
- `connectedListWriters`, `connectedHistoryWriters`, `connectedProgressProviders`, `listWriter`, `historyWriter` · TrackingProvider.kt · v04
- `onCancelDeviceFlow`, `onAuthLaunchFailed` · TraktAuthRepository.kt · v04
- `scrobbleStart`, `scrobbleStop` · TraktScrobbleRepository.kt · v04
- `prefetchEpisodes`, `getCachedEpisodeMapping`, `prefetchEpisodeMapping` · TraktEpisodeMappingService.kt · v04
- `currentListTabs` · TraktLibraryRepository.kt · v04
- `shouldUseTraktLibrary` · TraktSettingsRepository.kt · v04
- `selectWatchProgressSource` · WatchProgressRepository.kt · v04
- `scope` privado, `clearPinWithPassword`, `pullProfileLocks`, `ProfileLockState` · ProfileRepository.kt · v05
- `PopupAddProfileBubble`, `PopupProfileBubble` · ProfileSwitcherTab.kt · v05
- `moveFolderUp`, `moveFolderDown` · CollectionEditorRepository.kt · v05
- `generateId` · CollectionRepository.kt · v05
- `searchKeywords`, `TmdbKeywordSearchResponse/Result` · TmdbCollectionSourceResolver.kt · v05
- `toNuvioPosterShape` (duplicada) · FolderDetailScreen.kt · v05
- `parseCatalog` · HomeCatalogParser.kt · v05
- `collectionMap` (local) · HomeCatalogSettingsRepository.kt · v05
- `setCinematicBackground` · MetaScreenSettingsRepository.kt · v06
- `firstPlayableEpisode`, `firstReleasedPlayableEpisode` · SeriesPlaybackResolver.kt · v06
- `setTorboxApiKey`, `setRealDebridApiKey`, `setPremiumizeApiKey`, `setStreamMaxResults`, `setStreamSortMode`, `setStreamMinimumQuality`, `setStreamDolbyVisionFilter`, `setStreamHdrFilter`, `setStreamCodecFilter` · DebridSettingsRepository.kt · v07
- `configuredSourceNames`, `instantName` · DebridProvider.kt · v07
- `premiumizeApiKey` · DebridSettings.kt · v07
- `showFileSizeBadgesSnapshot`, `badgePlacementSnapshot` · StreamBadgeSettingsRepository.kt · v07
- `LibmpvAutoSyncCoordinator` (función) · AutoSyncLibmpvCoordinator.android.kt · v08
- `findDelayOnlyAlignment`, `buildDelayOnlyTimeline`, `DELAY_ONLY_MIN_CUES` · AutoSyncTimelineRetime.kt · v08
- `parseReport`, `decode`, `replay` · AutoSyncTimingDump.kt · v08
- `normalizeToUtf8` · SubtitleCharsetDetector.kt · v08
- Reintento de AutoSync: `AutoSyncRetryStatus`, `AutoSyncRetryUiState`, `mergeRejectedReferenceKeys`, `RetryContext`, `retryWithAnotherReference`, `invalidateRetryContext`, strings `autosync_retry_*`, `autosync_toast_retry_synced/none/failed` · v09-v10
- `excludedReferenceKeys`, `requiredReferenceSource`, `onReferenceSearchOutcome`, `AutoSyncReferenceSearchOutcome`, `hadEligibleBeforeExclusion` · AutomaticSubtitleSync.kt · v11
- `AutoSyncResolvedTimeline.reference`, `AutoSyncReferenceIdentity`, `equivalentReferenceKeysFor`, `AutoSyncReferenceSource`, `referenceSource` · AutomaticSubtitleSync.kt · v12
- `NetworkStatusUiState.isOnline` · NetworkStatusRepository.kt · v14
- `NuvioNavigationBar`, `NuvioNavigationBarScopeImpl`, `NavItemLabel` · NavigationBar.kt · v15
- `NativeProfileSwitcherController`, `NativeProfileOption`, `NativeProfileSwitcherState`, `nativeTabSelect`, parámetro `nativeProfileSwitcherController` de `App`/`AppGate` · v15
- `nuvioOverlayGradientBrush` · Gradients.kt; `Space.s7`, `Z.playerOverlay` · Tokens.kt · v15
- `showReshapedLiveTvChannelsPanel` · PlayerScreenRuntimeState.kt · v16
- `setLibassRenderType` · PlayerSettingsRepository.kt · v16
- `SubtitleBackgroundColorSwatches`, `SubtitleAudioUiState`, `speechHeardSec` · v16
- `libmpvVolumeForBoost`, `LIBMPV_VOLUME_MAX` · VolumeBoostAndroid.kt; `activeSessionKey` · AudioSubtitleSyncController.kt; `LibassRenderType` · v17

## Strings quitados (nombres)
- v19, en los 4 idiomas: `action_delete_confirm_title`, `compose_player_seek_delta_backward/forward`, `external_player_unavailable`, `library_calendar_month_events`, `library_calendar_no_month_events`, `library_calendar_selected_day`, `player_picture_in_picture`, `settings_advanced_backup_import_hint`, `settings_stream_connection_fit_description`.
- v21: los 19 de `nuvio_rs_strings.xml`.
- v22: traducciones sin texto base (6 en es; 161 en iw y zh-Hant), entre ellas `downloads_section_movies/shows`, `downloads_empty_episodes`, `downloads_episode_count`, `downloads_status_completed`, `compose_player_episode_code`, grupos `tmdb_sources_*`, `updates_*`, `submit_intro_*`, `player_torrent_*`, `plugins_error_*`.
- Quitados a propósito antes: `settings_playback_threshold_minutes`, `player_engine_unavailable_rebuild`.

## Devueltos por necesidad (anotar aquí)
- `TrackingAttributedItem` (interfaz) — v18, la piden `LibraryModels`, `WatchedModels`, `WatchProgressModels`.
