```json
{
  "title": "DRAW_ARC",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 5
}
```

Zeichnet den Teil der Ellipse von DRAW\_ELLIPSE vom Winkel `start` bis `end`, in Grad, gezählt von `a` nach `b`. Die Linie ist der Bogen\; der Füllstil zeichnet ein Tortenstück.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand (eine Variable)
- `center`, `a`, `b` STRUCT c0de\_paint/cpaint/POINT: die Punkte der Ellipse
- `start`, `end` FLOAT: die Winkel
- `line_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: die Stile (optional)

**Beispiel**

```c0de
CALL c0de_paint/cpaint/DRAW_ARC
  PARAMETER canvas c ;
  PARAMETER center middle ;
  PARAMETER a east ;
  PARAMETER b up ;
  PARAMETER start 0 ;
  PARAMETER end 180 ;
  PARAMETER line_style arch ;
```
