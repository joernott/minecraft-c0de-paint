```json
{
  "title": "DRAW_PYRAMID",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 10
}
```

Zeichnet eine Pyramide mit quadratischer Grundfläche, ihre Seiten parallel zu den Achsen. Die halbe Seitenlänge ist `r`\; die Spitze liegt `h` Voxel von der Grundfläche entfernt in Richtung `direction`. Die Linien sind die vier Seiten der Grundfläche und die vier Kanten zur Spitze.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand (eine Variable)
- `center` STRUCT c0de\_paint/cpaint/POINT: der Mittelpunkt der Grundfläche
- `r`, `h` INTEGER: halbe Seite und Höhe
- `direction` STRING: die Richtung der Spitze
- `line_style`, `side_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: die Stile (optional)

**Beispiel**

```c0de
CALL c0de_paint/cpaint/DRAW_PYRAMID
  PARAMETER canvas c ;
  PARAMETER center base ;
  PARAMETER r 10 ;
  PARAMETER h 10 ;
  PARAMETER direction "+y" ;
  PARAMETER fill_style sandstone ;
```
