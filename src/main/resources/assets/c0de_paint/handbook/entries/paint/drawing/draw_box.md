```json
{
  "title": "DRAW_BOX",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 6
}
```

Draws an axis-aligned box between two opposite corners. The lines are its 12 edges. A cube is a box with equal sides.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas (a variable)
- `from`, `to` STRUCT c0de\_paint/cpaint/POINT: the corners
- `line_style`, `side_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: the styles (optional)

**Example**

```c0de
CALL c0de_paint/cpaint/DRAW_BOX
  PARAMETER canvas c ;
  PARAMETER from a ;
  PARAMETER to b ;
  PARAMETER line_style logs ;
  PARAMETER side_style planks ;
```
