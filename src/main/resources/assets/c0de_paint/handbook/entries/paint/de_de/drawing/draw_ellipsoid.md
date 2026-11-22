```json
{
  "title": "DRAW_ELLIPSOID",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 8
}
```

Zeichnet ein achsparalleles Ellipsoid mit den Radien `rx`, `ry` und `rz`\; eine Kugel, wenn sie gleich sind. Ein Radius von 0 macht es auf dieser Achse flach.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand (eine Variable)
- `center` STRUCT c0de\_paint/cpaint/POINT: der Mittelpunkt
- `rx`, `ry`, `rz` INTEGER: die Radien
- `side_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: die Stile (optional)

**Beispiel**

```c0de
CALL c0de_paint/cpaint/DRAW_ELLIPSOID
  PARAMETER canvas c ;
  PARAMETER center middle ;
  PARAMETER rx 8 ;
  PARAMETER ry 8 ;
  PARAMETER rz 8 ;
  PARAMETER side_style glass ;
```
