```json
{
  "title": "CANVAS_ITEMS",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 8
}
```

Die Gegenstände, die RENDER\_CANVAS zum Bauen der Leinwand nimmt, als HASH von Gegenstands-ID zu Anzahl: zwei Stufen für eine doppelte Stufe, drei Kerzen für einen Block mit drei Kerzen, ein Blumentopf und die Pflanze für eine eingetopfte Pflanze, ein Wassereimer für Wasser und für jeden geflutteten Block. Die oberen Hälften von Türen und Pflanzen, die Kopfteile von Betten und Kolbenköpfe brauchen nichts, weil der andere Teil sie setzt.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand
- Ergebnis `result` HASH INTEGER: die Gegenstände
- Ergebnis `without_item` HASH INTEGER: die Blöcke nach ID, die kein Gegenstand setzt, wie Feuer

**Beispiel**

```c0de
CALL c0de_paint/cpaint/CANVAS_ITEMS
  PARAMETER canvas c ;
  RESULT result shopping_list
```
