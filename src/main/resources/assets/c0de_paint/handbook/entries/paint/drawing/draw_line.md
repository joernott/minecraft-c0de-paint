```json
{
  "title": "DRAW_LINE",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 1
}
```

Draws a straight line between two points.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas (a variable)
- `from` STRUCT c0de\_paint/cpaint/POINT: one end
- `to` STRUCT c0de\_paint/cpaint/POINT: the other end
- `style` TYPE c0de\_paint/cpaint/STYLE: the style

**Example**

```c0de
CALL c0de_paint/cpaint/DRAW_LINE
  PARAMETER canvas c ;
  PARAMETER from a ;
  PARAMETER to b ;
  PARAMETER style fence ;
```
