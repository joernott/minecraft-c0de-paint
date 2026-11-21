```json
{
  "title": "CANVAS_MIRROR",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 10
}
```

Mirrors the canvas at the plane perpendicular to the axis at `position`, like 4.5 for the plane between the voxels 4 and 5. Without `position`, it mirrors at the middle of the bounds, so the bounds stay the same. The states of the blocks are mirrored too, also the hinges of doors.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas (a variable)
- `axis` STRING: "x", "y" or "z"
- `position` FLOAT: the plane (optional)

**Example**

```c0de
CALL c0de_paint/cpaint/CANVAS_MIRROR
  PARAMETER canvas c ;
  PARAMETER axis "x" ;
```
