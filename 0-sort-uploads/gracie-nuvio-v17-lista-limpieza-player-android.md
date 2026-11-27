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
- [x] **P6** downloads, party, autosync → `gracie-nuvio-v08-chore-limpieza-autosync.zip` (+ v09, v10, v11, v12)
- [x] **P7** mdblist → sin cambios (nada sin uso; no hay ZIP)
- [x] **P8** settings → P8a y P8b hechas, sin cambios (no hay ZIP).
- [x] **P9** core: P9a → `gracie-nuvio-v14-chore-limpieza-core-red.zip`; P9b → `gracie-nuvio-v15-chore-limpieza-core-ui.zip`
- [x] **P10** player commonMain (parte 1: 50 archivos, `AddonSubtitleLoader.kt` → `PlayerScreenRuntimeAudioDelay.kt`, orden alfabético) → sin cambios (no hay ZIP)
- [x] **P11a** player commonMain parte 2 (62 archivos) → `gracie-nuvio-v16-chore-limpieza-player.zip`
- [x] **P11b** androidMain/player (78 archivos) → `gracie-nuvio-v17-fix-limpieza-player-android.zip`
- [ ] **P12** recursos sin uso (drawables, strings, mipmaps, `composeApp/libs`)
- [ ] **P13** tabla final (se queda para el FINAL del todo, después de P14-P18 si ella quiere)

## Tareas nuevas de ella (añadidas al final; hacerlas DESPUÉS de P13, una por entrega, "feat"/"fix")
Orden sugerido: de lo más fácil a lo más difícil. Ella pidió que van al final del plan.
- [ ] **P14** (feat) Botón "Eliminar todas las descargas" en la pantalla de Descargas. Hoy solo se borra una por una. Con confirmación ("¿seguro?") para no borrar por error. Pistas: `downloads/DownloadsScreen.kt`, `downloads/DownloadsRepository.kt` (hoy solo tiene `clearLocalState`, mirar cómo borra una descarga y repetirlo para todas; ojo con descargas en curso y con los subtítulos/portadas guardados).
- [x] **P15 HECHA en v13 (`gracie-nuvio-v13-fix-subtitulos-espanol.zip`, falta que ella la pruebe)** (fix) Subtítulos DESCARGADOS en español se ven raros (tildes/ñ mal) y a veces no pasa con los de streaming. Sospecha: la codificación del archivo al bajarlo/guardarlo. Pistas: `downloads/DownloadSubtitles.kt` (~línea 44 ya lee bytes y detecta la codificación) y `androidMain/.../DownloadSubtitleStorage.android.kt` (guarda en UTF-8). Comparar con cómo lo hace el streaming (`autosync/SubtitleCharsetDetector.kt`, con pista de idioma `languageHint`: ¿las descargas se la pasan?). Hay que encontrar la causa real, no parchear. Pedirle un ejemplo (título/addon/idioma) si no se reproduce leyendo.
- [ ] **P16** (fix) "Descargar temporada": los resultados de los addons no salen al pasar a cada capítulo; ella tiene que darle a reiniciar/volver a buscar. Debe avanzar solo al siguiente capítulo y hacer la búsqueda sola. Pistas: `StreamDestination.kt`, `DetailsDestinations.kt`, `details/MetaDetailsScreen.kt`, `details/components/EpisodeWatchedActionSheet.kt` (aparece "downloadSeason"). Buscar dónde se pasa al siguiente capítulo y por qué no se lanza la búsqueda de streams/addons (¿estado que no se reinicia?, ¿carga que se cancela?).
- [ ] **P17** (feat) Que "sync to the audio" se haga DE INMEDIATO, sin esperar a comparar subtítulos: ella solo quiere la sincronización por audio, primero y única. Pistas: `androidMain/.../player/audiosync/AudioSyncFallback.kt` (hoy espera a que AutoSync decida: `earlyKey`, `heldStatus`, `visible`, `pendingSwitch`) y `autosync/AutoSyncPlayerCoordinator.android.kt` (llama a `audioFallback` cuando AutoSync mantiene el timing original). Idea: al elegir subtítulo, arrancar el audio directo y apagar la comparación de subtítulos de AutoSync (así se podría borrar mucho código de `autosync/`, preguntarle si quiere eso después). Avisarle: el audio sync necesita unos segundos de audio para decidir, no es instantáneo del todo.
- [ ] **P18** (fix) La píldora de AutoSync y la sincronización de subtítulos no parecen funcionar con las DESCARGAS (los subtítulos descargados a veces traen problemas de timing). Pistas: `autosync/AutoSyncPlatform.android.kt`, `AutoSyncPlayerCoordinator.android.kt` (solo se activa con URL `http(s)`: ver `startsWith("https://")` en `AutoSyncMpvCoordinator.start`), y cómo reproduce el player un archivo local descargado (¿URL `file://`? entonces AutoSync se salta). Depende de P17: si el audio sync pasa a ser lo único, hacer P18 sobre ese camino. Hacer P17 antes de P18.

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

