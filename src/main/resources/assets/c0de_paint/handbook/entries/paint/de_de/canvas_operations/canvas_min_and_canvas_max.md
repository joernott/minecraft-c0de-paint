```json
{
  "title": "CANVAS_MIN und CANVAS_MAX",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 1
}
```

Die Ecken der Grenzen. Eine leere Leinwand löst CANVAS\_EMPTY aus.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand
- Ergebnis `result` STRUCT c0de\_paint/cpaint/POINT: die unterste Ecke (CANVAS\_MIN) oder die oberste Ecke (CANVAS\_MAX)

**Beispiel**

```c0de
CALL c0de_paint/cpaint/CANVAS_MAX
  PARAMETER canvas c ;
  RESULT result top
```
