```json
{
  "title": "DRAW_ARC",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 5
}
```

Draws the part of the ellipse of DRAW\_ELLIPSE from the angle `start` to `end`, in degrees, counted from `a` towards `b`. The line is the arc\; the fill style draws a pie slice.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas (a variable)
- `center`, `a`, `b` STRUCT c0de\_paint/cpaint/POINT: the points of the ellipse
- `start`, `end` FLOAT: the angles
- `line_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: the styles (optional)

**Example**

```c0de
CALL c0de_paint/cpaint/DRAW_ARC
  PARAMETER canvas c ;
  PARAMETER center middle ;
  PARAMETER a east ;
  PARAMETER b up ;
  PARAMETER start 0 ;
  PARAMETER end 180 ;
  PARAMETER line_style arch ;
```
