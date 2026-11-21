```json
{
  "title": "CANVAS",
  "icon": "c0de:type_library_block",
  "category": "types",
  "ordinal": 0
}
```

Eine Leinwand ist ein dreidimensionales Raster aus Voxeln ohne feste Größe\; sie wächst mit ihrem Inhalt. Jedes Voxel ist entweder nicht gesetzt oder enthält einen Block. Luft ist ein Block, ein Voxel kann also absichtlich auf Luft gesetzt werden: Beim Rendern entfernt es den Block an dieser Stelle.

Die Grenzen einer Leinwand sind der kleinste Quader, der alle gesetzten Voxel enthält. Koordinaten dürfen negativ sein, müssen aber innerhalb der Grenzen aus den Einstellungen liegen, sonst löst eine Zeichnung CANVAS\_LIMIT aus und zeichnet nichts. SAY und der Debugger zeigen eine Leinwand etwa als "CANVAS 20×10×8, 1600 voxels".

Eine neue Variable enthält eine leere Leinwand. Programme arbeiten mit Leinwänden nur über die Funktionen der Bibliothek. Eine Leinwand lässt sich nicht in JSON oder YAML umwandeln.

```c0de
VARIABLE house TYPE c0de_paint/cpaint/CANVAS
```
