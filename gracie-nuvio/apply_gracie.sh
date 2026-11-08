#!/usr/bin/env bash
# Aplica las modificaciones de Gracie's Nuvio sobre el Nuvio oficial 0.6.10 (build 153).
# Se corre desde la raíz del repo: bash gracie-nuvio/apply_gracie.sh
set -eu
cd "$(dirname "$0")/.."

# 1) Candado: solo vale para el original 0.6.10 build 153 (los archivos de files/ son de esa versión).
VF=iosApp/Configuration/Version.xcconfig
[ -f "$VF" ] || { echo "ERROR: falta $VF (no es el Nuvio oficial)."; exit 1; }
ver=$(grep '^MARKETING_VERSION' "$VF" | cut -d= -f2 | tr -d ' \r')
bld=$(grep '^CURRENT_PROJECT_VERSION' "$VF" | cut -d= -f2 | tr -d ' \r')
if [ "$ver" != "0.6.10" ] || [ "$bld" != "153" ]; then
  echo "ERROR: el original es $ver (build $bld) y estas modificaciones son para 0.6.10 (build 153)."
  echo "Lanza Fetch Original Nuvio con ref = 0.6.10 y vuelve a intentar."
  exit 1
fi

# 2) Copiar nuestros archivos (nuevos y modificados) encima del original.
cp -R gracie-nuvio/files/. ./
echo "Copiados $(find gracie-nuvio/files -type f | wc -l) archivos."

# 3) Nombre de la app en los 3 idiomas.
for f in composeApp/src/androidMain/res/values/strings.xml \
         composeApp/src/androidMain/res/values-es/strings.xml \
         composeApp/src/androidMain/res/values-bn/strings.xml; do
  [ -f "$f" ] || { echo "ERROR: falta $f"; exit 1; }
  sed -i "s|<string name=\"app_name\">[^<]*</string>|<string name=\"app_name\">Gracie\\\\'s Nuvio</string>|" "$f"
  grep -q "Gracie" "$f" || { echo "ERROR: no se pudo cambiar app_name en $f"; exit 1; }
done

# 4) Limpieza (no se toca iosApp/Configuration ni testFixtures).
rm -f README.md CONTRIBUTING.md store.json .gitmodules
rm -rf docs assets
rm -rf iosApp/DownloadsWidgetExtension iosApp/iosApp iosApp/iosApp.xcodeproj iosApp/iosAppTests
rm -rf composeApp/src/commonTest composeApp/src/androidHostTest composeApp/src/androidFullHostTest

# 5) Comprobaciones finales.
grep -q 'applicationId = "com.gracie.nuvio"' androidApp/build.gradle.kts || { echo "ERROR: applicationId no quedó en com.gracie.nuvio"; exit 1; }
[ -f composeApp/src/androidMain/kotlin/com/nuvio/app/features/player/audiosync/AudioSyncFallback.kt ] || { echo "ERROR: falta el audio sync"; exit 1; }
[ -f iosApp/Configuration/Version.xcconfig ] || { echo "ERROR: se perdió Version.xcconfig"; exit 1; }
echo "Listo: modificaciones aplicadas."
