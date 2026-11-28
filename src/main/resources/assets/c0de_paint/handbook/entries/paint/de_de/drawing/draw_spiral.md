```json
{
  "title": "DRAW_SPIRAL",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 6
}
```

Zeichnet eine Spirale auf der Ellipse von DRAW\_ELLIPSE, die in Richtung `direction` steigt. Sie beginnt bei `a` und dreht sich in Richtung `b`. Jedes Voxel der Spirale liegt `elevation` Schichten höher als das vorige, abgerundet: 1 steigt eine Schicht pro Voxel, 0.5 bei jedem zweiten Voxel, 2 lässt eine Schicht Lücke. Die Spirale endet unter der Schicht `h`, wie ein Zylinder der Höhe `h`. Die Linie ist die Spirale\; der Füllstil zeichnet die Rampe zwischen der Achse durch `center` und der Spirale, wie die Stufen einer Wendeltreppe, mit der Achse in jeder Schicht. Nahe der Achse sind die Stufen einer Rampe nur ein Voxel breit, wie bei einer echten Wendeltreppe\; einen dickeren Kern zeichnest du als Zylinder in die Leinwand.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand (eine Variable)
- `center`, `a`, `b` STRUCT c0de\_paint/cpaint/POINT: die Punkte der Ellipse
- `direction` STRING: "+x", "-x", "+y", "-y", "+z" oder "-z"
- `elevation` FLOAT: die Schichten von einem Voxel der Spirale zum nächsten, mehr als 0 (optional, Standard 1)
- `h` INTEGER: die Höhe in Schichten
- `line_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: die Stile (optional)

**Beispiel**

```c0de
CALL c0de_paint/cpaint/DRAW_SPIRAL
  PARAMETER canvas c ;
  PARAMETER center middle ;
  PARAMETER a east ;
  PARAMETER b south ;
  PARAMETER direction "+y" ;
  PARAMETER elevation 0.25 ;
  PARAMETER h 20 ;
  PARAMETER line_style railing ;
  PARAMETER fill_style steps ;
```
