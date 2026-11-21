```json
{
  "title": "CANVAS_MOVE",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 4
}
```

Verschiebt den Inhalt um `offset`.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand (eine Variable)
- `offset` STRUCT c0de\_paint/cpaint/POINT: wie weit, auf jeder Achse

**Beispiel**

```c0de
CALL c0de_paint/cpaint/CANVAS_MOVE
  PARAMETER canvas c ;
  PARAMETER offset up ;
```
