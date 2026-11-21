```json
{
  "title": "DRAW_ELLIPSE",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 4
}
```

Draws an ellipse around `center`. The distance of `a` from the center is the first semi-axis. `b` lies in the plane of the ellipse\; its distance from the first axis is the second semi-axis. A circle has equal distances.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas (a variable)
- `center`, `a`, `b` STRUCT c0de\_paint/cpaint/POINT: the points
- `line_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: the styles (optional)

**Example**

```c0de
CALL c0de_paint/cpaint/DRAW_ELLIPSE
  PARAMETER canvas c ;
  PARAMETER center middle ;
  PARAMETER a east ;
  PARAMETER b south ;
  PARAMETER line_style rim ;
```
