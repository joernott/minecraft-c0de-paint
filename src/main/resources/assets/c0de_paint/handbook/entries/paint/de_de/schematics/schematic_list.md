```json
{
  "title": "SCHEMATIC_LIST",
  "icon": "c0de:call_block",
  "category": "schematics",
  "ordinal": 3
}
```

Die Schematics in einem Ordner von `schematics` und seinen Unterordnern, sortiert, als Namen, die die anderen Funktionen nehmen.

**Parameter und Ergebnisse**

- `folder` STRING: der Ordner (optional, Vorgabe der Ordner `schematics` selbst)
- Ergebnis `result` ARRAY STRING: die Namen

**Beispiel**

```c0de
CALL c0de_paint/schematics/SCHEMATIC_LIST
  PARAMETER folder "castles" ;
  RESULT result names
```
