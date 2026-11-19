# claude.md — Reglas para Claude

## ⛔ Este archivo NO se toca
- **Nunca modifiques este archivo.** Ni para actualizarlo, ni para mejorarlo, ni para corregirlo, ni para añadir reglas.
- No lo incluyas en ningún ZIP. No crees copias ni versiones nuevas.
- Tampoco toques `grok.md`. Solo ella cambia estos archivos.
- Si crees que falta una regla, dilo en una línea en el chat y sigue trabajando.

## Cómo trabajar
- Ella no sabe programar. Tienes **libertad total** en el código: decide tú cómo hacerlo.
- **Analiza primero.** Lee lo mínimo (`grep`, rangos de líneas) y escribe en el chat un plan de 2 a 4 líneas: qué archivos tocarás y qué cambiarás. Sin plan, no edites.
- Eres un profesional: resuélvelo tú de principio a fin. Si lo que cambias o añades puede dañar otra cosa, revísalo (quién usa lo que tocaste, imports, recursos, compilación) antes de dar el trabajo por terminado.
- Pregunta solo si algo te bloquea. Si puedes seguir con una suposición razonable, hazla y dila en una línea.

## Más de una tarea: paso a paso, sin gastar todos los tokens
Ella tiene pocos tokens por conversación. Lo peor que puede pasar es que te quedes sin espacio sin darle un ZIP ni un archivo actualizado. Por eso:

1. **Identifica primero.** Si te manda más de una cosa, antes de tocar código escribe en el chat la lista de lo que vas a hacer, en orden. No edites nada hasta tener esa lista.
2. **Divide.** Si una tarea es larga, pártela en partes pequeñas. Cada parte debe poder entregarse sola y no romper nada.
3. **Una parte por entrega.** Haz una parte, entrégala en su ZIP y sigue con la siguiente. **Nunca intentes resolver todo a la vez.**
4. **Siempre progreso.** Cada respuesta con cambios termina con un ZIP. Nunca dejes el trabajo a medias sin ZIP. Si ves que se acaba el espacio, para y entrega lo que ya está listo.
5. **Sin que ella lo pida.** El ZIP y la lista de pendientes se entregan siempre, solos. Ella no tiene que pedírtelos.
6. **Poco a poco.** No hay prisa. Es mejor una parte bien hecha y entregada que cinco a medias.

### Lista de pendientes (to-do)
- Se entrega como **archivo suelto en el chat**, en cada entrega. **Nunca dentro del ZIP.**
- Nombre: `TODO.md` (si ella ya usa un archivo de plan, como `PLAN_GRACIE.md`, actualiza ese).
- Contenido, corto y fácil de pegar en un chat nuevo:
  - **Hecho:** lo ya entregado, con el nombre del ZIP.
  - **Pendiente:** lo que falta, en el orden en que lo harás.
  - **Para retomar:** archivos importantes que tocaste, suposiciones que hiciste y cualquier dato que un chat nuevo necesite para seguir sin releer todo.
- Si ella sube un archivo de plan o to-do, léelo primero y sigue su lista.

## Alcance y tokens
- Ahorra tokens. Haz solo lo pedido y lo necesario para llegar ahí.
- Si te pide **añadir o cambiar** algo: solo eso, con el cambio más pequeño posible. Sin extras, refactors ni mejoras no pedidas.
- Si te pide **arreglar** algo: aquí sí, haz todo lo posible hasta que quede arreglado. Busca la causa real, no el parche, y revisa lo que pueda fallar por lo mismo.
- No listes ni abras el proyecto entero. No releas lo ya leído. Agrupa las ediciones relacionadas.
- Tras cada edición comprueba con `grep` o `sed -n` que quedó en su sitio.
- Si el proyecto tiene código de otros (originales o forks), no copies y pegues si ella lo prohíbe en el plan. Úsalo de guía y escribe código propio, salvo que el plan diga que se puede copiar.

## Sobre ella
- No sabe programar. Habla **siempre en español**, con palabras sencillas y sin jerga.
- Ya conoce lo básico de GitHub (subir archivos, ejecutar workflows, fusionar pull requests). No se lo expliques. Si debe hacer algo nuevo a mano, dale los pasos exactos, uno por uno.
- Tú modificas el código y las aplicaciones. Tienes **permiso total** para leer, crear, modificar y borrar archivos del proyecto (menos estos archivos de reglas). No preguntes "¿puedo hacer X?": hazlo y explica después.
- Ella es la tester: sube, compila, prueba y te cuenta. No tienes acceso a su GitHub. Nunca digas "no puedo compilar ni probar": entrega y espera su resultado.
- Cada chat empieza sin memoria. Este archivo y el `TODO.md` son la memoria.

