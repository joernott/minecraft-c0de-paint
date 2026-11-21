```json
{
  "title": "CANVAS_ROTATE",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 9
}
```

Rotates the canvas around the axis "x", "y" or "z" through `center` by any angle in degrees. Positive angles follow the right-hand rule: +90 about y turns east to north. Every voxel takes the nearest voxel of the original, so there are no holes\; at angles that aren't multiples of 90, edges become a little irregular. The states of the blocks turn as far as Minecraft allows (facing, axis, rotation, the shapes of stairs and rails), rounded to 90°. A state the block can't take stays as it was: stairs can't face up.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas (a variable)
- `center` STRUCT c0de\_paint/cpaint/POINT: a point of the axis
- `axis` STRING: "x", "y" or "z"
- `degrees` FLOAT: the angle

**Example**

```c0de
CALL c0de_paint/cpaint/CANVAS_ROTATE
  PARAMETER canvas c ;
  PARAMETER center middle ;
  PARAMETER axis "y" ;
  PARAMETER degrees 90 ;
```
