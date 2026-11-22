```json
{
  "title": "DRAW_CONE",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 10
}
```

Draws a cone with the base of DRAW\_CYLINDER\; its tip lies `h` voxels from the base in `direction`. The line is the rim of the base.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas (a variable)
- `center` STRUCT c0de\_paint/cpaint/POINT: the center of the base
- `r`, `h` INTEGER: the radius and the height
- `direction` STRING: the direction of the tip
- `line_style`, `side_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: the styles (optional)

**Example**

```c0de
CALL c0de_paint/cpaint/DRAW_CONE
  PARAMETER canvas c ;
  PARAMETER center roof ;
  PARAMETER r 5 ;
  PARAMETER h 6 ;
  PARAMETER direction "+y" ;
  PARAMETER fill_style shingles ;
```
