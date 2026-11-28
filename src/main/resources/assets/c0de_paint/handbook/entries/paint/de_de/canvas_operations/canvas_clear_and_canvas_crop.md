```json
{
  "title": "CANVAS_CLEAR und CANVAS_CROP",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 3
}
```

CANVAS\_CLEAR setzt die Voxel im Quader von `from` bis `to` zurück, ohne `from` und `to` alle Voxel. CANVAS\_CROP setzt alle Voxel außerhalb des Quaders zurück.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand (eine Variable)
- `from`, `to` STRUCT c0de\_paint/cpaint/POINT: der Quader (bei CANVAS\_CLEAR optional)

**Beispiel**

```c0de
CALL c0de_paint/cpaint/CANVAS_CROP
  PARAMETER canvas c ;
  PARAMETER from a ;
  PARAMETER to b ;
```
