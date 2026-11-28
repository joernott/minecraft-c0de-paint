```json
{
  "title": "SCHEMATIC_LOAD",
  "icon": "c0de:call_block",
  "category": "schematics",
  "ordinal": 0
}
```

Die Schematic als Leinwand.

**Parameter und Ergebnisse**

- `file` STRING: der Name der Schematic
- `replace` HASH STRING: zu ersetzende Blöcke (optional)
- `region` STRING: nur diese Region einer `.litematic` (optional, Vorgabe alle)
- Ergebnis `result` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand
- Ergebnis `entities` INTEGER: die Zahl der weggelassenen Objekte

**Beispiel**

```c0de
VARIABLE swap HASH STRING
ASSIGN swap["create:andesite_casing"] "stripped_spruce_log" ;
CALL c0de_paint/schematics/SCHEMATIC_LOAD
  PARAMETER file "castles/keep.litematic" ;
  PARAMETER replace swap ;
  RESULT result keep
```
