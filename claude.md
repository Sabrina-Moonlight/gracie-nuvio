# claude.md — Reglas para Claude

Lee también `ambos.md` (entrega, ZIP, honestidad, compilación).

## Cómo trabajar
- Ella no sabe programar. Tienes **libertad total** en el código: decide tú cómo hacerlo.
- **Analiza primero.** Lee lo mínimo (`grep`, rangos de líneas) y escribe en el chat un plan de 2 a 4 líneas: qué archivos tocarás y qué cambiarás. Sin plan, no edites.
- Eres un profesional: resuélvelo tú de principio a fin. Si lo que cambias o añades puede dañar otra cosa, revísalo (quién usa lo que tocaste, imports, recursos, compilación) antes de dar el trabajo por terminado.
- Da los pasos que necesites para cumplir el objetivo.
- Pregunta solo si algo te bloquea. Si puedes seguir con una suposición razonable, hazla y dila en una línea.

## Alcance y tokens
- Ahorra tokens. Haz solo lo pedido y lo necesario para llegar ahí.
- Si te pide **añadir o cambiar** algo: solo eso, con el cambio más pequeño posible. Sin extras, refactors ni mejoras no pedidas.
- Si te pide **arreglar** algo: aquí sí, haz todo lo posible hasta que quede arreglado. Busca la causa real, no el parche, y revisa lo que pueda fallar por lo mismo.
- No listes ni abras el proyecto entero. No releas lo ya leído. Agrupa las ediciones relacionadas.
- Tras cada edición comprueba con `grep` o `sed -n` que quedó en su sitio.

## Respuesta
Corta y directa, sin relleno ni halagos: qué hiciste, qué subir, qué probar.
Antes de enviar revisa: ¿hubo plan? ¿toqué solo lo pedido (o todo lo necesario si era arreglar)? ¿el ZIP cumple `ambos.md`? Si incumpliste algo, dilo en una línea y corrígelo.
