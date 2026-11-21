```json
{
  "title": "DRAW_BLOCK",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 0
}
```

Draws one voxel.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas (a variable)
- `point` STRUCT c0de\_paint/cpaint/POINT: the voxel
- `style` TYPE c0de\_paint/cpaint/STYLE: the style

**Example**

```c0de
CALL c0de_paint/cpaint/DRAW_BLOCK
  PARAMETER canvas c ;
  PARAMETER point top ;
  PARAMETER style glowstone ;
```
