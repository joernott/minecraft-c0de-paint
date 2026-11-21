```json
{
  "title": "DRAW_LINE",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 1
}
```

Zeichnet eine gerade Linie zwischen zwei Punkten.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand (eine Variable)
- `from` STRUCT c0de\_paint/cpaint/POINT: ein Ende
- `to` STRUCT c0de\_paint/cpaint/POINT: das andere Ende
- `style` TYPE c0de\_paint/cpaint/STYLE: der Stil

**Beispiel**

```c0de
CALL c0de_paint/cpaint/DRAW_LINE
  PARAMETER canvas c ;
  PARAMETER from a ;
  PARAMETER to b ;
  PARAMETER style fence ;
```
