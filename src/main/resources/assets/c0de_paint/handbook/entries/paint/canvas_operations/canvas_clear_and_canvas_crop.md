```json
{
  "title": "CANVAS_CLEAR and CANVAS_CROP",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 3
}
```

CANVAS\_CLEAR unsets the voxels in the box from `from` to `to`, or all voxels without them. CANVAS\_CROP unsets all voxels outside the box.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas (a variable)
- `from`, `to` STRUCT c0de\_paint/cpaint/POINT: the box (optional for CANVAS\_CLEAR)

**Example**

```c0de
CALL c0de_paint/cpaint/CANVAS_CROP
  PARAMETER canvas c ;
  PARAMETER from a ;
  PARAMETER to b ;
```
