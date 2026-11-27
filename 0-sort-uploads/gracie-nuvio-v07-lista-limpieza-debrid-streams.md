# Lista: revisión de bugs y código sin uso (gracie-nuvio)

## Tarea
Encontrar bugs, código sin uso (para eliminarlo) y código mejorable. ~263.000 líneas de Kotlin, por eso se trabaja por partes.

## Paso a paso (se repite en CADA parte)
1. **Analizar.** Leer el módulo y anotar bugs, código sin uso y código mejorable.
   "Sin uso" = `grep -w` en TODO el proyecto (todos los sets, tests, recursos, manifest). Cuidado con `expect/actual`, `@Serializable`, `@Preview`, manifest, reflexión.
2. **Plan en el chat** (2 a 4 líneas). Sin plan no se edita.
3. **Editar** lo mínimo: bugs de raíz, sin uso = eliminar, mejoras = cambio chico.
4. **Segundo vistazo:** `grep` de que lo eliminado no lo usa nadie, que imports y sintaxis siguen bien, y que la app queda igual. Si no se puede confirmar, NO se borra: va a "dudoso".
5. **Entregar** ZIP + esta lista suelta (fuera del ZIP).
6. Al final: **tabla** con Bugs | Código sin uso | Código mejorable.

## Partes
- [x] **P1a** Candidatos sin uso de módulos chicos, raíz de `app` y navigation → `gracie-nuvio-v01-chore-limpieza-codigo.zip`
- [x] **P1b** addons, auth, catalog, cloud, membership, notifications → `gracie-nuvio-v02-chore-limpieza-addons-cloud.zip`
- [x] **P1c** search, shuffle, tmdb, trailer, updater, watched, watching, pillnav, p2p, plugins → `gracie-nuvio-v03-chore-limpieza-watched-plugins.zip`
- [x] **P2** tracking, trakt, simkl, watchprogress → `gracie-nuvio-v04-chore-limpieza-trakt-tracking.zip`
- [x] **P3** library, collection, profiles, home → `gracie-nuvio-v05-chore-limpieza-perfiles-home.zip`
- [x] **P4** details (+ components) → `gracie-nuvio-v06-chore-limpieza-details.zip`
- [x] **P5** streams, debrid, servers → `gracie-nuvio-v07-chore-limpieza-debrid-streams.zip`
- [ ] **P6** downloads, party, autosync
- [ ] **P7** mdblist
- [ ] **P8** settings (partir en 2)
- [ ] **P9** core (partir en 2)
- [ ] **P10** player commonMain (parte 1)
- [ ] **P11** player commonMain (parte 2) + androidMain/player
- [ ] **P12** recursos sin uso (drawables, strings, mipmaps, `composeApp/libs`)
- [ ] **P13** tabla final

## Hecho
**v01 — `gracie-nuvio-v01-chore-limpieza-codigo.zip` (P1a)**
Eliminado (0 usos en todo el proyecto, comprobado con `grep -w`):
- `AppGateOverlay.kt` (archivo entero; resto de iOS, `iosApp` ya no tiene código)
- `navigationIdentity` en `navigation/Routes.kt` (resto de SwiftUI)
- `externalPlayerUnavailableText` en `MainAppContent.kt` (el resultado del reproductor externo no tiene caso "no disponible")
- `tabsRouteActive` en `MainTabsDestination.kt`
- `AuthState.isLoggedIn` en `core/auth/AuthModels.kt`
- `localizedMovieTypeLabel` y `localizedPlayerPictureInPicture` en `core/i18n/LocalizedUiText.kt` (+2 imports)
- `CustomPosterUrlRepository.isScreenEnabled`
- `P2pStreaming.setEnableUpload` (la opción se sigue guardando/leyendo en backup)
- `LiveTvPlaybackRegistry.listUrlFor`
- `WatchingState.areEpisodesWatched`
- `WatchingEpisodeRef` en `WatchingModels.kt`

**v02 — `gracie-nuvio-v02-chore-limpieza-addons-cloud.zip` (P1b)**
Eliminado (0 usos, comprobado con `grep -w`):
- `readResponseBodyLimited` en `androidMain/.../addons/AddonPlatform.android.kt` (privada; `readAtMostBytes` y `ResponseBody` se siguen usando)
- `torboxRequestIdParameterName` en `cloud/TorboxCloudLibraryProviderApi.kt` (el código real usa `DebridApiClients.kt`) y su test en `commonTest/.../TorboxCloudLibraryProviderApiTest.kt`