**v08 — `gracie-nuvio-v08-chore-limpieza-autosync.zip` (P6)**
Eliminado (0 usos, comprobado con `grep -w` en todo el proyecto):
- Archivo entero `autosync/AutoSyncSampledReferenceAligner.kt` (va en `DELETE.txt`)
- `AutoSyncLibmpvCoordinator.android.kt`: función `LibmpvAutoSyncCoordinator` (la clase y `rememberLibmpvAutoSyncCoordinator` se quedan)
- `AutoSyncTimelineRetime.kt`: `findDelayOnlyAlignment`, `buildDelayOnlyTimeline` y por cascada la constante `DELAY_ONLY_MIN_CUES`
- `AutoSyncTimingDump.kt`: `parseReport`, `decode` y `replay` (queda `encode`, que usa el log de depuración)
- `SubtitleCharsetDetector.kt`: `normalizeToUtf8`
`downloads` y `party`: nada seguro que borrar.

**v09 — `gracie-nuvio-v09-chore-quitar-boton-reintento.zip` (decisión de P6)**
- Eliminado `autosync/AutoSyncRetryAction.kt` (botón "reintentar con otra referencia" + `AutoSyncActivePlayer`; nunca se mostraba).
- Eliminados los 5 strings `autosync_retry_*` (values y values-es).

**v10 — `gracie-nuvio-v10-chore-quitar-logica-reintento.zip` (ella lo autorizó: "si falla un release se arregla después")**
- `AutoSyncPlayerContracts.kt`: quitados `AutoSyncRetryStatus`, `AutoSyncRetryUiState`, `mergeRejectedReferenceKeys` y los 2 miembros de reintento de la interfaz.
- `AutoSyncMpvCoordinator.kt` y `AutoSyncPlayerCoordinator.android.kt`: quitado todo el reintento (estado, `RetryContext`, `retryWithAnotherReference`, `invalidateRetryContext`, bloques en `runSync`/apply, imports).
- `AutoSyncPlayerEngineController.android.kt`: quitados los 2 overrides de reintento y el override de `setSubtitleDelayMs` (solo avisaba al reintento; ahora lo reenvía el `by base`). Si algo falla con el retraso manual, mirar aquí.
- `values/autosync_toast_strings.xml`: quitados `autosync_toast_retry_synced/none/failed`.

**v11 — `gracie-nuvio-v11-chore-limpieza-autosync-parametros.zip` (P6b)**
- `AutomaticSubtitleSync.kt`: quitados `excludedReferenceKeys`, `requiredReferenceSource`, `onReferenceSearchOutcome`, el enum `AutoSyncReferenceSearchOutcome`, `ReferenceSelection.hadEligibleBeforeExclusion` y todas sus ramas muertas (en `findTimelineRetime` y `awaitNearCompleteLiveReferences`). Con los valores que siempre se pasaban (vacío/null) el comportamiento es el mismo. Además quitado el import `awaitAll` (sin uso).
- Revisado: llaves cuadran (485/485 y 925/925), sin restos de los nombres quitados en todo el proyecto, tests no los usaban.
- **P6c HECHO en v12** (ver abajo). Texto original:
- ~~Pendiente (P6c, opcional):~~ `AutoSyncResolvedTimeline.reference` (clase `AutoSyncReferenceIdentity`, función `equivalentReferenceKeysFor`, campos `equivalentKeys`, y quizá `AutoSyncReferenceTimingFingerprint`) ya no lo lee nadie; solo se escribe en `AutomaticSubtitleSync.kt` ~1412-1430. Ojo: `referenceTimingFingerprint(...)` también se usa para agrupar (~2069), esa parte NO se borra.

