```json
{
  "title": "SCHEMATIC_MISSING",
  "icon": "c0de:call_block",
  "category": "schematics",
  "ordinal": 2
}
```

Die Blöcke der Schematic, die dieser Server nicht hat, als HASH von Block-ID zu Anzahl\; leer, wenn die Schematic hier gebaut werden kann. Blöcke von Mods, die nicht installiert sind, etwa die Blöcke von Create auf einem Server ohne Create, und Blöcke neuerer Minecraft-Versionen erscheinen hier. Ruf die Funktion mit dem `replace` auf, das du verwenden willst, um es zu prüfen.

**Parameter und Ergebnisse**

- `file` STRING: der Name der Schematic
- `replace` HASH STRING: zu ersetzende Blöcke (optional)
- Ergebnis `result` HASH INTEGER: die fehlenden Blöcke

**Beispiel**

```c0de
CALL c0de_paint/schematics/SCHEMATIC_MISSING
  PARAMETER file "factory.nbt" ;
  PARAMETER replace swap ;
  RESULT result missing
```