**v03 — `gracie-nuvio-v03-chore-limpieza-watched-plugins.zip` (P1c)**
- Eliminado `shouldMirrorWatchedMarkToTrackers` (`watched/WatchedRepository.kt`): el código real compara `trackerHistorySync == Mirror` directamente; solo lo usaba un test, que también se quitó.
- Eliminado el alias `isoCalendarDateOrNull` (`watching/domain/WatchingPolicies.kt`); su test ahora llama a `parseEpisodeReleaseLocalDate` (hace exactamente lo mismo).
- Eliminado `plugins/runtime/wasm/WasmBridge.kt` (módulo vacío, solo un TODO) y su registro en `PluginRuntime.kt`.

**v04 — `gracie-nuvio-v04-chore-limpieza-trakt-tracking.zip` (P2)**
Eliminado (0 usos, comprobado con `grep -w`):
- `TrackingProvider.kt`: `connectedListWriters`, `connectedHistoryWriters`, `connectedProgressProviders`, `listWriter`, `historyWriter`
- `TraktAuthRepository.kt`: `onCancelDeviceFlow`, `onAuthLaunchFailed`
- `TraktScrobbleRepository.kt`: `scrobbleStart`, `scrobbleStop` (el scrobble real usa `scrobble()`)
- `TraktEpisodeMappingService.kt`: `prefetchEpisodes`, `getCachedEpisodeMapping`, `prefetchEpisodeMapping` (+4 imports)
- `TraktLibraryRepository.kt`: `currentListTabs`
- `TraktSettingsRepository.kt`: `shouldUseTraktLibrary`
- `WatchProgressRepository.kt`: `selectWatchProgressSource` (la pantalla llama directo a `WatchProgressSourceCoordinator.selectSource`)
- Archivo entero `tracking/TrackingAttribution.kt` + su test `TrackingAttributionTest.kt` (solo ese test lo usaba)

**v05 — `gracie-nuvio-v05-chore-limpieza-perfiles-home.zip` (P3)**
Eliminado (0 usos, comprobado con `grep -w`):
- `profiles/ProfileRepository.kt`: `scope` privado, `clearPinWithPassword`, `pullProfileLocks`, clase `ProfileLockState` (+3 imports)
- `profiles/ProfileSwitcherTab.kt`: `PopupAddProfileBubble` y `PopupProfileBubble` (~230 líneas, + 11 imports)
- `collection/CollectionEditorRepository.kt`: `moveFolderUp`, `moveFolderDown` (queda `moveFolderByIndex`)
- `collection/CollectionRepository.kt`: `generateId` (+2 imports)
- `collection/TmdbCollectionSourceResolver.kt`: `searchKeywords` y sus clases `TmdbKeywordSearchResponse/Result`
- `collection/FolderDetailScreen.kt`: `toNuvioPosterShape` duplicada (+1 import)
- `home/HomeCatalogParser.kt`: `parseCatalog` (queda `parseCatalogResponse`)
- `home/HomeCatalogSettingsRepository.kt`: variable local `collectionMap`
Arreglado: 3 `activeProfile!!` en `ProfileRepository.kt` (leían el estado dos veces; si cambiaba en medio podía cerrar la app) → `?.let`.

**v06 — `gracie-nuvio-v06-chore-limpieza-details.zip` (P4)**
- `details/MetaScreenSettingsRepository.kt`: eliminado `setCinematicBackground` (la reemplaza `setBackgroundMode`; el campo `cinematicBackground` se queda porque se guarda en disco).
- `details/SeriesPlaybackResolver.kt`: eliminados `firstPlayableEpisode` y `firstReleasedPlayableEpisode`.
- Comprobado: no quedaron huérfanos por cascada (detector global repetido: 0 candidatos nuevos).