**v12 — `gracie-nuvio-v12-chore-limpieza-referencia.zip` (P6c)**
- `AutomaticSubtitleSync.kt`: quitados el campo `AutoSyncResolvedTimeline.reference`, la clase `AutoSyncReferenceIdentity`, `equivalentReferenceKeysFor`, el enum `AutoSyncReferenceSource` y la variable `referenceSource` (+ sus 2 asignaciones). Se queda `AutoSyncReferenceTimingFingerprint` y `referenceTimingFingerprint` (agrupan referencias).
- Único cambio de comportamiento: antes, si había un subtítulo ganador pero la lista de referencias quedaba vacía tras filtrar atípicas (~línea 672), se devolvía `null` por una comprobación que solo servía para rellenar `reference`. Ahora se devuelve el ganador. Caso rarísimo; si AutoSync se comporta raro, mirar `findTimelineRetime` cerca de `return@supervisorScope AutoSyncResolvedTimeline(`.
- Revisado: 0 restos de los nombres quitados en todo el proyecto, llaves 619/619 y paréntesis 958/958, nadie más construye `AutoSyncResolvedTimeline`.

**P7 (mdblist), sin ZIP:** 0 candidatos. Revisé a mano 20 nombres sospechosos (los que aparecen solo 2 veces en el proyecto): todos tienen un uso real. No hay `!!`, TODO ni `catch` vacíos.

**v13 — `gracie-nuvio-v13-fix-subtitulos-espanol.zip` (P15, hecho en otro chat; solo registrado aquí)**
- Toca `autosync/SubtitleCharsetDetector.kt` (commonMain) y en tests `LegacySubtitleCharsetDetector.kt` + `SubtitleCharsetDetectorTest.kt`. Ojo: `gracie-nuvio-main.zip` NO lo trae (se subió aparte).

**v17 — `gracie-nuvio-v17-fix-limpieza-player-android.zip` (P11b)**
- **BUG arreglado:** `VolumeBoostPreferencesAndroid.initialize` nadie la llamaba, así que `VolumeBoost.isAvailable` era siempre `false` en Android: la barra de volumen no pasaba de 100% y el ajuste de aumento de volumen no aparecía. Ahora `MainActivity.kt` la llama (junto a las otras 2 de `AutoSync`/`Pill nav`). **Cambio visible:** ahora SÍ sale el aumento de volumen hasta 200% (está activado por defecto). Si no lo quiere, quitar esa línea.
- Eliminado (0 usos): `libmpvVolumeForBoost` y `LIBMPV_VOLUME_MAX` (`VolumeBoostAndroid.kt`, + import `pow`), `activeSessionKey` (`AudioSubtitleSyncController.kt`), archivo `PlayerLibassExtensions.kt` (enum `LibassRenderType`, los ajustes se guardan como texto).
- Revisado: llaves/paréntesis cuadran. No se pudo compilar. Los 6 `!!` de androidMain/player (`AsrSyncEngine:272`, `AudioSyncTracker:196`, `AudioSyncExtractors:152`, `AudioSubtitleSyncController:413,782`, `CustomDefaultTrackNameProvider:26`) no tocados (se comprueba antes).

