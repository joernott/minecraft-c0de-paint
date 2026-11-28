```json
{
  "title": "RENDER_CANVAS",
  "icon": "minecraft:bricks",
  "category": "rendering",
  "ordinal": 0
}
```

Builds the canvas in the world\; its voxel 0,0,0 lands at `position`. Voxels that are not set leave the world as it is, air removes the block there, and blocks that are already in place are skipped.

Without `input`, the program needs allowBlockChanges (`config/c0de.properties` of c0de) or an administrator as owner, and blocks in the way are replaced. With `input`, every owner may render: the items come from `output` first, then from `input`, as CANVAS\_ITEMS counts them (buckets of water come back empty)\; blocks in the way are mined with the tools of `tools`, which take damage, and the drops go to `output`. Blocks that break without a tool, like dirt and glass, are mined by hand\; without `output`, the drops are lost. The sheets, the spawn protection and blocks a player can't break raise PERMISSION\_DENIED.

**Block data.** The data of the blocks (the element `data`) is placed with them. Blocks with inventories keep their contents: without `input`, the items come with the data\; with `input`, they are taken from the inventories like the blocks, and `contents` decides what happens when some are missing: "REQUIRED" (the default) raises ITEM\_MISSING before the first block, "AVAILABLE" fills in what there is, "NONE" places the inventories empty. With `input`, data that would create things out of nothing is left out: loot tables, the bees of beehives, the item of suspicious sand and gravel, the book of a lectern. The data of command blocks, structure blocks and jigsaw blocks needs an administrator (PERMISSION\_DENIED).

**Missing blocks.** With `blocks` "AVAILABLE", the blocks that the inventories hold are placed and the voxels of the others are skipped instead of raising ITEM\_MISSING. The result `missing` lists by item id what was left out, blocks and contents, so the program can fetch it and render again: blocks already in place are skipped, and they keep their contents.

The chunks of the area are loaded for the rendering, also far away from all players, and stay loaded for a while\; the inventories must lie in loaded chunks (CHUNK\_NOT\_LOADED). Before the first block, RENDER\_CANVAS checks the rights, the blocks in the inventories (ITEM\_MISSING) and a tool for every block that needs one, so an error doesn't leave half a building. A full output (INVENTORY\_FULL) or a broken tool stop it later\; the message says how many blocks were placed. The process waits while rendering: it places `renderBlocksPerTick` blocks per tick, from bottom to top, and torches, ladders and the like after the other blocks of their layer. Mining takes the time of a player divided by `renderMiningSpeed`.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas
- `position` STRUCT c0de/minecraft/POSITION: where the voxel 0,0,0 lands
- `input` STRUCT c0de/minecraft/POSITION: the inventory with the blocks (optional)
- `tools` STRUCT c0de/minecraft/POSITION: the inventory with the tools (optional)
- `output` STRUCT c0de/minecraft/POSITION: the inventory for removed blocks, used first for blocks (optional)
- `contents` STRING: "REQUIRED", "AVAILABLE" or "NONE", for the contents of inventories (optional, default "REQUIRED")
- `blocks` STRING: "REQUIRED" or "AVAILABLE", for missing blocks (optional, default "REQUIRED")
- result `missing` HASH INTEGER: what was left out, by item id

**Example**

```c0de
CALL c0de_paint/cpaint/RENDER_CANVAS
  PARAMETER canvas c ;
  PARAMETER position site ;
  PARAMETER input supplies ;
  PARAMETER tools toolbox ;
  PARAMETER output rubble ;
```
