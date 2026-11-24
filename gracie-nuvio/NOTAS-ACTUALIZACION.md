# NOTAS-ACTUALIZACION.md — Para la IA que haga la próxima actualización de Nuvio

Léelo junto con `claude.md`. Aquí está lo aprendido en la actualización 0.6.10 → 0.6.11-beta. Si algo de aquí ya no sirve, cámbialo y entrega este archivo en el ZIP.

## Método (el único que se usa)
- Workflow **Update Nuvio** (`.github/workflows/update-nuvio.yml`): compara 3 versiones (Nuvio viejo, Gracie actual, Nuvio nuevo) y mezcla archivo por archivo en una rama aparte `actualizacion-<version>`. `main` no cambia hasta que ella fusione.
- Ella lo lanza en Actions → Update Nuvio. Solo escribe la versión nueva (etiqueta de Nuvio, ej. `0.6.12-beta`); la vieja se lee de `BASE-NUVIO.txt` (en la **raíz** del repo).
- `apply_gracie.sh`, Apply Gracie y Fetch Original ya no existen / no se usan. No reconstruyas `apply_gracie.sh`. Fetch Original pisaba los archivos de Gracie.
- Al fusionar una actualización, `BASE-NUVIO.txt` debe quedar con la versión nueva (entrégalo en un ZIP con `ROUTES.txt`).

## Qué es de Gracie (el workflow lo respeta)
- Archivos solo suyos (burbuja AutoSync, ajuste de audio, barra pill, caché, volumen, iconos…): no se tocan.
- Archivos de Nuvio que ella modificó y Nuvio no: se quedan.
- Lo que ella **quitó a propósito** y no debe volver: iPhone (`iosApp/`, `composeApp/src/ios*`), `desktopMain`, `nativeInterop`, `scripts/`, `Docs/`, `assets/`, y los idiomas `values-xx` que no tiene (conserva `values` y `values-es`).

## Cambios de comportamiento de Gracie que NO deben perderse
Después de cada actualización comprueba que siguen y que Nuvio no los devolvió:
- **Seek Preview quitado**: no debe existir `showHorizontalSeekPreview` (aviso con la hora al arrastrar horizontalmente). Archivos: `PlayerSurfaceGestures.kt`, `PlayerScreenRuntimeGestureActions.kt`, `PlayerScreenRuntimeUi.kt` y los tests `PlayerSurfaceGesturesTest`, `PlayerGestureOverlayTest`, `NextEpisodeCardTest`. El gesto de buscar (se aplica al soltar) sí debe seguir. Si reaparece, quítalo con el cambio mínimo y revisa que compile.
- **El progreso no se marca como visto al salirte cerca del final**: `isProgressComplete` (`WatchingPolicies.kt`) solo devuelve true si el video terminó (`isEnded`); `WatchProgressCompletionPercentThreshold` (`WatchProgressModels.kt`) vale `100f`, no `90f`. Tests ajustados: `WatchProgressRulesTest`, `InfusePlaybackTest`. Si Nuvio cambia esas reglas, vuelve a aplicarlo.

## Qué pasó en 0.6.10 → 0.6.11-beta
- Análisis: 92 archivos solo de Gracie, 77 de Nuvio modificados por ella, 15 solo cambiados por Nuvio, 12 por ambos, 389 quitados por Gracie a propósito.
- Choques de texto (3 archivos, 4 choques), todos de AutoSync: `AutoSyncPlaybackSettings.kt`, `AutoSyncMpvCoordinator.kt`, `AutoSyncPlayerCoordinator.android.kt`.

## Qué pasó en 0.6.11-beta → 1.1.0-beta.2
- Resumen del workflow: 16 choques de texto, resueltos en 4 entregas (idiomas/versión/biblioteca, debrid/streams, reproductor, PlayerEngine). Casi todos eran el mismo cambio que Nuvio y Gracie hicieron por separado (`forceRefresh`, `sourceFromLinkCache`): se queda la versión de Nuvio, que ya incluye lo de Gracie.
- **Fallo del workflow:** trajo `values-uk` y `values-zh` como «nuevos» aunque Gracie solo tiene `values` y `values-es`. Hay que borrarlos (DELETE.txt) y no dejar `uk`/`zh` en `locale_config.xml` ni en `AppLanguage.kt`.
- Textos nuevos de Nuvio: hay que traducirlos a `values-es` (si no, salen en inglés).
- `Version.xcconfig` (iosApp/Configuration) es de donde Android lee la versión: choca siempre; poner la de Nuvio nueva.
- Gracie quitó a propósito `settings_playback_threshold_minutes` y `player_engine_unavailable_rebuild`; no se usan, no devolverlos.
- Revisión sin compilar hecha: sin marcas de choque, imports y `Res.string`/`Res.drawable` definidos, `expect` con `actual`, miembros llamados en los objetos que cambió Nuvio, Seek Preview sigue quitado, progreso 100 %.

## Lo aprendido (revísalo en la próxima actualización)
- Los choques de texto no son todo. Busca también archivos SOLO de Gracie que llamen funciones cuya firma cambió Nuvio (caso: Nuvio quitó el argumento `preferredSubtitleLanguage` de `AutoSyncPlaybackSettingsRows`, y `NuvioReshapedSettingsPage.kt` lo seguía pasando), recursos `Res.string` nuevos y métodos nuevos en interfaces (Nuvio añadió `setSubtitleUriWithRestoredAutoSync`; está implementado en `AutoSyncPlayerEngineController.android.kt` y `AutoSyncMpvCoordinator.kt`).
- Gracie renombró `operationToken` a `libassOperationToken` y `retryOperationToken`. Todo código nuevo de Nuvio que use `operationToken` hay que adaptarlo en los archivos de AutoSync.
- Archivos de Nuvio que Gracie usa y cambiaron sin choque (revisa firmas y llamadas): `AuthStorage` (+ `.android`) y `AuthRepository` ← `MainActivity.kt` y `HomeScreen.kt`; `HomeHeroSection` ← `HomeScreen.kt`; `AutoSyncPreferencesRepository` y `AutoSyncMpvCoordinator` ← archivos de AutoSync. Nuvio añadió archivos nuevos que otros necesitan (`AuthSessionErrors.kt`, `BackdropFocus*.kt`, `MdbListRemapCache.kt`).
- Revisión antes de entregar un arreglo: sin marcas de choque, llaves y paréntesis balanceados, todos los `Res.string` usados definidos. Aquí no se compila: la prueba real es el release.

## Cómo trabajar sobre la rama (no main)
- **Sort Uploads solo corre solo en `main`.** Para la rama: sube el ZIP a `0-sort-uploads/` de la rama, y lanza a mano Actions → Sort Uploads → elige la rama. Luego se lanza solo el release de esa rama.
- Un ZIP con **una sola carpeta en la raíz que no existe en el repo** (ej. `gracie-nuvio/`) pierde esa carpeta (cree que es un envoltorio). Solución: un `ROUTES.txt` suelto en la raíz del ZIP.
- Un archivo nuevo en la raíz del repo también necesita `ROUTES.txt`.
- Choques: un grupo de 3 a 5 archivos por chat, primero los pequeños. Lee solo las partes marcadas (`<<<<<<< GRACIE`, `||||||| NUVIO-vieja`, `>>>>>>> NUVIO-nueva`). No supongas el contenido de archivos que no tengas: pídelos.
- Mientras dure la actualización no se suben otros cambios a `main`.