**v16 — `gracie-nuvio-v16-chore-limpieza-player.zip` (P11a)**
Eliminado (0 usos, `grep -w` en todo el proyecto):
- `PlayerScreenRuntimeState.kt`: `showReshapedLiveTvChannelsPanel`
- `PlayerSettingsRepository.kt`: `setLibassRenderType` (el guardado sigue: lo usa la sincronización de ajustes en `PlayerSettingsStorage.android.kt:1774`)
- `SubtitleAudioModels.kt`: `SubtitleBackgroundColorSwatches` y la clase `SubtitleAudioUiState`
- `SubtitleSyncStatus.kt`: campo `speechHeardSec` (la única construcción usa argumentos con nombre)
- Revisado: llaves y paréntesis cuadran, sin imports huérfanos. No se pudo compilar aquí. **v15 y v16 se pueden subir juntas** (no tocan los mismos archivos).
- Dudoso/posible BUG (no tocado, ligado a P17): `SubtitleSyncStatus.switchRequests` nadie la lee. `AudioSubtitleSyncController.kt:737` llama `requestSwitch(winner.key)` (cambiar solo a un subtítulo mejor), pero el player no escucha, así que ese cambio automático no ocurre. Decidir en P17.

**P10 (player commonMain, parte 1), sin ZIP:** 0 candidatos. Revisado con script (nombres declarados que aparecen 1 sola vez en todo el proyecto, y los que solo usa un test), más búsqueda de `!!`, TODO, `catch` vacíos y privados sin uso: nada en los 50 archivos. Único hallazgo: `InfusePlayback.kt` parece solo de iOS, pero SÍ se usa en producción (`MainAppContent.kt:693`, `AppUrlBridge.kt:51`) → no se toca. `PlayerChapters.mpvChapters` solo la usa su test (`MpvChaptersTest`); `PlayerEngine.android.kt:1963` repite esa misma lógica a mano. Quedó como dudoso/mejorable (no tocado).

**P8a (settings, mitad 1), sin ZIP:** 0 candidatos. Revisado con `grep -w` (todo el proyecto y solo código de producción): ninguna función/clase/propiedad/miembro sin uso ni usada solo por tests. Los `!!` de `PlaybackP2pSettings.kt:97`, `PlaybackSkipSettings.kt:217` y `MetaScreenSettingsPage.kt:252` son seguros (se comprueba antes; estado de Compose). Menor: `AppLanguage.isRightToLeft()` siempre devuelve `false` (no tocado).

**P8b (settings, mitad 2: `PlaybackVideoSettings.kt` → final, 37 archivos), sin ZIP:** 0 candidatos seguros. Único nombre con 1 uso: `SupportersContributorsPage.kt` `monthLabel` = campo de una clase `@Serializable` (dato de red), NO tocar. Sin `!!`, TODO ni `catch` vacíos en esta mitad.

**v14 — `gracie-nuvio-v14-chore-limpieza-core-red.zip` (P9a)**
- `core/network/NetworkStatusRepository.kt`: eliminada la propiedad `NetworkStatusUiState.isOnline` (0 usos en todo el proyecto, `grep -w`). `isOfflineLike` y `condition` se quedan.
- Resto de la primera mitad de `core`: 0 candidatos más, sin `!!`, TODO ni `catch` vacíos.

**v15 — `gracie-nuvio-v15-chore-limpieza-core-ui.zip` (P9b)**
- `core/ui/NavigationBar.kt`: eliminados `NuvioNavigationBar` (barra flotante vieja, la nueva vive en `FloatingNavigationBar.kt`), `NuvioNavigationBarScopeImpl` y `NavItemLabel` (solo los usaba ella) + 14 imports. Se quedan `NuvioNavBarScrollState`, `NuvioNavigationBarScope` y `NuvioClassicNavigationBar`.
- `core/ui/NativeTabBridge.kt`: eliminados `NativeProfileSwitcherController`, `NativeProfileOption`, `NativeProfileSwitcherState` y `nativeTabSelect` (resto de iOS; Android llama `App()` sin argumentos, así que el controlador siempre era `null`) + 16 imports.
- `App.kt` y `AppGate.kt`: quitado el parámetro `nativeProfileSwitcherController` y los 2 `LaunchedEffect` que lo escuchaban (con `null` no hacían nada).
- `core/ui/Gradients.kt`: eliminado `nuvioOverlayGradientBrush`. `core/ui/Tokens.kt`: eliminados `Space.s7` y `Z.playerOverlay`.
- Revisado: `grep -w` en todo el proyecto sin restos; llaves y paréntesis cuadran; imports sin uso limpiados. No se pudo compilar aquí. Si falla la compilación, mirar `AppGate.kt` y `App.kt`.
- Los archivos `baseline-prof.txt` y `startup-prof.txt` (androidApp) aún nombran lo borrado: son perfiles generados, no rompen nada.

