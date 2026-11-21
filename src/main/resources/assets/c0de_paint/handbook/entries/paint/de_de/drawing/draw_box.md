```json
{
  "title": "DRAW_BOX",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 6
}
```

Zeichnet einen achsparallelen Quader zwischen zwei gegenüberliegenden Ecken. Die Linien sind seine 12 Kanten. Ein Würfel ist ein Quader mit gleichen Seiten.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand (eine Variable)
- `from`, `to` STRUCT c0de\_paint/cpaint/POINT: die Ecken
- `line_style`, `side_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: die Stile (optional)

**Beispiel**

```c0de
CALL c0de_paint/cpaint/DRAW_BOX
  PARAMETER canvas c ;
  PARAMETER from a ;
  PARAMETER to b ;
  PARAMETER line_style logs ;
  PARAMETER side_style planks ;
```
