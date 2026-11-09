# ambos.md — Reglas para cualquier IA (Claude y Grok)

Vale para los tres proyectos: gracie-island, gracie-music y gracie-nuvio.
Además de este archivo, lee el de tu IA: `claude.md` o `grok.md`.
Si algo de aquí choca con tu archivo, manda tu archivo.

## Sobre ella
- No sabe programar. Habla **siempre en español**, con palabras sencillas y sin jerga.
- Ya conoce lo básico de GitHub (subir archivos, ejecutar workflows, fusionar pull requests). No se lo expliques. Si debe hacer algo nuevo a mano, dale los pasos exactos, uno por uno.
- Tú modificas el código y las aplicaciones. Tienes **permiso total** para leer, crear, modificar y borrar archivos del proyecto. No preguntes "¿puedo hacer X?": hazlo y explica después.
- Ella es la tester: sube, compila, prueba y te cuenta. No tienes acceso a su GitHub. Nunca digas "no puedo compilar ni probar": entrega y espera su resultado.
- Cada chat empieza sin memoria: estos archivos son la memoria. Si cambia una regla de trabajo, actualiza el archivo que toque y entrégalo en el ZIP.

## Las apps
Son tres apps personales suyas, cada una fork de otro proyecto. Todas usan Gradle y Compose (Nuvio además tiene iOS). Lo que vale en una no tiene por qué valer en otra: trabaja con el código del proyecto que tienes delante y no supongas carpetas, nombres ni librerías de otro fork.

## Honestidad
- No afirmes que algo funciona si no lo comprobaste. Di qué revisaste y cómo.
- Si no estás seguro de la causa, dilo y propón la forma más barata de confirmarla.
- No inventes archivos, funciones ni datos. Si no está en el código, dilo.

## Entrega
Entrega siempre los cambios en **un ZIP** que ella sube a `Drop-Files/`. El workflow **Sort Uploads** lo abre, coloca los archivos nuevos, **pisa** los que cambian (los iguales no se tocan), borra el ZIP y lanza solo el release.

**Nombre del ZIP** (para que salgan en orden en el explorador de archivos):

`gracie-APP-vNN-TIPO-descripcion.zip`

- `APP`: lo que va después de `gracie-` en el nombre del proyecto (`island`, `music`, `nuvio`).
- `vNN`: número de entrega con **dos dígitos** (`v01`, `v02`… `v10`). Así `v10` no queda antes que `v2`, ni en el explorador ni en Sort Uploads, que abre los ZIP en orden alfabético. Sigue desde el último número que ella te diga; si no lo sabes, empieza en `v01`.
- `TIPO`: `fix` (arreglo), `feat` (algo nuevo) o `chore` (mantenimiento, documentos, instrucciones).
- `descripcion`: 1 a 3 palabras en minúscula, con guiones, sin acentos, sin ñ y sin espacios.

Ejemplos: `gracie-music-v01-fix-letras.zip`, `gracie-island-v02-feat-modo-oscuro.zip`.
Si en un mismo chat haces varias entregas, un ZIP por entrega, numerados en orden. Los posteriores pisan a los anteriores.

**Dentro del ZIP:**
- Solo los archivos que cambiaste, con su nombre original y **su ruta completa desde la raíz del proyecto** (`app/src/main/...`). La raíz del ZIP es la raíz del proyecto, sin carpeta envoltorio. Con ruta completa no hace falta `ROUTES.txt`.
- Un archivo suelto (sin carpetas) solo se coloca solo si ya existe en la raíz del proyecto (`build.gradle.kts`). Si es nuevo en la raíz, o lo pones suelto aunque viva en otra carpeta, añade `ROUTES.txt`: una línea por archivo, `Nombre.ext = ruta/completa/Nombre.ext`.
- `DELETE.txt` si hay que borrar archivos: una ruta completa por línea. Solo archivos que existan (si no existe, Sort Uploads marca error). Nunca carpetas, ni `LICENSE`, ni `.git`, ni `.github`.
- **Nunca** dentro del ZIP: `.github/`, `*.keystore`, `*.jks`, `LICENSE`. Sort Uploads los ignora.

**Fuera del ZIP:** todo lo de `.github/` (workflows) va como archivo suelto, y le dices la ruta exacta para que lo edite a mano en GitHub.

**En el chat:** una línea por archivo con lo que cambió y una línea de qué probar. Nada más.

**Antes de entregar:** abre el ZIP (`unzip -l`) y confirma que el nombre sigue el formato, que las rutas son las correctas y que no hay nada de lo prohibido.

## Compilación
- Ella sube el ZIP, el bot lo ordena y lanza el release. No prepares ni compiles nada por tu cuenta, salvo que ella lo pida.
- Antes de dar algo por listo, lee el código y confirma que no rompe la compilación (sintaxis, imports, recursos que falten, usos de lo que tocaste).
- Si falla la compilación, pídele que copie el mensaje de la línea marcada en rojo y arregla en una entrega nueva.
