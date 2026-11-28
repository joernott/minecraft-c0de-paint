```json
{
  "title": "CANVAS_SCALE",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 9
}
```

Skaliert die Leinwand um `center` auf jeder Achse um einen Prozentsatz von 1 oder mehr. Jedes Voxel nimmt das nächste Voxel des Originals, Blöcke behalten also ihre Zustände.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand (eine Variable)
- `x`, `y`, `z` INTEGER: die Prozentsätze
- `center` STRUCT c0de\_paint/cpaint/POINT: der feste Punkt (optional, Standard 0,0,0)

**Beispiel**

```c0de
CALL c0de_paint/cpaint/CANVAS_SCALE
  PARAMETER canvas c ;
  PARAMETER x 200 ;
  PARAMETER y 200 ;
  PARAMETER z 200 ;
```
