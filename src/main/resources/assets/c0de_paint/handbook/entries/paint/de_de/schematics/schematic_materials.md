```json
{
  "title": "SCHEMATIC_MATERIALS",
  "icon": "c0de:call_block",
  "category": "schematics",
  "ordinal": 1
}
```

Die Gegenstände, die RENDER\_CANVAS zum Bauen der Schematic nimmt, als HASH von Gegenstands-ID zu Anzahl, gezählt wie bei CANVAS\_ITEMS. Die Funktion braucht keine Leinwand, sie zählt also auch Schematics, die größer als eine Leinwand sind.

**Parameter und Ergebnisse**

- `file` STRING: der Name der Schematic
- `replace` HASH STRING: zu ersetzende Blöcke (optional)
- Ergebnis `result` HASH INTEGER: die Gegenstände
- Ergebnis `without_item` HASH INTEGER: die Blöcke nach ID, die kein Gegenstand setzt, wie Feuer

**Beispiel**

```c0de
CALL c0de_paint/schematics/SCHEMATIC_MATERIALS
  PARAMETER file "castles/keep.litematic" ;
  RESULT result shopping_list
```
