```json
{
  "title": "CANVAS_COUNT",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 0
}
```

Die Zahl der gesetzten Voxel.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand
- Ergebnis `result` INTEGER: die Zahl

**Beispiel**

```c0de
CALL c0de_paint/cpaint/CANVAS_COUNT
  PARAMETER canvas c ;
  RESULT result voxels
```
