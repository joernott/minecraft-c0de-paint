```json
{
  "title": "CANVAS_GET",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 2
}
```

Der Block eines Voxels, mit dem Gewicht 1. Seine id ist "", wenn das Voxel nicht gesetzt ist, und "minecraft:air" für Luft.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand
- `point` STRUCT c0de\_paint/cpaint/POINT: das Voxel
- Ergebnis `result` STRUCT c0de\_paint/cpaint/BLOCK: der Block

**Beispiel**

```c0de
CALL c0de_paint/cpaint/CANVAS_GET
  PARAMETER canvas c ;
  PARAMETER point p ;
  RESULT result block
```
