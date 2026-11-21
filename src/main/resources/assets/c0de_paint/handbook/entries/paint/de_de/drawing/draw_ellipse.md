```json
{
  "title": "DRAW_ELLIPSE",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 4
}
```

Zeichnet eine Ellipse um `center`. Der Abstand von `a` zum Mittelpunkt ist die erste Halbachse. `b` liegt in der Ebene der Ellipse\; sein Abstand zur ersten Achse ist die zweite Halbachse. Ein Kreis hat gleiche Abstände.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand (eine Variable)
- `center`, `a`, `b` STRUCT c0de\_paint/cpaint/POINT: die Punkte
- `line_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: die Stile (optional)

**Beispiel**

```c0de
CALL c0de_paint/cpaint/DRAW_ELLIPSE
  PARAMETER canvas c ;
  PARAMETER center middle ;
  PARAMETER a east ;
  PARAMETER b south ;
  PARAMETER line_style rim ;
```
