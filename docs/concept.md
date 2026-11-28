# c0de paint

c0de paint is a companion mod of [c0de](https://github.com/joernott/minecraft-c0de). It adds a library with drawing functions that create two- and three-dimensional shapes in Minecraft.

A program draws shapes into a **canvas**, a three-dimensional array of voxels. Canvases can be combined and transformed. The function RENDER_CANVAS places the content of a canvas in the world. Players who are not administrators give it an inventory from which the blocks are taken, as with PLACE_BLOCK of c0de/minecraft.

A **style** describes how a line, a face or the inside of a body is drawn: not at all, with air, with one block, with randomly chosen blocks, with a gradient or with a pattern.

The open points of the concept are listed in [concept_review.md](concept_review.md).

## Mod and library

- The mod is called c0de paint and has the mod ID `c0de_paint`. It requires c0de from the version that introduces library types (see "Types") up to, but not including, the next major version.
- It contains one library. A program includes it with `INCLUDE c0de_paint/cpaint`, a wink at Deluxe Paint, which is usually called DPaint. This document leaves out the library prefix of names, e.g. `CANVAS` for `c0de_paint/cpaint/CANVAS`.
- The handbook of the library is one of the binders of c0de, in English and German, like the handbook of c0de net.
- On Modrinth, the mod gets the environment "client and server", like c0de.
- **Errors:** the library uses the codes of c0de where they fit (INVALID_ARGUMENT, PERMISSION_DENIED, CHUNK_NOT_LOADED, ITEM_MISSING, INVENTORY_FULL) and its own from 1100 on, which it has as INTEGER constants: CANVAS_LIMIT (1100), CANVAS_EMPTY (1101) and NOT_A_PLANE (1102). c0de net uses 1000 to 1099.

## Types

c0de paint defines two **library types**, CANVAS and STYLE. They are neither structs nor arrays. A program only works with them through the functions of the library. Library types are a new feature of c0de (see "Changes to c0de").

Like every c0de value, a canvas or style is copied on assignment, as a parameter and as a result. Internally the copy is cheap (copy on write). Programs declare them with the TYPE block of c0de: a variable declared with `VARIABLE c TYPE c0de_paint/cpaint/CANVAS` contains an empty canvas, one declared with `VARIABLE s TYPE c0de_paint/cpaint/STYLE` contains STYLE_NONE.

Besides these, the library defines two structs:

| Struct | Elements | Use |
|---|---|---|
| POINT | x, y, z INTEGER | A coordinate in a canvas |
| BLOCK | id STRING, properties HASH STRING, weight INTEGER | A block of a style or of a canvas, with block states as in PLACE_BLOCK |

Coordinates in the world are always `c0de/minecraft/POSITION`. Coordinates in a canvas are POINTs, made of integers.

### Canvas

- A canvas has no fixed size. It grows with its content.
- Every voxel is either **not set** (nothing is drawn there) or holds a block. Air is a block, so a voxel can be set to air on purpose.
- The **bounds** of a canvas are the smallest box that contains all set voxels. An empty canvas has no bounds.
- Coordinates may be negative. Every coordinate must lie within the limits from the configuration (default −128 to 127 on each axis). If a drawing function would set a voxel outside the limits, it raises the error CANVAS_LIMIT and draws nothing.
- Two canvases may use different ranges of coordinates. For example, one holds a house at x 0 to 20 and another a tower at x 30 to 40; merging them keeps both in place.

## Styles

A style is created with one of these functions. c0de has no constants of library types, and functions are called with CALL, so STYLE_NONE and STYLE_EMPTY are functions without parameters too; a new STYLE variable already holds STYLE_NONE.

| Function | Result |
|---|---|
| STYLE_NONE | Don't draw: the voxels stay as they are. In a merge, a voxel that is not set doesn't change the target. |
| STYLE_EMPTY | Air (`minecraft:air`). Erases voxels that were drawn before. Together with merging, it creates hollow bodies with thick walls. |
| STYLE_SOLID(block) | Always the same block. Its weight is ignored. |
| STYLE_RANDOM(blocks) | For every voxel, a randomly chosen block from the ARRAY OF BLOCK. The chance of a block is its weight divided by the sum of all weights. |
| STYLE_GRADIENT(blocks, from, to, axes, fuzzy) | A gradient through the blocks, in the order of the array (see below). |
| STYLE_PATTERN(pattern, from) | The canvas `pattern`, repeated (see below). |

**Blocks** are given as BLOCK. An `id` without a namespace gets `minecraft:`, e.g. "stone" is "minecraft:stone".

**Weights** are integers of 1 or more. A weight of 0 or less raises INVALID_ARGUMENT. This matches the blocks of an inventory, where a slot never holds 0 items.

**Random numbers** come from the random number generator of the process, as with RANDOM. If a program is started with a fixed seed, its drawings are the same every time.

### Blocks from an inventory

INVENTORY_BLOCKS(position) returns the blocks of an inventory as an ARRAY OF BLOCK:
- in the order of the slots;
- with the number of items as weight;
- a block that fills several slots is listed once, at its first slot, with the sum of the items;
- items that can't be placed as blocks are ignored.

This replaces the CREATE_RANDOM_STYLE and CREATE_GRADIENT of the first draft:
- `STYLE_RANDOM(INVENTORY_BLOCKS(chest))` builds a random style from a chest.
- `STYLE_GRADIENT(INVENTORY_BLOCKS(chest), …)` builds a gradient in the order of the slots.

### Gradient

STYLE_GRADIENT(blocks, from, to, axes, fuzzy)

- **from** (POINT): where the gradient starts. The first block of the array is drawn there.
- **axes** (STRING, default "xyz"): the axes along which the distance from `from` is measured, as a straight line:
  - "xyz" measures from the point `from`, which gives spheres.
  - "y" measures from the plane y = from.y, which gives horizontal layers.
  - "xz" measures from the vertical line through `from`, which gives cylinders.
- **to** (POINT, optional): where the gradient ends.
  - The length of the gradient is the distance from `from` to `to`, measured along `axes`.
  - Without `to`, the gradient ends at the voxel of the part (line, face or inside) that is farthest from `from`.
- **The weights divide the length.** Each block gets a share of the length in proportion to its weight. Voxels beyond the end get the last block.
- **Example:**
  - Five black, three gray and two white concrete: a line of 10 voxels gets 5 black, 3 gray and 2 white.
  - A line of 20 voxels gets 10 black, 6 gray and 4 white.
  - **The rule behind it:** a voxel at distance d gets the block whose share contains (d + ½) · W / (L + 1). W is the sum of the weights; L is the length.
- **fuzzy** (INTEGER 0 to 3, default 0): how blurred the borders between two blocks are:
  - 0: sharp borders.
  - 1: one line of voxels on each side of a border takes the neighbouring block with a chance of 50 %.
  - 2: two lines, with 50 % and 25 %.
  - 3: three lines, with 50 %, 25 % and 12.5 %.

### Pattern

STYLE_PATTERN(pattern, from) repeats the canvas `pattern` along all three axes.
- **Size:** the size of one repetition is the size of the bounds of `pattern`.
- **Position:** the minimum corner of the bounds lies at `from` (POINT, default 0,0,0).
- **Gaps:** voxels that are not set in the pattern are not drawn.

## Drawing

Every drawing function draws into a canvas that it receives as a reference parameter, as INPUT does with its variable. The functions return nothing.

### Rules for all shapes

- **The parts of a shape:** a shape consists of **lines** (outlines and edges), **faces** (the surface of a body) and its **inside** (the area of a 2D shape, the volume of a body).
  - Each part has its own style parameter: `line_style`, `side_style` and `fill_style`.
  - The style parameters are optional. Their default is STYLE_NONE, except for the line of DRAW_LINE and the block of DRAW_BLOCK.
  - A part whose style is STYLE_NONE takes the style of the next part inward:
    - lines take the side style, or the fill style for 2D shapes;
    - faces take the fill style.
  - **Examples:**
    - DRAW_BOX with only a fill style gives a full box.
    - A side style without a fill style gives a hollow box.
    - Only a line style gives a frame.
- **Rasterization:**
  - A voxel belongs to a shape when its center lies inside the shape.
  - Lines and faces are one voxel thick.
  - Lines connect through edges and corners, not only through faces. This makes them thin, as with the Bresenham algorithm in 3D.
- **2D shapes in 3D space:**
  - 2D shapes can lie in any plane. The plane is given by three points, which are usually the points of the shape itself.
  - If the points lie on one line, the function raises the error NOT_A_PLANE.
- **3D bodies:** bodies are axis-aligned, or have a direction. Other orientations come from CANVAS_ROTATE.

### Functions

| Function | Shape | Styles |
|---|---|---|
| DRAW_BLOCK(canvas, point, style) | One voxel. | style |
| DRAW_LINE(canvas, from, to, style) | A straight line between two points. | style |
| DRAW_RECTANGLE(canvas, a, b, c, …) | A rectangle. a and b are the ends of one side. The other side runs perpendicular to it, up to the distance of c from the line a–b. Axis-aligned: a = 0,0,0, b = 9,0,0, c = 0,0,4. | line, fill |
| DRAW_POLYGON(canvas, points, …) | A polygon through the ARRAY OF POINT, closed automatically. The first three points that don't lie on one line define the plane. Points beside the plane are projected onto it. | line, fill |
| DRAW_ELLIPSE(canvas, center, a, b, …) | An ellipse. `a` is the end of the first semi-axis; its distance from `center` is rx. `b` lies in the plane, and its distance from the first axis is ry. A circle has equal distances. | line, fill |
| DRAW_ARC(canvas, center, a, b, start, end, …) | The part of the ellipse of DRAW_ELLIPSE from angle `start` to `end` (FLOAT, in degrees, counted from `a` towards `b`). The fill style gives a pie slice. | line, fill |
| DRAW_SPIRAL(canvas, center, a, b, direction, elevation, h, …) | A spiral on the ellipse of DRAW_ELLIPSE, from `a` towards `b`, rising in `direction` ("+x" … "-z"): every voxel lies `elevation` layers (FLOAT, more than 0, default 1, rounded down) higher than the one before, up to the layer `h` - 1. The fill style gives the ramp between the axis and the spiral, like a spiral staircase. Decided on 2026-11-22 (requested by the user while testing). | line, fill |
| DRAW_BOX(canvas, from, to, …) | An axis-aligned box between two opposite corners. | line (edges), side, fill |
| DRAW_ELLIPSOID(canvas, center, rx, ry, rz, …) | An axis-aligned ellipsoid; a sphere when rx = ry = rz. | side, fill |
| DRAW_CYLINDER(canvas, center, r, h, direction, …) | A cylinder. The base circle has its center at `center` and the radius r. The cylinder grows by h voxels in `direction` ("+x", "-x", "+y", "-y", "+z", "-z"). The lines are the rims of the two ends. | line (rims), side, fill |
| DRAW_CONE(canvas, center, r, h, direction, …) | A cone with the same base and direction as the cylinder; its tip lies h voxels from the base. | line (rim), side, fill |
| DRAW_PYRAMID(canvas, center, r, h, direction, …) | A pyramid with a square base, its sides parallel to the axes. The center of the base is `center`, half the length of a side is r, and the tip lies h voxels from the base in `direction`. | line (edges), side, fill |
| DRAW_TETRAHEDRON, DRAW_OCTAHEDRON, DRAW_DODECAHEDRON, DRAW_ICOSAHEDRON (canvas, center, r, …) | The Platonic solid around `center`, with all corners at the distance r from it. Each solid has a fixed orientation, shown in the handbook. Other orientations come from CANVAS_ROTATE. | line (edges), side, fill |

The cube is DRAW_BOX and the sphere is DRAW_ELLIPSOID, so they have no functions of their own.

## Canvas operations

All operations take the canvas as a reference parameter and change it, unless they return a new value.

| Function | Effect |
|---|---|
| CANVAS_COUNT(canvas) | The number of set voxels. |
| CANVAS_MIN(canvas), CANVAS_MAX(canvas) | The corners of the bounds, as POINTs. They raise CANVAS_EMPTY for an empty canvas. |
| CANVAS_GET(canvas, point) | The block of a voxel as BLOCK, with the weight 1. Its id is "" if the voxel is not set and "minecraft:air" for air. |
| CANVAS_CLEAR(canvas, from, to) | Unsets the voxels in the box from–to. Without from and to, it unsets the whole canvas. |
| CANVAS_CROP(canvas, from, to) | Unsets all voxels outside the box from–to. |
| CANVAS_MOVE(canvas, offset) | Moves the content by the POINT `offset`. |
| CANVAS_REPLACE(canvas, id, style) | Draws every voxel with the block `id` again, with `style`, whatever the states of the block are. |
| CANVAS_SCALE(canvas, x, y, z, center) | Scales the canvas along each axis. x, y and z are INTEGER percentages; a value below 1 raises an error. Scaling is around `center` (default 0,0,0) and takes the nearest voxel, so blocks keep their states. |
| CANVAS_ROTATE(canvas, center, axis, degrees) | Rotates around the axis "x", "y" or "z" through the POINT `center`, by any angle (FLOAT, in degrees), by the right-hand rule: +90° about y turns east (+x) to north (−z). Every target voxel takes the nearest source voxel, so there are no holes and no doubled voxels. With angles that aren't multiples of 90°, the edges of shapes become slightly irregular. |
| CANVAS_MIRROR(canvas, axis, position) | Mirrors at the plane perpendicular to the axis "x", "y" or "z", at the coordinate `position` (INTEGER or FLOAT; 4.5 is the plane between the voxels 4 and 5). Without `position`, it mirrors at the middle of the bounds, so the bounds stay the same. |
| CANVAS_MERGE(target, source, mode, offset) | Draws the canvas `source`, or each canvas of an array of canvases in order, into `target`, moved by `offset` (default 0,0,0). With mode "OVERWRITE", set voxels of the source replace those of the target. With "KEEP", they only fill voxels of the target that are not set. Voxels that are not set in the source never change the target; air does. |
| CANVAS_MATERIALS(canvas) | The blocks the canvas needs, as HASH INTEGER from block ID to number, without air; blocks with different states count together. |
| CANVAS_FROM_WORLD(from, to, air) | A new canvas with the blocks of the world between the POSITIONs `from` and `to`; `from` becomes 0,0,0. Air becomes not set, or air if `air` is TRUE. Chunks that are not loaded raise CHUNK_NOT_LOADED. The contents of chests and other block data are not copied. |

### Block states in transformations

- **Rotation:** CANVAS_ROTATE turns the states of the blocks as far as Minecraft allows:
  - `facing`, `axis`, `rotation` and the shapes of stairs and rails.
  - The direction is rounded to the nearest multiple of 90° (for `rotation`, the nearest of its 16 steps).
  - A state that the block can't take stays unchanged. For example, stairs can't face upwards.
- **Mirroring:** CANVAS_MIRROR flips the same states, and also the hinges of doors.
- **Upside down:** when a rotation about x or z by 180° or a mirror at y turns a block upside down, `half` and the `type` of slabs swap top and bottom.
- The sides of fences, walls, panes and redstone wire turn and mirror with the block.

## Rendering

RENDER_CANVAS(canvas, position, input, tools, output) places the canvas in the world. Its voxel 0,0,0 lands at `position`.
- **Unset voxels** leave the world as it is.
- **Air** removes the block that is there.
- **Blocks already in place:** a voxel whose block is already there in the same states is skipped.

### Rights

The rights are the same as for PLACE_BLOCK and MINE_BLOCK:
- **Without `input`,** the program needs allowBlockChanges or an administrator as owner, otherwise PERMISSION_DENIED. Blocks in the way are replaced.
- **With `input`,** every owner may render:
  - The blocks are taken from `output` first, then from `input`.
  - Blocks in the way are mined with the tools from `tools` and take damage, as with MINE_BLOCK.
  - The removed blocks go to `output`. Without `output`, they are lost.
- **Protected places:** the sheets of the c0de dimension, the spawn protection and blocks that a player couldn't break (e.g. bedrock) can't be changed; they raise PERMISSION_DENIED.

### Check before the first block

RENDER_CANVAS checks everything it can before placing the first block, so that an error doesn't leave half a building:
- rights;
- protected places;
- that all chunks are loaded (CHUNK_NOT_LOADED);
- that the blocks in `input` and `output` are enough (ITEM_MISSING, naming the first block that is missing);
- that there is a fitting tool for every block that has to be removed.

### Block data and missing blocks (0.2.0)

Decided by the user on 2026-11-28 (see [block_review.md](block_review.md)):
- **Block entity data** (the element `data` of BLOCK) is placed with the block: the text of signs, the patterns of banners, the settings of machines.
- **Blocks with inventories keep their contents** (chests, barrels, shulker boxes, furnaces, hoppers, dispensers, lecterns, jukeboxes, decorated pots and the like). Without `input`, the contents are placed as they are in the data. With `input`, they are refilled from the inventories like the blocks: from `output` first, then from `input`.
- **The parameter `contents`** STRING of RENDER_CANVAS decides how:
  - "REQUIRED" (default): every item of the contents must be in the inventories; the check before the first block raises ITEM_MISSING otherwise.
  - "AVAILABLE": the contents are filled with what the inventories hold; what is missing is left out.
  - "NONE": inventories are placed empty.
- **The parameter `blocks`** STRING decides what happens to blocks that the inventories don't hold:
  - "REQUIRED" (default, as in 0.1.0): ITEM_MISSING in the check before the first block.
  - "AVAILABLE": the blocks that are there are placed, the voxels of the missing ones are skipped.
- **The optional result `missing`** HASH INTEGER lists by item id what was left out (blocks and contents), so a program can fetch it and render again: the blocks that are already in place are skipped.
- Both parameters only matter with `input`; without it, everything is placed.
- **What counts as contents:** the items of block entities that are inventories (chests, barrels, shulker boxes with their items, furnaces, hoppers, dispensers, droppers, brewing stands, crafters, chiseled bookshelves, jukeboxes, decorated pots) and the book of a lectern. With `input`, data that would create things out of nothing is left out: the bees of beehives and nests, the item of suspicious sand and gravel, the loot tables of containers (an unopened loot chest would fill itself). The rest of the data (sign text, banner patterns, names, settings of machines) is placed as it is.
- **Data that only administrators may set** (command blocks, structure blocks, jigsaw blocks): as in PLACE_BLOCK of c0de, only processes of administrators place it; for others, RENDER_CANVAS raises PERMISSION_DENIED in the check before the first block.

**Errors during rendering.** A full output inventory (INVENTORY_FULL) or a tool that breaks can only appear during rendering. Then RENDER_CANVAS stops, and the error message says how many blocks were placed.

### Order and speed

- **Order:**
  - From bottom to top, layer by layer.
  - Blocks that hang on others are placed after the other blocks of their layer: torches, ladders, buttons, signs on walls and the like.
  - Sand, gravel and liquids behave as if a player had placed them.
- **Speed:**
  - The process waits while rendering, as with MOVE_ITEMS. It places up to `renderBlocksPerTick` blocks per tick, within the tickBudget of c0de.
  - Blocks that are mined with tools take the time that a player needs with the same tool, divided by `renderMiningSpeed`:
    - 1 (the default) is the speed of a player;
    - higher numbers are faster;
    - 0 breaks blocks instantly, as a player in creative mode does.

    The tools take the usual damage at every speed. Blocks that are replaced without tools (administrators, allowBlockChanges) don't wait.
  - Because a whole canvas is placed by one statement, rendering is faster than PLACE_BLOCK and MINE_BLOCK in a loop even at the speed of a player.
- **Unloaded chunks:** if a chunk is unloaded during rendering, the process waits until it is loaded again.

## Schematics (0.2.0)

Requested by the user on 2026-11-28: a second library, `INCLUDE c0de_paint/schematics`, reads the schematics that players save with [Litematica](https://github.com/sakura-ryoko/litematica) and [Create](https://github.com/Creators-of-Create/Create) into canvases. The review of BLOCK that it needed is in [block_review.md](block_review.md). The decisions of the user are recorded there.

### Files

- **Where:** both mods save schematics in the folder `schematics` of the game folder of the player (Litematica: `<game>/schematics`, configurable; Create: `<game>/schematics`). On a server, that folder is on the computer of the player, not on the server. So the files are read by the **client of the player who runs the process**, like the requests of c0de net: the server sends the name to that client, the client reads the file and sends it to the server in parts of at most 32 KiB, and the server reads it. The process waits on its CALL line meanwhile.
- **Names:** a file is named relative to the folder `schematics`, with `/` between folders, e.g. "castles/keep.litematic". Names that leave the folder (`..`, absolute paths, drive letters) or have characters that hide what they are are refused, as c0de refuses the names of programs on disks.
- **Formats:**
  - `.litematic`, versions 1 to 7. Gzipped NBT with the regions of the schematic; each region has a palette and a packed array of block states (`max(2, bits of palette size − 1)` bits per voxel, spanning two longs where needed; index y·sizeX·sizeZ + z·sizeX + x from the minimum corner of the region). Sizes can be negative; the minimum corner is the smaller of `Position` and `Position + Size ∓ 1` on every axis. Files from before Minecraft 1.13 (version < 5 or data version < 1631) are refused.
  - `.nbt`, the structure files of Minecraft, which Create writes: `size`, `palette` (or the first of `palettes`), `blocks` with `pos`, `state` and `nbt`, `entities`, `DataVersion`.
  - Sponge `.schem` (WorldEdit) isn't asked for; the reader could be added later.
- **Older Minecraft versions:** the palette entries and the block entity data are upgraded with the data fixer of Minecraft (`BLOCK_STATE`, `BLOCK_ENTITY`) from the data version of the file, as Litematica does, so renamed blocks come out with their new names. Files of newer versions are read as they are; blocks the server doesn't know show up in SCHEMATIC_MISSING.
- **Limits:** the server setting `maxSchematicSize` (bytes of the file, 1 KiB to 64 MiB, default 4 MiB) and the bounds of a canvas (`canvasMin`, `canvasMax`); a larger schematic raises SCHEMATIC_TOO_LARGE before anything is drawn.
- **The client decides:** the client setting `allowSchematics` (default true) in `c0de_paint-client.properties`. With false, the functions raise SCHEMATICS_DISABLED.
- **Uploads of Create on the server** (`schematics/uploaded/<player>/` on the server) are not read in 0.2.0 (decided by the user on 2026-11-28); they could be a second source later.

### Functions

All functions take the name of the file and the optional parameter `replace`, a HASH STRING that replaces blocks while the file is read, by the rule of CANVAS_REPLACE (block_review.md, point 4): the key is an id ("create:andesite_casing") or an id with states ("oak_stairs[half=top]"), the value the new block with optional states; the new block keeps the states of the old one that it has. The value "" leaves the voxels not set.

| Function | Parameters | Result |
|---|---|---|
| SCHEMATIC_LOAD | `file` STRING, `replace` HASH STRING (optional), `region` STRING (optional: only this region of a `.litematic`) | `result` CANVAS: the schematic with its block states and block entity data, its minimum corner at voxel 0, 0, 0; the regions of a `.litematic` keep their places to each other. Entities are left out; the optional result `entities` INTEGER counts them. |
| SCHEMATIC_MATERIALS | `file`, `replace` | `result` HASH INTEGER: the items that RENDER_CANVAS takes to build the schematic, by item id (block_review.md, point 5); the optional result `without_item` HASH INTEGER lists the blocks by id that no item places (and that RENDER_CANVAS can't build in survival). |
| SCHEMATIC_MISSING | `file`, `replace` | `result` HASH INTEGER: the blocks by id that the server doesn't have (no such block, or one of a feature that the world hasn't switched on), with their numbers. Empty if the schematic can be built here. Run with the planned `replace` to check it. |
| SCHEMATIC_LIST *(my addition)* | `folder` STRING (optional) | `result` ARRAY STRING: the schematics in the folder and its subfolders, so a program can offer a choice. |

SCHEMATIC_MATERIALS and SCHEMATIC_MISSING work without a canvas, so they also check schematics that are larger than the bounds of a canvas. For a canvas that a program has drawn or changed, `c0de_paint/cpaint` gets CANVAS_ITEMS with the same counting as SCHEMATIC_MATERIALS; CANVAS_MATERIALS keeps counting block ids.

**Errors** (from 1103 on): SCHEMATIC_NOT_FOUND (1103), SCHEMATIC_INVALID (1104: not a schematic, a format or version that isn't supported, an unknown region), SCHEMATIC_TOO_LARGE (1105), CLIENT_UNAVAILABLE (1106: the player who runs the process isn't online or has no c0de paint on the client), SCHEMATICS_DISABLED (1107). Raised by the functions; INVALID_ARGUMENT for a name that isn't allowed and for `replace` values that aren't blocks.

### Runner library

The runner library reads the files from a folder on disk (system property `c0depaint.schematics`, default `schematics`) instead of from a client, and has no registry: SCHEMATIC_MISSING returns the blocks whose namespace isn't `minecraft`, and the data fixer isn't used.

## Configuration

The file `config/c0de_paint.properties` on the server contains:

| Setting | Default | Range | Meaning |
|---|---|---|---|
| canvasMin | −128 | −1024 to 0 | Smallest coordinate of a canvas on every axis. |
| canvasMax | 127 | 0 to 1023 | Largest coordinate of a canvas on every axis. |
| renderBlocksPerTick | 64 | 1 to 1024 | Blocks that RENDER_CANVAS places per tick. |
| renderMiningSpeed | 1 | 0 to 100 | How much faster than a player RENDER_CANVAS mines blocks with tools; 0 means instantly, as in creative mode. |
| canvasVoxelsPerTick | 65536 | 1024 to 1048576 | Voxels that a drawing function, CANVAS_SCALE, CANVAS_ROTATE or CANVAS_MIRROR processes per tick. Larger operations are spread over several ticks, while the process waits. The shape is computed in the first tick. The other canvas operations work in one tick. |
| maxSchematicSize | 4194304 | 1024 to 67108864 | The largest schematic file (bytes) that the functions of `c0de_paint/schematics` read (0.2.0). |

## Changes to c0de

c0de paint needs these additions to c0de, made in sprint 19 of c0de (c0de 0.7.0; see [Library type](https://github.com/joernott/minecraft-c0de/blob/main/docs/concept/04-data.md#library-type) in the concept of c0de):

- **Library types.** A library can define types of its own besides structs. A library type has:
  - a name;
  - a default value;
  - copy-on-write copies;
  - a comparison;
  - a text for SAY, displays and the debugger (e.g. "CANVAS 20×10×8, 1600 voxels");
  - saving and loading with the cursor, in a compact form, so that canvases survive a server restart.

  c0de has no memory limits per process; c0de paint limits canvases itself with `canvasMin` and `canvasMax`.
- **Conversions.** Converting a library type to YAML or JSON raises an error.
- **Work in parts.** A library function keeps its progress while the cursor stays on its CALL line (`call.state()`), so canvas operations can be spread over several ticks.

c0de net doesn't need to change: the interface of the libraries only gets additions.

## Runner library and tests

- **Runner library:**
  - Like c0de net, c0de paint has a runner library for the program runner of c0de, so that c0de.at can test its recipes without Minecraft.
  - In the runner, RENDER_CANVAS writes the placed blocks layer by layer to a YAML file, which the tests compare with the expected result.
- **Tests:**
  - Unit tests cover drawing, styles and canvas operations.
  - GameTests cover rendering in the world.
