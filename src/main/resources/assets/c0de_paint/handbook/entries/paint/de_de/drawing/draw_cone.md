```json
{
  "title": "DRAW_CONE",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 9
}
```

Zeichnet einen Kegel mit der Grundfläche von DRAW\_CYLINDER\; seine Spitze liegt `h` Voxel von der Grundfläche entfernt in Richtung `direction`. Die Linie ist der Rand der Grundfläche.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand (eine Variable)
- `center` STRUCT c0de\_paint/cpaint/POINT: der Mittelpunkt der Grundfläche
- `r`, `h` INTEGER: Radius und Höhe
- `direction` STRING: die Richtung der Spitze
- `line_style`, `side_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: die Stile (optional)

**Beispiel**

```c0de
CALL c0de_paint/cpaint/DRAW_CONE
  PARAMETER canvas c ;
  PARAMETER center roof ;
  PARAMETER r 5 ;
  PARAMETER h 6 ;
  PARAMETER direction "+y" ;
  PARAMETER fill_style shingles ;
```
