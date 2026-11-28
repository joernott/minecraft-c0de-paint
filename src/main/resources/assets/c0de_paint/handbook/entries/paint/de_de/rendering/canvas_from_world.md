```json
{
  "title": "CANVAS_FROM_WORLD",
  "icon": "minecraft:spyglass",
  "category": "rendering",
  "ordinal": 1
}
```

Eine neue Leinwand mit den Blöcken der Welt zwischen zwei Positionen, mit ihren Zuständen und ihren Daten (dem Inhalt von Truhen, dem Text von Schildern)\; `from` wird zu 0,0,0. Luft wird zu nicht gesetzt, oder zu Luft, wenn `air` TRUE ist. Jede Ecke muss von `from` aus gesehen in den Grenzen einer Leinwand liegen: mit den Standardeinstellungen bis zu 127 Blöcke in positiver und 128 in negativer Richtung jeder Achse\; sonst CANVAS\_LIMIT. Nicht geladene Chunks lösen CHUNK\_NOT\_LOADED aus.

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
