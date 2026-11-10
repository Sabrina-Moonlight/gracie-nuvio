# NOTAS-ACTUALIZACION.md — Para la IA que haga la próxima actualización de Nuvio

Léelo junto con `claude.md` y `ambos.md`. Aquí está lo aprendido en la actualización 0.6.10 → 0.6.11-beta. Si algo de aquí ya no sirve, cámbialo y entrega este archivo en el ZIP.

## Método actual (no uses Fetch Original + Apply Gracie)
- `apply_gracie.sh` **se perdió** (se borró del repo). No lo busques ni lo reconstruyas.
- Se usa el workflow **Update Nuvio** (`.github/workflows/update-nuvio.yml`): compara 3 versiones (Nuvio viejo, Gracie actual, Nuvio nuevo) y mezcla archivo por archivo en una rama aparte `actualizacion-<version>`. `main` no cambia hasta que ella fusione.
- La versión base la guarda `BASE-NUVIO.txt` (raíz). Después de fusionar una actualización hay que dejarla con la versión nueva.
- Fetch Original y Apply Gracie quedan solo como respaldo. Fetch Original **pisa** los archivos de Gracie: no lo uses sobre `main`.

## Qué es de Gracie (el workflow lo respeta)
- Archivos solo suyos (burbuja AutoSync, ajuste de audio, barra pill, caché, volumen, iconos…): no se tocan.
- Archivos de Nuvio que ella modificó y Nuvio no: se quedan.
- Lo que ella **quitó a propósito** y no debe volver: iPhone (`iosApp/`, `composeApp/src/ios*`), `desktopMain`, `nativeInterop`, `scripts/`, `Docs/`, `assets/`, y los idiomas `values-xx` que no tiene (conserva `values` y `values-es`). El workflow ignora los archivos nuevos de Nuvio dentro de carpetas que Gracie ya no tiene.

## Seek Preview: ella NO lo quiere
- Ella ya lo había quitado, pero en el ZIP de `main` analizado seguía el código `showHorizontalSeekPreview` (el aviso que sale al arrastrar horizontalmente) en 6 archivos: `PlayerSurfaceGestures.kt`, `PlayerScreenRuntimeGestureActions.kt`, `PlayerScreenRuntimeUi.kt` y 3 tests (`PlayerSurfaceGesturesTest`, `PlayerGestureOverlayTest`, `NextEpisodeCardTest`). Eran idénticos a Nuvio 0.6.10.
- Casi seguro lo quitaba el `apply_gracie.sh` perdido. El resumen del workflow avisa si aparece. Si aparece: quítalo con el cambio mínimo y revisa que el gesto de arrastre siga funcionando y que los tests compilen. Si no sabes qué debe verse o no verse, pregúntale.

## Riesgos de compilación conocidos (sin choque de texto)
Nuvio 0.6.11 cambió estos archivos y código de Gracie los usa. Revisa firmas y llamadas:
- `AuthStorage` (+ `.android`) y `AuthRepository` ← `MainActivity.kt` y `HomeScreen.kt`.
- `HomeHeroSection` ← `HomeScreen.kt`.
- `AutoSyncPreferencesRepository` y `AutoSyncMpvCoordinator` ← varios archivos de AutoSync de Gracie.
- Nuvio añadió `AuthSessionErrors.kt`, `BackdropFocus*.kt` y `MdbListRemapCache.kt` (nuevos): pueden ser necesarios para que compilen los archivos de Nuvio que los usan.

## Choques reales en 0.6.10 → 0.6.11-beta (3 archivos, 4 choques)
Todos de AutoSync, y los dos primeros son imports. Los 9 archivos de AutoSync/strings que se mezclaron solos también hay que revisarlos.
- `AutoSyncPlaybackSettings.kt` (1)
- `AutoSyncMpvCoordinator.kt` (1)
- `AutoSyncPlayerCoordinator.android.kt` (2; Gracie lo cambió en unas 760 líneas, es el más grande)

## Reglas para arreglar choques
- Un grupo de 3 a 5 archivos por chat; primero los pequeños. Lee solo las partes que chocan (marcadas con `<<<<<<< GRACIE`, `||||||| NUVIO-vieja`, `>>>>>>> NUVIO-nueva`). Entrega ZIP con ruta completa que pise el archivo en la rama.
- No supongas el contenido de archivos que no tengas: pídelos.

## Truco de Sort Uploads
Si un ZIP trae **una sola carpeta en la raíz y esa carpeta no existe en el repo** (ej. `gracie-nuvio/`), Sort Uploads cree que es un envoltorio y la quita. Solución: pon también un archivo suelto en la raíz del ZIP (por ejemplo `ROUTES.txt`).
