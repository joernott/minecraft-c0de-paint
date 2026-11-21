```json
{
  "title": "CANVAS_MOVE",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 4
}
```

Moves the content by `offset`.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas (a variable)
- `offset` STRUCT c0de\_paint/cpaint/POINT: how far, on every axis

**Example**

```c0de
CALL c0de_paint/cpaint/CANVAS_MOVE
  PARAMETER canvas c ;
  PARAMETER offset up ;
```
