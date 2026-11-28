```json
{
  "title": "CANVAS_FROM_WORLD",
  "icon": "minecraft:spyglass",
  "category": "rendering",
  "ordinal": 1
}
```

A new canvas with the blocks of the world between two positions, with their states and their data (the contents of chests, the text of signs)\; `from` becomes 0,0,0. Air becomes not set, or air if `air` is TRUE. Every corner must lie within the limits of a canvas seen from `from`: with the default settings, up to 255 blocks in the positive and 128 in the negative direction of every axis\; otherwise CANVAS\_LIMIT. Chunks that are not loaded raise CHUNK\_NOT\_LOADED.

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
