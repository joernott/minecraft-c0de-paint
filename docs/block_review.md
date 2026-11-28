# Review of the BLOCK object

Requested by the user on 2026-11-28, before the include `c0de_paint/schematics` is added (sprint 9). The question: does the BLOCK of 0.1.0 cover what schematics need, or does it need rework?

## Today

- **The struct BLOCK** (`c0de_paint/cpaint/BLOCK`) has three elements:
  - `id` STRING; an id without a namespace gets `minecraft:`;
  - `properties` HASH STRING, the block states;
  - `weight` INTEGER, 1 or more.
- **A voxel of a canvas** holds a `Block` of `core`: the id and the sorted block states, nothing else. The weight lives only in the styles (`Weighted`); CANVAS_GET returns every block with the weight 1.
- **The id is a string**, never checked against the registry before RENDER_CANVAS. A canvas can hold blocks that the server doesn't know.
- **The items of a rendering** come from `Renderer.item()`: the item of the block (`asItem`), nothing for air, the upper half of doors and plants and the head of beds, INVALID_ARGUMENT for blocks without an item (water, fire, piston heads).

## What schematics need

| Need | Covered today? |
|---|---|
| Blocks that the server doesn't know (Create blocks on a server without Create, blocks of a newer Minecraft) are kept, so that they can be listed and replaced. | Yes: the id is a string and only RENDER_CANVAS needs a known block. |
| Blocks with their states. | Yes. |
| **Block entity data:** the text of signs, the patterns of banners, the heads of players, the contents of chests and shulker boxes, the settings of Create's blocks (gearshifts, deployers, funnels with filters). Litematica keeps them in `TileEntities`, Create in the `nbt` of a block. | **No.** A voxel has no place for them; signs would be blank, Create machines unconfigured. CANVAS_FROM_WORLD loses them as well. |
| **The items to build a schematic:** a list by item, not by block. Double slabs need two slabs, a candle block with three candles three candles, a snow layer of 4 four snow layers, a potted plant a flower pot and the plant, a waterlogged block a water bucket. | **Partly:** CANVAS_MATERIALS counts block ids, not items, and RENDER_CANVAS takes one item per block. Water, lava and other blocks without an item raise INVALID_ARGUMENT instead of being listed. |
| **Replacing blocks** while importing: "every `create:andesite_casing` becomes stripped spruce logs", keeping the states that the new block has. | **No:** CANVAS_REPLACE replaces by id but drops the states (one of the issues found for 0.2.0). |
| **Upgrading old files:** blocks renamed between Minecraft versions (e.g. `grass` → `short_grass` in 1.20.3). | Not a question of BLOCK: the vanilla DataFixer upgrades palettes while reading. |
| Entities (item frames, armor stands, paintings, Create's super glue). | No, and not planned: a canvas is made of voxels. The import counts the entities it leaves out. |

## Recommendation

1. **Keep the struct BLOCK and its elements**, so the programs of 0.1.0 keep working. `weight` stays a part of the struct but is documented as an element of styles only; canvases ignore it.
2. **Add the element `data` STRING:** the block entity data as SNBT, as `/data get block` shows it (e.g. `{Text1:'"Welcome"'}`), or "" for none. A voxel of a canvas stores it with the block (the palette of a canvas section holds the block with its data, so blocks without data cost nothing more). STYLE_SOLID, CANVAS_GET, CANVAS_SET and the transformations carry it along; CANVAS_FROM_WORLD reads it; the schematic import fills it.
3. **Render the data only where nothing is taken from inventories.** Otherwise a schematic with full chests or shulker boxes would create items out of nothing. RENDER_CANVAS without `input` (administrators, `allowBlockChanges`) places the data; with `input`, the blocks are placed without data and the result of the call says how many lost it. *(Decision of the user, see below.)*
4. **One rule for replacing blocks**, used by CANVAS_REPLACE and by the `replace` parameter of the import: a key matches a block by id (`create:andesite_casing`) or by id and states (`oak_stairs[half=top]`); the new block keeps every state of the old one that it also has, then the states written in the replacement apply. A replacement "" leaves the voxel not set.
5. **A mapping from blocks to items** in the mod layer that RENDER_CANVAS and the lists of the schematics share, so that a list says exactly what a rendering takes: counts of slabs, candles, sea pickles, turtle eggs and snow layers; flower pot plus plant; buckets for water, lava and waterlogged blocks; nothing for the parts of multi-block structures (upper halves, bed heads, piston heads) and for blocks without an item, which are listed apart. RENDER_CANVAS takes buckets of water and lava from the inventories and puts back the empty buckets.
6. **No new struct** for the lists: a HASH INTEGER by item id, like CANVAS_MATERIALS, keeps them easy to print and to compare with an inventory.

## Decisions for the user

- **Block entity data in survival rendering** (point 3): placed only without `input` (recommended), or never, or always with the contents of inventories stripped (signs and banners keep their data; chests stay empty).
- **The element name** `data` (recommended; the word of `/data`) or `nbt`.
