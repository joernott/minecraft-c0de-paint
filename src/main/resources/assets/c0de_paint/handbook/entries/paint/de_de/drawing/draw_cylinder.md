```json
{
  "title": "DRAW_CYLINDER",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 9
}
```

Zeichnet einen Zylinder. Der Grundkreis hat seinen Mittelpunkt bei `center` und den Radius `r`\; der Zylinder wächst um `h` Voxel in Richtung `direction`. Die Linien sind die Ränder beider Enden.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand (eine Variable)
- `center` STRUCT c0de\_paint/cpaint/POINT: der Mittelpunkt der Grundfläche
- `r`, `h` INTEGER: Radius und Höhe
- `direction` STRING: "+x", "-x", "+y", "-y", "+z" oder "-z"
- `line_style`, `side_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: die Stile (optional)

**Beispiel**

```c0de
CALL c0de_paint/cpaint/DRAW_CYLINDER
  PARAMETER canvas c ;
  PARAMETER center base ;
  PARAMETER r 4 ;
  PARAMETER h 12 ;
  PARAMETER direction "+y" ;
  PARAMETER side_style stone ;
```
