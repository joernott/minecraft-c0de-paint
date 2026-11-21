```json
{
  "title": "CANVAS_MIN and CANVAS_MAX",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 1
}
```

The corners of the bounds. An empty canvas raises CANVAS\_EMPTY.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas
- result `result` STRUCT c0de\_paint/cpaint/POINT: the lowest corner (CANVAS\_MIN) or the highest corner (CANVAS\_MAX)

**Example**

```c0de
CALL c0de_paint/cpaint/CANVAS_MAX
  PARAMETER canvas c ;
  RESULT result top
```