## El proyecto
Trabaja con el código del proyecto que tienes delante. No supongas carpetas, nombres, paquetes ni librerías: compruébalos en el código. Si algo no está, dilo.

## Honestidad
- No afirmes que algo funciona si no lo comprobaste. Di qué revisaste y cómo.
- Si no estás seguro de la causa, dilo y propón la forma más barata de confirmarla.
- No inventes archivos, funciones ni datos. Si no está en el código, dilo.

## Entrega
Entrega siempre los cambios en **un ZIP** que ella sube a `Drop-Files/`. El workflow **Sort Uploads** lo abre, coloca los archivos nuevos, **pisa** los que cambian (los iguales no se tocan), borra el ZIP y lanza solo el release.

**Nombre del ZIP** (para que salgan en orden en el explorador de archivos):

`gracie-APP-vNN-TIPO-descripcion.zip`

- `APP`: lo que va después de `gracie-` en el nombre del proyecto (por ejemplo, si el proyecto es `gracie-music`, `APP` es `music`).
- `vNN`: número de entrega con **dos dígitos** (`v01`, `v02`… `v10`). Así `v10` no queda antes que `v2`, ni en el explorador ni en Sort Uploads, que abre los ZIP en orden alfabético. Sigue desde el último número que ella te diga; si no lo sabes, empieza en `v01`.
- `TIPO`: `fix` (arreglo), `feat` (algo nuevo) o `chore` (mantenimiento, documentos, instrucciones).
- `descripcion`: 1 a 3 palabras en minúscula, con guiones, sin acentos, sin ñ y sin espacios.

Ejemplos: `gracie-music-v01-fix-letras.zip`, `gracie-music-v02-feat-modo-oscuro.zip`.
Si en un mismo chat haces varias entregas, un ZIP por entrega, numerados en orden. Los posteriores pisan a los anteriores.

**Dentro del ZIP:**
- Solo los archivos que cambiaste, con su nombre original y **su ruta completa desde la raíz del proyecto**. La raíz del ZIP es la raíz del proyecto, sin carpeta envoltorio. Con ruta completa no hace falta `ROUTES.txt`.
- Un archivo suelto (sin carpetas) solo se coloca solo si ya existe en la raíz del proyecto. Si es nuevo en la raíz, o lo pones suelto aunque viva en otra carpeta, añade `ROUTES.txt`: una línea por archivo, `Nombre.ext = ruta/completa/Nombre.ext`.
- `DELETE.txt` si hay que borrar archivos: una ruta completa por línea. Solo archivos que existan (si no existe, Sort Uploads marca error). Nunca carpetas, ni `LICENSE`, ni `.git`, ni `.github`.
- **Nunca** dentro del ZIP: `.github/`, `*.keystore`, `*.jks`, `LICENSE`, `claude.md`, `grok.md`, ni el `TODO.md`/plan. Sort Uploads ignora lo primero; lo demás va suelto.

**Fuera del ZIP (archivos sueltos en el chat):**
- Todo lo de `.github/` (workflows): dile la ruta exacta para que lo edite a mano en GitHub.
- El `TODO.md` o plan, en cada entrega.

**En el chat:** una línea por archivo con lo que cambió y una línea de qué probar. Nada más.

**Antes de entregar:** abre el ZIP (`unzip -l`) y confirma que el nombre sigue el formato, que las rutas son las correctas y que no hay nada de lo prohibido.

## Compilación
- Ella sube el ZIP, el bot lo ordena y lanza el release. No prepares ni compiles nada por tu cuenta, salvo que ella lo pida.
- Antes de dar algo por listo, lee el código y confirma que no rompe la compilación (sintaxis, imports, recursos que falten, usos de lo que tocaste).
- Si falla la compilación, pídele que copie el mensaje de la línea marcada en rojo y arregla en una entrega nueva.

## Respuesta
Corta y directa, sin relleno ni halagos: qué hiciste, qué subir, qué probar, y qué queda pendiente.

**Antes de enviar revisa:**
- ¿Hubo lista y plan antes de editar?
- ¿Hice solo una parte, pequeña, y no todo a la vez?
- ¿Toqué solo lo pedido (o todo lo necesario si era arreglar)?
- ¿El ZIP cumple la sección «Entrega» y tiene progreso?
- ¿Va el `TODO.md` suelto, actualizado, sin que ella lo pidiera?
- ¿No toqué `claude.md` ni `grok.md`?

Si incumpliste algo, dilo en una línea y corrígelo.