## Hallazgos acumulados
| Parte | Bugs | Código sin uso | Código mejorable | Dudoso (no tocado) |
|---|---|---|---|---|
| P11b | **Sí: aumento de volumen nunca se activaba en Android (arreglado en v17)** | 4 (2 constantes/funciones, 1 función, 1 archivo/enum) | 6 `!!` revisados, seguros hoy (ver v17) | — |
| P11a | Posible: el cambio automático de subtítulo por audio (`requestSwitch`) no lo escucha nadie (ver v16) | 6 (1 propiedad de estado, 1 setter, 1 lista de colores, 1 clase, 1 campo, 1 campo de clase) | `PlayerScreenRuntimePlaybackActions.kt:159` usa `!!` (seguro, se comprueba antes) | `SubtitleSyncStatus.switchRequests` |
| P10 | ninguno | ninguno | `PlayerEngine.android.kt:1963` copia la lógica de `mpvChapters` (podría llamarla) | Solo test: `mpvChapters`. Infuse: se usa, no tocar. |
| P9b | ninguno | ~330 líneas: barra flotante vieja, controlador nativo de perfiles (iOS), `nativeTabSelect`, 1 degradado, 2 tokens | `ShelfComponents.kt:177` usa `!!` (seguro: `hasFallback` lo comprueba antes). `Alignment` sin uso en `AppGate.kt` (ya estaba, no tocado). | `NativeTabBridge.requestTab` y `requestedTabs` (leído en `MainAppContent.kt:441`): ahora nadie emite, solo quedan por inercia de iOS. Quitarlos toca `MainAppContent.kt`: decidir. |
| P9a | ninguno | 1 propiedad (`isOnline`) | — | — |
| P8b | ninguno | ninguno | — | `SupportersContributorsPage.monthLabel` (campo de red, no tocar) |
| P8a | ninguno | ninguno | `AppLanguage.isRightToLeft()` siempre `false` | — |
| P7 | ninguno | ninguno | ninguno | Solo tests: `supportsReordering/reorderLists` (ya anotado en P2). |
| P6 | ninguno | 8 funciones/constantes + 1 archivo | `!!` revisados y seguros hoy (se comprueba antes): `DownloadsRepository.kt:149`, `AutomaticSubtitleSync.kt:2673`, `Mp4SubtitleSampleTables.kt:318`, `AutoSyncBubbleGlassAndroid.kt:101`. `SubtitleCharsetDetector.kt:138` tiene `catch (_: Exception) {}` vacío (inofensivo). | Botón y lógica de reintento ya eliminados (v09, v10). Solo test: `PartyClock.isSynced`. `TraktEpisodeMappingService.logAndResetStats` sigue sin llamarse (de P2). |
| P5 | ninguno | 13 funciones/propiedades | `ConnectionSpeed.kt:228-229` y `PlexProvider.kt:397` usan `!!` (seguros: se comprueba antes). | Solo tests: `isServerSource`, `hasPlayableSource`, `DebridSettings.torboxApiKey/realDebridApiKey/hasAnyApiKey`. `SiloPlayback.retryable` nadie lo lee (dato de respuesta, no tocado). `DebridApiModels.streamable` es dato de red. |
| P4 | ninguno | 3 funciones | `MetaDetailsScreen.kt` líneas ~1303 y ~1656: el mismo bloque "cargar más comentarios" está copiado dos veces y ambos tragan cualquier error con `catch (_: Exception) { }` (también la cancelación; inofensivo hoy). `EpisodeRatingsAlignment.kt:61-62` usa `!!` (seguro, filtrado antes). | `SeriesGraphApi.tconst` es campo de datos de la red, no tocar. |
| P3 | 3 lecturas dobles con `!!` en `ProfileRepository` (posible cierre por condición de carrera): arregladas | 17 funciones/clases | `PosterGrid.kt:147`, `ProfileEditScreen.kt:368`, `CollectionEditorRepository.kt:629,686` usan `!!` (seguros hoy) | Solo tests: `LibraryLocalState.isContentCurrent` y `runIfContentCurrent`. Campos guardados en disco, NO tocar: `focusGifEnabled`, `heroBackdropUrl`, `heroVideoUrl`, `titleLogoUrl`. `serverRevision` sí se usa (compara si cambió el servidor). |
| P2 | ninguno | 16 funciones + 1 archivo con su test | `TrackingProvider`: los mapas `listWriters` e `historyWriters` ahora solo se escriben y nadie los lee (los escritores se usan directo). `TraktEpisodeMappingService`: los contadores de estadísticas se calculan pero `logAndResetStats` nadie la llama. `ResumePromptRepository.kt:56` usa `!!` (seguro). | Solo usados por tests (no se tocan): `traktBestLandscapeUrl`, `parseTraktListId`, `ContinueWatchingEnrichmentCache.getNextUpSnapshot`, `WatchProgressCodec.encodeEntries/decodeEntries`, `byProgressKey/byVideoId`, `supportsReordering/reorderLists` (interfaz de MDBList). `SimklSyncModels.schemaVersion` es un campo guardado en disco, no tocar. |
| P1c | ninguno | 2 funciones solo-test + 1 módulo vacío (WasmBridge) | `ShuffleEpisodeState.kt:29` y `PluginsSettingsScreen.kt:508-510` usan `!!` (seguros hoy, se podrían cambiar por `?.let`) | — |
| P1b | ninguno | 2 funciones + 1 test de una función muerta | `CatalogScreen.kt:334` usa `!!` (hoy es seguro por `hasFallback`; se podría cambiar por `?.let`) | — |
| P1a | ninguno encontrado | los 12 de arriba | — | `AppUpdater.ignoreThisVersion`: nadie la llama, así que "ignorar versión" nunca se activa aunque `getIgnoredTag/setIgnoredTag` existan en 3 sets. Decidir: añadir botón o quitar todo. |