**v07 — `gracie-nuvio-v07-chore-limpieza-debrid-streams.zip` (P5)**
Eliminado (0 usos, comprobado con `grep -w`):
- `debrid/DebridSettingsRepository.kt`: 9 setters viejos (`setTorboxApiKey`, `setRealDebridApiKey`, `setPremiumizeApiKey`, `setStreamMaxResults`, `setStreamSortMode`, `setStreamMinimumQuality`, `setStreamDolbyVisionFilter`, `setStreamHdrFilter`, `setStreamCodecFilter`). Los reemplazan `setProviderApiKey` y `setStreamPreferences`. Las funciones auxiliares y `DebridSettingsStorage.save*` se mantienen: las usan la migración de ajustes viejos y la sincronización.
- `debrid/DebridProvider.kt`: `configuredSourceNames` y, por cascada, `instantName`
- `debrid/DebridSettings.kt`: `premiumizeApiKey`
- `streams/StreamBadgeSettingsRepository.kt`: `showFileSizeBadgesSnapshot`, `badgePlacementSnapshot`

## Hallazgos acumulados
| Parte | Bugs | Código sin uso | Código mejorable | Dudoso (no tocado) |
|---|---|---|---|---|
| P5 | ninguno | 13 funciones/propiedades | `ConnectionSpeed.kt:228-229` y `PlexProvider.kt:397` usan `!!` (seguros: se comprueba antes). | Solo tests: `isServerSource`, `hasPlayableSource`, `DebridSettings.torboxApiKey/realDebridApiKey/hasAnyApiKey`. `SiloPlayback.retryable` nadie lo lee (dato de respuesta, no tocado). `DebridApiModels.streamable` es dato de red. |
| P4 | ninguno | 3 funciones | `MetaDetailsScreen.kt` líneas ~1303 y ~1656: el mismo bloque "cargar más comentarios" está copiado dos veces y ambos tragan cualquier error con `catch (_: Exception) { }` (también la cancelación; inofensivo hoy). `EpisodeRatingsAlignment.kt:61-62` usa `!!` (seguro, filtrado antes). | `SeriesGraphApi.tconst` es campo de datos de la red, no tocar. |
| P3 | 3 lecturas dobles con `!!` en `ProfileRepository` (posible cierre por condición de carrera): arregladas | 17 funciones/clases | `PosterGrid.kt:147`, `ProfileEditScreen.kt:368`, `CollectionEditorRepository.kt:629,686` usan `!!` (seguros hoy) | Solo tests: `LibraryLocalState.isContentCurrent` y `runIfContentCurrent`. Campos guardados en disco, NO tocar: `focusGifEnabled`, `heroBackdropUrl`, `heroVideoUrl`, `titleLogoUrl`. `serverRevision` sí se usa (compara si cambió el servidor). |
| P2 | ninguno | 16 funciones + 1 archivo con su test | `TrackingProvider`: los mapas `listWriters` e `historyWriters` ahora solo se escriben y nadie los lee (los escritores se usan directo). `TraktEpisodeMappingService`: los contadores de estadísticas se calculan pero `logAndResetStats` nadie la llama. `ResumePromptRepository.kt:56` usa `!!` (seguro). | Solo usados por tests (no se tocan): `traktBestLandscapeUrl`, `parseTraktListId`, `ContinueWatchingEnrichmentCache.getNextUpSnapshot`, `WatchProgressCodec.encodeEntries/decodeEntries`, `byProgressKey/byVideoId`, `supportsReordering/reorderLists` (interfaz de MDBList). `SimklSyncModels.schemaVersion` es un campo guardado en disco, no tocar. |
| P1c | ninguno | 2 funciones solo-test + 1 módulo vacío (WasmBridge) | `ShuffleEpisodeState.kt:29` y `PluginsSettingsScreen.kt:508-510` usan `!!` (seguros hoy, se podrían cambiar por `?.let`) | — |
| P1b | ninguno | 2 funciones + 1 test de una función muerta | `CatalogScreen.kt:334` usa `!!` (hoy es seguro por `hasFallback`; se podría cambiar por `?.let`) | — |
| P1a | ninguno encontrado | los 12 de arriba | — | `AppUpdater.ignoreThisVersion`: nadie la llama, así que "ignorar versión" nunca se activa aunque `getIgnoredTag/setIgnoredTag` existan en 3 sets. Decidir: añadir botón o quitar todo. |

