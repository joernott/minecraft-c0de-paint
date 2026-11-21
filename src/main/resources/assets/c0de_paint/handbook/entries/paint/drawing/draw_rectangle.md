```json
{
  "title": "DRAW_RECTANGLE",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 2
}
```

Draws a rectangle. `a` and `b` are the ends of one side\; the other side runs perpendicular to it, up to the distance of `c` from the line through `a` and `b`. With a at 0,0,0, b at 9,0,0 and c at 0,0,4, it is a flat rectangle of 10 by 5 voxels.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas (a variable)
- `a`, `b`, `c` STRUCT c0de\_paint/cpaint/POINT: the points
- `line_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: the styles (optional)

**Example**

```c0de
CALL c0de_paint/cpaint/DRAW_RECTANGLE
  PARAMETER canvas c ;
  PARAMETER a a ;
  PARAMETER b b ;
  PARAMETER c c ;
  PARAMETER line_style frame ;
  PARAMETER fill_style glass ;
```
