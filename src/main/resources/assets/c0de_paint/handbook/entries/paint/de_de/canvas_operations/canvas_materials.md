```json
{
  "title": "CANVAS_MATERIALS",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 7
}
```

Die Blöcke einer Leinwand, als HASH von Block-id zu Anzahl, ohne Luft (CANVAS\_COUNT zählt Luft mit). Blöcke mit verschiedenen Zuständen zählen zusammen. CANVAS\_ITEMS zählt die Gegenstände, die RENDER\_CANVAS nimmt.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand
- Ergebnis `result` HASH INTEGER: die Anzahlen

**Beispiel**

```c0de
CALL c0de_paint/cpaint/CANVAS_MATERIALS
  PARAMETER canvas c ;
  RESULT result shopping_list
```