## Para retomar (chat nuevo)
- Proyecto: `gracie-nuvio-main.zip` → `gracie-nuvio-main/`. `APP` = `nuvio`. Siguiente ZIP: `gracie-nuvio-v08-...`.
- Código en `composeApp/src/` (sets: commonMain, androidMain, fullCommonMain, androidFull, androidPlaystore, tests). No hay iosMain.
- Suposición: no se puede compilar aquí; verificación por lectura y `grep`.
- **P12: revisar también los strings que usaban los globos de perfil borrados (`ProfileSwitcherTab`).**
- **Recursos que quedaron sin uso por P1a (revisar en P12):** `media_movie`, `player_picture_in_picture`, `external_player_unavailable` (values y values-es). Confirmar con `grep` antes de borrar.
- **Método rápido usado:** script que busca declaraciones (`fun/class/object/val`) cuyo nombre aparece 1 sola vez en todo el proyecto. Lo que sigue son los candidatos restantes (actualizado tras P5: 76) (formato `ruta tipo nombre`; ruta relativa a `composeApp/src/`, sin `kotlin/com/nuvio/app/`). Todos están SIN verificar: revisar cada uno con `grep -w` y leer el contexto antes de borrar (algunos pueden ser iOS/librería/API pública).
  Método 2 (usado en P1b): privados sin uso + nombres que solo usa un test + búsqueda de `!!`/`TODO`/`catch` vacíos. Aplicar el mismo a cada módulo.

