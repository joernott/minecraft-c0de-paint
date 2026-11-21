```json
{
  "title": "CANVAS_FROM_WORLD",
  "icon": "minecraft:spyglass",
  "category": "rendering",
  "ordinal": 1
}
```

Eine neue Leinwand mit den Blöcken der Welt zwischen zwei Positionen, mit ihren Zuständen\; `from` wird zu 0,0,0. Luft wird zu nicht gesetzt, oder zu Luft, wenn `air` TRUE ist. Der Inhalt von Truhen und andere Blockdaten werden nicht kopiert. Ein Bereich, der größer als die Grenzen ist, löst CANVAS\_LIMIT aus, nicht geladene Chunks CHUNK\_NOT\_LOADED.

**Parameter und Ergebnisse**

- `from`, `to` STRUCT c0de/minecraft/POSITION: die Ecken des Bereichs
- `air` BOOLEAN: auch Luft kopieren (optional, Standard FALSE)
- Ergebnis `result` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand

**Beispiel**

```c0de
CALL c0de_paint/cpaint/CANVAS_FROM_WORLD
  PARAMETER from corner ;
  PARAMETER to other_corner ;
  RESULT result copy
```
