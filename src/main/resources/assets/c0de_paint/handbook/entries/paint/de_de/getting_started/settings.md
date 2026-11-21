```json
{
  "title": "Einstellungen",
  "icon": "minecraft:writable_book",
  "category": "getting_started",
  "ordinal": 2
}
```

Der Server hat die Datei `config/c0de_paint.properties`.

- `canvasMin` und `canvasMax` (Standard −128 und 127) begrenzen die Koordinaten einer Leinwand auf jeder Achse.
- `renderBlocksPerTick` (1 bis 1024, Standard 64) ist die Zahl der Blöcke, die RENDER\_CANVAS pro Tick setzt.
- `renderMiningSpeed` (0 bis 100, Standard 1) gibt an, wie viel schneller als ein Spieler RENDER\_CANVAS Blöcke mit Werkzeugen abbaut\; 0 baut sofort ab.
- `canvasVoxelsPerTick` (1024 bis 1048576, Standard 65536) ist die Zahl der Voxel, die eine Zeichenfunktion oder eine Transformation pro Tick bearbeitet. Größere Arbeit verteilt sich auf mehrere Ticks, während der Prozess wartet.
