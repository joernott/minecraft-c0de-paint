```json
{
  "title": "CANVAS_COUNT",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 0
}
```

The number of set voxels.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas
- result `result` INTEGER: the number

**Example**

```c0de
CALL c0de_paint/cpaint/CANVAS_COUNT
  PARAMETER canvas c ;
  RESULT result voxels
```
