```json
{
  "title": "CANVAS_MATERIALS",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 7
}
```

Die Blöcke, die eine Leinwand braucht, als HASH von Block-id zu Anzahl, ohne Luft. Blöcke mit verschiedenen Zuständen zählen zusammen.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand
- Ergebnis `result` HASH INTEGER: die Anzahlen

**Beispiel**

```c0de
CALL c0de_paint/cpaint/CANVAS_MATERIALS
  PARAMETER canvas c ;
  RESULT result shopping_list
```
