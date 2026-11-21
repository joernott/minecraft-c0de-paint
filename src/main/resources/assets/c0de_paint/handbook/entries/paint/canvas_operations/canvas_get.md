```json
{
  "title": "CANVAS_GET",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 2
}
```

The block of a voxel, with the weight 1. Its id is "" if the voxel is not set, and "minecraft:air" for air.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas
- `point` STRUCT c0de\_paint/cpaint/POINT: the voxel
- result `result` STRUCT c0de\_paint/cpaint/BLOCK: the block

**Example**

```c0de
CALL c0de_paint/cpaint/CANVAS_GET
  PARAMETER canvas c ;
  PARAMETER point p ;
  RESULT result block
```