```
./androidApp/src/androidTest/kotlin/com/nuvio/android/BackgroundDownloadTest.kt class BackgroundDownloadTest
./androidApp/src/androidTest/kotlin/com/nuvio/android/BackgroundDownloadTest.kt fun verifyCompletedDownload
./androidApp/src/androidTest/kotlin/com/nuvio/android/LibraryTrackingUiTest.kt fun createRenameVisibilityAndDeleteUseSharedController
./androidApp/src/androidTest/kotlin/com/nuvio/android/LibraryTrackingUiTest.kt fun errorKeepsEditorAvailableWithoutExposingBackendText
./androidApp/src/androidTest/kotlin/com/nuvio/android/LibraryTrackingUiTest.kt fun pendingMutationDisablesEditorAndShowsTvStatusCopy
./androidApp/src/androidTest/kotlin/com/nuvio/android/LibraryTrackingUiTest.kt fun sameNamedMembershipDestinationsIdentifyProviderAndDispatchCorrectKey
./androidApp/src/androidTest/kotlin/com/nuvio/android/MdbListLiveTest.kt class MdbListLiveTest
./androidApp/src/androidTest/kotlin/com/nuvio/android/MdbListLiveTest.kt fun verifyRegisteredDeviceAccountAndLiveContracts
./androidApp/src/androidTest/kotlin/com/nuvio/android/MdbListSettingsUiTest.kt class MdbListSettingsUiTest
./androidApp/src/androidTest/kotlin/com/nuvio/android/MdbListSettingsUiTest.kt fun connectedAccountAndDisconnectCopyRenderInRealSettings
./androidApp/src/androidTest/kotlin/com/nuvio/android/MdbListSettingsUiTest.kt fun libraryAndWatchProgressSourcesCanBeSelectedIndependently
./androidApp/src/androidTest/kotlin/com/nuvio/android/MpvSurfaceTest.kt class MpvSurfaceTest
./androidApp/src/androidTest/kotlin/com/nuvio/android/SubtitlePlaybackTest.kt class SubtitlePlaybackTest
./androidApp/src/androidTest/kotlin/com/nuvio/android/SubtitleSettingsTest.kt class SubtitleSettingsTest
./androidApp/src/androidTest/kotlin/com/nuvio/android/SubtitleSettingsTest.kt fun importsPreviewsAndRemovesACustomFontThroughTheSystemPicker
androidMain/features/autosync/AutoSyncLibmpvCoordinator.android.kt fun LibmpvAutoSyncCoordinator
androidMain/features/downloads/DownloadsTransferJobService.kt fun onNetworkChanged
androidMain/features/downloads/DownloadsTransferJobService.kt fun onStartJob
androidMain/features/downloads/DownloadsTransferJobService.kt fun onStopJob
androidMain/features/player/AudioDelayMediaSource.kt fun onChildSourceInfoRefreshed
androidMain/features/player/AudioOutputKey.android.kt fun onAudioDevicesRemoved
androidMain/features/player/CustomDefaultTrackNameProvider.kt fun getChannelLayoutName
androidMain/features/player/ExternalPlayerActivityContract.kt fun createIntent
androidMain/features/player/GainAudioProcessor.kt fun queueInput
androidMain/features/player/MpvSurfaceView.android.kt fun onSurfaceTextureAvailable
androidMain/features/player/MpvSurfaceView.android.kt fun onSurfaceTextureDestroyed
androidMain/features/player/MpvSurfaceView.android.kt fun onSurfaceTextureSizeChanged
androidMain/features/player/MpvSurfaceView.android.kt fun onSurfaceTextureUpdated
androidMain/features/player/MpvSurfaceView.android.kt fun surfaceChanged
androidMain/features/player/MpvSurfaceView.android.kt fun surfaceCreated
androidMain/features/player/MpvSurfaceView.android.kt fun surfaceDestroyed
androidMain/features/player/PlayerEngine.android.kt fun onIsPlayingChanged
androidMain/features/player/PlayerEngine.android.kt fun onPlayWhenReadyChanged
androidMain/features/player/PlayerEngine.android.kt fun onPlaybackParametersChanged
androidMain/features/player/PlayerLibassExtensions.kt class LibassRenderType
androidMain/features/player/PlayerNowPlayingController.android.kt fun onFastForward
androidMain/features/player/PlayerNowPlayingController.android.kt fun onPause
androidMain/features/player/PlayerNowPlayingController.android.kt fun onRewind
androidMain/features/player/PlayerNowPlayingController.android.kt fun onSeekTo
androidMain/features/player/PlayerPlaybackNetworking.kt fun checkServerTrusted
androidMain/features/player/PlayerPlaybackNetworking.kt fun createDataSourceFactory
androidMain/features/player/PlayerPlaybackNetworking.kt fun getAcceptedIssuers
androidMain/features/player/VodCache.kt fun onCacheIgnored
androidMain/features/player/audiosync/AudioSubtitleSyncController.kt fun activeSessionKey
androidMain/features/player/volumeboost/VolumeBoostAndroid.kt fun libmpvVolumeForBoost
androidMain/features/player/volumeboost/VolumeBoostAndroid.kt fun onAudioSessionIdChanged
androidMain/features/player/volumeboost/VolumeBoostAndroid.kt object VolumeBoostPreferencesAndroid
commonMain/core/ui/Gradients.kt fun nuvioOverlayGradientBrush
commonMain/core/ui/NativeTabBridge.kt fun nativeTabSelect
commonMain/core/ui/NativeTabBridge.kt fun observeState
commonMain/core/ui/NativeTabBridge.kt fun requestManageProfiles
commonMain/core/ui/Tokens.kt val playerOverlay
commonMain/features/autosync/AutoSyncRetryAction.kt fun AutoSyncSubtitleModalSlot
commonMain/features/autosync/AutoSyncSampledReferenceAligner.kt fun findDelay
commonMain/features/autosync/AutoSyncSampledReferenceAligner.kt object AutoSyncSampledReferenceAligner
commonMain/features/autosync/AutoSyncTimelineRetime.kt fun buildDelayOnlyTimeline
commonMain/features/autosync/AutoSyncTimelineRetime.kt fun findDelayOnlyAlignment
commonMain/features/autosync/AutoSyncTimingDump.kt fun parseReport
commonMain/features/collection/CollectionModels.kt val heroBackdropUrl
commonMain/features/collection/CollectionModels.kt val heroVideoUrl
commonMain/features/collection/CollectionModels.kt val titleLogoUrl
commonMain/features/debrid/DebridApiModels.kt val streamable
commonMain/features/details/SeriesGraphApi.kt val tconst
commonMain/features/home/HomeCatalogSettingsRepository.kt val serverRevision
commonMain/features/player/PlayerScreenRuntimeState.kt var showReshapedLiveTvChannelsPanel
commonMain/features/player/PlayerSettingsRepository.kt fun setLibassRenderType
commonMain/features/player/SubtitleAudioModels.kt class SubtitleAudioUiState
commonMain/features/player/SubtitleAudioModels.kt val SubtitleBackgroundColorSwatches
commonMain/features/player/SubtitleAudioModels.kt val addonSubtitleError
commonMain/features/player/SubtitleSyncStatus.kt val speechHeardSec
commonMain/features/player/SubtitleSyncStatus.kt val switchRequests
commonMain/features/servers/silo/SiloPlayback.kt val retryable
commonMain/features/settings/SupportersContributorsPage.kt val monthLabel
commonMain/features/simkl/SimklSyncModels.kt val schemaVersion
commonMain/features/trakt/TraktEpisodeMappingService.kt fun logAndResetStats
commonMain/features/updater/AppUpdater.kt fun ignoreThisVersion
```
