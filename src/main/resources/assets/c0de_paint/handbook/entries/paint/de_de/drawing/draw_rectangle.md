```json
{
  "title": "DRAW_RECTANGLE",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 2
}
```

Zeichnet ein Rechteck. `a` und `b` sind die Enden einer Seite\; die andere Seite verläuft senkrecht dazu, bis zum Abstand von `c` zur Geraden durch `a` und `b`. Mit a bei 0,0,0, b bei 9,0,0 und c bei 0,0,4 ist es ein flaches Rechteck von 10 mal 5 Voxeln.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand (eine Variable)
- `a`, `b`, `c` STRUCT c0de\_paint/cpaint/POINT: die Punkte
- `line_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: die Stile (optional)

**Beispiel**

```c0de
CALL c0de_paint/cpaint/DRAW_RECTANGLE
  PARAMETER canvas c ;
  PARAMETER a a ;
  PARAMETER b b ;
  PARAMETER c c ;
  PARAMETER line_style frame ;
  PARAMETER fill_style glass ;
```
