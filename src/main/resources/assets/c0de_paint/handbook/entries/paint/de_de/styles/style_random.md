```json
{
  "title": "STYLE_RANDOM",
  "icon": "minecraft:magenta_dye",
  "category": "styles",
  "ordinal": 3
}
```

Für jedes Voxel ein zufällig gewählter Block. Die Chance eines Blocks ist sein Gewicht geteilt durch die Summe aller Gewichte. Die Zufallszahlen kommen vom Prozess, wie bei RANDOM\; ein Programm, das mit einem festen Startwert läuft, zeichnet also jedes Mal dasselbe.

**Parameter und Ergebnisse**

- `blocks` ARRAY STRUCT c0de\_paint/cpaint/BLOCK: die Blöcke mit ihren Gewichten
- Ergebnis `result` TYPE c0de\_paint/cpaint/STYLE: der Stil

**Beispiel**

```c0de
CALL c0de_paint/cpaint/STYLE_RANDOM
  PARAMETER blocks stones ;
  RESULT result rubble
```
