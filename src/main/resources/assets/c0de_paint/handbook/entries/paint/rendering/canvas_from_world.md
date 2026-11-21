```json
{
  "title": "CANVAS_FROM_WORLD",
  "icon": "minecraft:spyglass",
  "category": "rendering",
  "ordinal": 1
}
```

A new canvas with the blocks of the world between two positions, with their states\; `from` becomes 0,0,0. Air becomes not set, or air if `air` is TRUE. The contents of chests and other block data are not copied. A region larger than the limits raises CANVAS\_LIMIT, chunks that are not loaded CHUNK\_NOT\_LOADED.

**Parameters and results**

- `from`, `to` STRUCT c0de/minecraft/POSITION: the corners of the region
- `air` BOOLEAN: copy air too (optional, default FALSE)
- result `result` TYPE c0de\_paint/cpaint/CANVAS: the canvas

**Example**

```c0de
CALL c0de_paint/cpaint/CANVAS_FROM_WORLD
  PARAMETER from corner ;
  PARAMETER to other_corner ;
  RESULT result copy
```
