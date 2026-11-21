```json
{
  "title": "DRAW_PYRAMID",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 10
}
```

Draws a pyramid with a square base, its sides parallel to the axes. Half the length of a side is `r`\; the tip lies `h` voxels from the base in `direction`. The lines are the four sides of the base and the four edges to the tip.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas (a variable)
- `center` STRUCT c0de\_paint/cpaint/POINT: the center of the base
- `r`, `h` INTEGER: half the side and the height
- `direction` STRING: the direction of the tip
- `line_style`, `side_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: the styles (optional)

**Example**

```c0de
CALL c0de_paint/cpaint/DRAW_PYRAMID
  PARAMETER canvas c ;
  PARAMETER center base ;
  PARAMETER r 10 ;
  PARAMETER h 10 ;
  PARAMETER direction "+y" ;
  PARAMETER fill_style sandstone ;
```
