```json
{
  "title": "CANVAS_REPLACE",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 5
}
```

Zeichnet jedes Voxel mit dem Block `id` neu mit `style`, egal welche Zustände der Block hat. Der neue Block behält die Zustände des alten, die er auch hat, so behalten Treppen ihre Richtung\; die Zustände des Stils haben Vorrang. Die Daten des alten Blocks bleiben, wenn der neue Block keine eigenen hat.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand (eine Variable)
- `id` STRING: der Block, etwa "stone"
- `style` TYPE c0de\_paint/cpaint/STYLE: der neue Stil

**Beispiel**

```c0de
CALL c0de_paint/cpaint/CANVAS_REPLACE
  PARAMETER canvas c ;
  PARAMETER id "cobblestone" ;
  PARAMETER style mossy ;
```
