```json
{
  "title": "CANVAS_MATERIALS",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 7
}
```

The blocks of a canvas, as a HASH from block id to number, without air (CANVAS\_COUNT counts air). Blocks with different states count together. CANVAS\_ITEMS counts the items that RENDER\_CANVAS takes.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas
- result `result` HASH INTEGER: the numbers

**Example**

```c0de
CALL c0de_paint/cpaint/CANVAS_MATERIALS
  PARAMETER canvas c ;
  RESULT result shopping_list
```
