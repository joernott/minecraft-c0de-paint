```json
{
  "title": "CANVAS_MERGE",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 6
}
```

Zeichnet `source`, eine Leinwand oder ein ARRAY von Leinwänden, um `offset` verschoben in `target`. Mit dem Modus "OVERWRITE" ersetzen gesetzte Voxel der Quelle die des Ziels\; mit "KEEP" füllen sie nur Voxel, die nicht gesetzt sind. Voxel, die in der Quelle nicht gesetzt sind, ändern das Ziel nie\; Luft schon.

**Parameter und Ergebnisse**

- `target` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand, die sich ändert (eine Variable)
- `source` TYPE c0de\_paint/cpaint/CANVAS oder ARRAY TYPE c0de\_paint/cpaint/CANVAS: was gezeichnet wird
- `mode` STRING: "OVERWRITE" oder "KEEP" (optional, Standard "OVERWRITE")
- `offset` STRUCT c0de\_paint/cpaint/POINT: die Verschiebung (optional, Standard 0,0,0)

**Beispiel**

```c0de
CALL c0de_paint/cpaint/CANVAS_MERGE
  PARAMETER target castle ;
  PARAMETER source tower ;
  PARAMETER offset corner ;
```
