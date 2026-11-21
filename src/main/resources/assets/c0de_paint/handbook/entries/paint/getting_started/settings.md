```json
{
  "title": "Settings",
  "icon": "minecraft:writable_book",
  "category": "getting_started",
  "ordinal": 2
}
```

The server has the file `config/c0de_paint.properties`.

- `canvasMin` and `canvasMax` (default −128 and 127) limit the coordinates of a canvas on every axis.
- `renderBlocksPerTick` (1 to 1024, default 64) is the number of blocks RENDER\_CANVAS places per tick.
- `renderMiningSpeed` (0 to 100, default 1) is how much faster than a player RENDER\_CANVAS mines blocks with tools\; 0 mines instantly.
- `canvasVoxelsPerTick` (1024 to 1048576, default 65536) is the number of voxels a drawing function or a transformation processes per tick. Larger work is spread over several ticks while the process waits.
