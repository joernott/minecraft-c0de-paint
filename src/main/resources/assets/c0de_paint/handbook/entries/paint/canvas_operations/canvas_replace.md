```json
{
  "title": "CANVAS_REPLACE",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 5
}
```

Draws every voxel with the block `id` again with `style`, whatever the states of the block are.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas (a variable)
- `id` STRING: the block, like "stone"
- `style` TYPE c0de\_paint/cpaint/STYLE: the new style

**Example**

```c0de
CALL c0de_paint/cpaint/CANVAS_REPLACE
  PARAMETER canvas c ;
  PARAMETER id "cobblestone" ;
  PARAMETER style mossy ;
```
