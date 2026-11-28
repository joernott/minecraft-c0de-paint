# Changelog

## 0.2.0

Schematics, block data and fixes from the recipes of c0de.at. It requires c0de 0.7.0 or later.

- **Schematics**: the new library `c0de_paint/schematics` reads the schematics of Litematica (`.litematic`) and Create (`.nbt`) from the folder `schematics` of the player who runs the process: SCHEMATIC_LOAD into a canvas, SCHEMATIC_MATERIALS (the items to build it), SCHEMATIC_MISSING (the blocks the server doesn't have) and SCHEMATIC_LIST. Blocks can be replaced while reading; schematics of older Minecraft versions are upgraded. The client setting `allowSchematics` and the server setting `maxSchematicSize`.
- **Block data**: BLOCK has the element `data`, the data of the block entity (sign text, chest contents, machine settings). Canvases, styles and transformations keep it, CANVAS_FROM_WORLD reads it, RENDER_CANVAS places it. Inventories are filled from the input; the new parameters `contents` and `blocks` of RENDER_CANVAS decide what happens when items are missing, and the result `missing` lists them.
- **Items**: RENDER_CANVAS takes what a player needs: two slabs for a double slab, the candles of a candle block, buckets for water (which come back empty). The new CANVAS_ITEMS lists them.
- **CANVAS_REPLACE** keeps the states of the blocks it replaces that the new block has, so stairs keep their direction.
- The constant `C0DE_PAINT_VERSION`.
- The handbook explains rendering soft blocks without tools and drops without output, where allowBlockChanges is set and the limit of CANVAS_FROM_WORLD.

## 0.1.0

The first version of c0de paint, for Minecraft 1.21.1, 1.21.11, 26.1.2, 26.2 and 26.3 on Fabric and NeoForge. It requires c0de 0.7.0 or later.

- The library `c0de_paint/cpaint` with the types CANVAS and STYLE and the structs POINT and BLOCK.
- Styles: STYLE_NONE, STYLE_EMPTY, STYLE_SOLID, STYLE_RANDOM, STYLE_GRADIENT, STYLE_PATTERN and INVENTORY_BLOCKS.
- Drawing: DRAW_BLOCK, DRAW_LINE, DRAW_RECTANGLE, DRAW_POLYGON, DRAW_ELLIPSE, DRAW_ARC, DRAW_SPIRAL, DRAW_BOX, DRAW_ELLIPSOID, DRAW_CYLINDER, DRAW_CONE, DRAW_PYRAMID and the Platonic solids, in any plane.
- Canvas operations: CANVAS_COUNT, CANVAS_MIN, CANVAS_MAX, CANVAS_GET, CANVAS_CLEAR, CANVAS_CROP, CANVAS_MOVE, CANVAS_REPLACE, CANVAS_MERGE, CANVAS_MATERIALS, CANVAS_SCALE, CANVAS_ROTATE and CANVAS_MIRROR, with the states of blocks.
- Rendering: RENDER_CANVAS with blocks and tools from inventories, and CANVAS_FROM_WORLD.
- The handbook in English and German, as a binder of the handbooks of c0de.
- The runner library `c0de-paint-runner-0.1.0.jar` for the program runner of c0de.
