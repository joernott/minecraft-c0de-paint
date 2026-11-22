```json
{
  "title": "DRAW_SPIRAL",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 6
}
```

Draws a spiral on the ellipse of DRAW\_ELLIPSE that rises in `direction`. It starts at `a` and turns towards `b`. Every voxel of the spiral lies `elevation` layers higher than the one before, rounded down: 1 climbs one layer per voxel, 0.5 every second voxel, 2 leaves a gap of one layer. The spiral ends below the layer `h`, like a cylinder of the height `h`. The line is the spiral\; the fill style draws the ramp between the axis through `center` and the spiral, like the steps of a spiral staircase, with the axis in every layer.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas (a variable)
- `center`, `a`, `b` STRUCT c0de\_paint/cpaint/POINT: the points of the ellipse
- `direction` STRING: "+x", "-x", "+y", "-y", "+z" or "-z"
- `elevation` FLOAT: the layers from one voxel of the spiral to the next, more than 0 (optional, default 1)
- `h` INTEGER: the height in layers
- `line_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: the styles (optional)

**Example**

```c0de
CALL c0de_paint/cpaint/DRAW_SPIRAL
  PARAMETER canvas c ;
  PARAMETER center middle ;
  PARAMETER a east ;
  PARAMETER b south ;
  PARAMETER direction "+y" ;
  PARAMETER elevation 0.25 ;
  PARAMETER h 20 ;
  PARAMETER line_style railing ;
  PARAMETER fill_style steps ;
```
