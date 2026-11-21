```json
{
  "title": "DRAW_ELLIPSOID",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 7
}
```

Draws an axis-aligned ellipsoid with the radii `rx`, `ry` and `rz`\; a sphere when they are equal. A radius of 0 makes it flat on that axis.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas (a variable)
- `center` STRUCT c0de\_paint/cpaint/POINT: the center
- `rx`, `ry`, `rz` INTEGER: the radii
- `side_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: the styles (optional)

**Example**

```c0de
CALL c0de_paint/cpaint/DRAW_ELLIPSOID
  PARAMETER canvas c ;
  PARAMETER center middle ;
  PARAMETER rx 8 ;
  PARAMETER ry 8 ;
  PARAMETER rz 8 ;
  PARAMETER side_style glass ;
```