## Para retomar (chat nuevo)
- Proyecto: `gracie-nuvio-main.zip` → `gracie-nuvio-main/`. `APP` = `nuvio`. Siguiente ZIP: `gracie-nuvio-v18-...` (siguiente: **P12** recursos sin uso). **v15, v16 y v17 aún NO compiladas por ella** (el proyecto base debe llevar v15 y v16 encima de `main`; v13 tampoco está en `main`); si falla, mirar `AppGate.kt` y `App.kt`. Las tareas nuevas P14-P18 van al final, tras P13, salvo que ella diga otra cosa..
- Código en `composeApp/src/` (sets: commonMain, androidMain, fullCommonMain, androidFull, androidPlaystore, tests). No hay iosMain.
- Suposición: no se puede compilar aquí; verificación por lectura y `grep`.
- **P12: revisar también los strings que usaban los globos de perfil borrados (`ProfileSwitcherTab`).**
- **Recursos que quedaron sin uso por P1a (revisar en P12):** `media_movie`, `player_picture_in_picture`, `external_player_unavailable` (values y values-es). Confirmar con `grep` antes de borrar.
- **Método rápido usado:** script que busca declaraciones (`fun/class/object/val`) cuyo nombre aparece 1 sola vez en todo el proyecto. Lo que sigue son los candidatos restantes (actualizado tras P6: 70) (formato `ruta tipo nombre`; ruta relativa a `composeApp/src/`, sin `kotlin/com/nuvio/app/`). Todos están SIN verificar: revisar cada uno con `grep -w` y leer el contexto antes de borrar (algunos pueden ser iOS/librería/API pública).
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
