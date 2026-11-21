```json
{
  "title": "DRAW_POLYGON",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 3
}
```

Draws a polygon through the points, closed automatically. The first three points that don't lie on one line define the plane\; points beside it are projected onto it.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas (a variable)
- `points` ARRAY STRUCT c0de\_paint/cpaint/POINT: the corners
- `line_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: the styles (optional)

**Example**

```c0de
CALL c0de_paint/cpaint/DRAW_POLYGON
  PARAMETER canvas c ;
  PARAMETER points gable ;
  PARAMETER fill_style planks ;
```
