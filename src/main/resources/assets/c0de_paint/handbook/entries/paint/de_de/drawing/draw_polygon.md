```json
{
  "title": "DRAW_POLYGON",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 3
}
```

Zeichnet ein Vieleck durch die Punkte, automatisch geschlossen. Die ersten drei Punkte, die nicht auf einer Geraden liegen, legen die Ebene fest\; Punkte daneben werden auf sie projiziert.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand (eine Variable)
- `points` ARRAY STRUCT c0de\_paint/cpaint/POINT: die Ecken
- `line_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: die Stile (optional)

**Beispiel**

```c0de
CALL c0de_paint/cpaint/DRAW_POLYGON
  PARAMETER canvas c ;
  PARAMETER points gable ;
  PARAMETER fill_style planks ;
```
