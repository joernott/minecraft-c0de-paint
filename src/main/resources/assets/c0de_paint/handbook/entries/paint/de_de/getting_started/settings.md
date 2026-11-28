```json
{
  "title": "Einstellungen",
  "icon": "minecraft:writable_book",
  "category": "getting_started",
  "ordinal": 2
}
```

Der Server hat die Datei `config/c0de_paint.properties`.

- `canvasMin` und `canvasMax` (Standard −128 und 255) begrenzen die Koordinaten einer Leinwand auf jeder Achse.
- `renderBlocksPerTick` (1 bis 1024, Standard 64) ist die Zahl der Blöcke, die RENDER\_CANVAS pro Tick setzt.
- `renderMiningSpeed` (0 bis 100, Standard 1) gibt an, wie viel schneller als ein Spieler RENDER\_CANVAS Blöcke mit Werkzeugen abbaut\; 0 baut sofort ab.
- `canvasVoxelsPerTick` (1024 bis 1048576, Standard 65536) ist die Zahl der Voxel, die eine Zeichenfunktion oder eine Transformation pro Tick bearbeitet. Größere Arbeit verteilt sich auf mehrere Ticks, während der Prozess wartet.
- `maxSchematicSize` (1024 bis 67108864, Standard 4194304) ist die größte Schematic-Datei in Bytes, die c0de\_paint/schematics liest.

Jeder Client hat die Datei `config/c0de_paint-client.properties` mit `allowSchematics` (Standard true): ob der Client die Schematics seines Spielers für c0de\_paint/schematics liest.

allowBlockChanges, das RENDER\_CANVAS ohne input braucht, ist eine Einstellung von c0de in `config/c0de.properties` auf dem Server.
