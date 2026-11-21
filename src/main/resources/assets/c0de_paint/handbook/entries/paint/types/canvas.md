```json
{
  "title": "CANVAS",
  "icon": "c0de:type_library_block",
  "category": "types",
  "ordinal": 0
}
```

A canvas is a three-dimensional grid of voxels without a fixed size\; it grows with its content. Every voxel is either not set or holds a block. Air is a block, so a voxel can be set to air on purpose: rendering removes the block there.

The bounds of a canvas are the smallest box that holds all set voxels. Coordinates may be negative, but must lie within the limits of the settings, otherwise a drawing raises CANVAS\_LIMIT and draws nothing. SAY and the debugger show a canvas like "CANVAS 20×10×8, 1600 voxels".

A new variable holds an empty canvas. Programs only work with canvases through the functions of the library. A canvas can't be converted to JSON or YAML.

```c0de
VARIABLE house TYPE c0de_paint/cpaint/CANVAS
```
