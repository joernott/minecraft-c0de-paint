```json
{
  "title": "DRAW_CYLINDER",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 9
}
```

Draws a cylinder. The base circle has its center at `center` and the radius `r`\; the cylinder grows by `h` voxels in `direction`. The lines are the rims of both ends.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas (a variable)
- `center` STRUCT c0de\_paint/cpaint/POINT: the center of the base
- `r`, `h` INTEGER: the radius and the height
- `direction` STRING: "+x", "-x", "+y", "-y", "+z" or "-z"
- `line_style`, `side_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: the styles (optional)

**Example**

```c0de
CALL c0de_paint/cpaint/DRAW_CYLINDER
  PARAMETER canvas c ;
  PARAMETER center base ;
  PARAMETER r 4 ;
  PARAMETER h 12 ;
  PARAMETER direction "+y" ;
  PARAMETER side_style stone ;
```
