```json
{
  "title": "RENDER_CANVAS",
  "icon": "minecraft:bricks",
  "category": "rendering",
  "ordinal": 0
}
```

Builds the canvas in the world\; its voxel 0,0,0 lands at `position`. Voxels that are not set leave the world as it is, air removes the block there, and blocks that are already in place are skipped.

Without `input`, the program needs allowBlockChanges or an administrator as owner, and blocks in the way are replaced. With `input`, every owner may render: the blocks come from `output` first, then from `input`\; blocks in the way are mined with the tools of `tools`, which take damage, and the drops go to `output`. The sheets, the spawn protection and blocks a player can't break raise PERMISSION\_DENIED.

Before the first block, RENDER\_CANVAS checks the rights, the chunks (CHUNK\_NOT\_LOADED), the blocks in the inventories (ITEM\_MISSING) and a tool for every block that needs one, so an error doesn't leave half a building. A full output (INVENTORY\_FULL) or a broken tool stop it later\; the message says how many blocks were placed. The process waits while rendering: it places `renderBlocksPerTick` blocks per tick, from bottom to top, and torches, ladders and the like after the other blocks of their layer. Mining takes the time of a player divided by `renderMiningSpeed`.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas
- `position` STRUCT c0de/minecraft/POSITION: where the voxel 0,0,0 lands
- `input` STRUCT c0de/minecraft/POSITION: the inventory with the blocks (optional)
- `tools` STRUCT c0de/minecraft/POSITION: the inventory with the tools (optional)
- `output` STRUCT c0de/minecraft/POSITION: the inventory for removed blocks, used first for blocks (optional)

**Example**

```c0de
CALL c0de_paint/cpaint/RENDER_CANVAS
  PARAMETER canvas c ;
  PARAMETER position site ;
  PARAMETER input supplies ;
  PARAMETER tools toolbox ;
  PARAMETER output rubble ;
```
