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

## Configuration

The file `config/c0de_paint.properties` on the server contains:

| Setting | Default | Range | Meaning |
|---|---|---|---|
| canvasMin | −128 | −1024 to 0 | Smallest coordinate of a canvas on every axis. |
| canvasMax | 127 | 0 to 1023 | Largest coordinate of a canvas on every axis. |
| renderBlocksPerTick | 64 | 1 to 1024 | Blocks that RENDER_CANVAS places per tick. |
| renderMiningSpeed | 1 | 0 to 100 | How much faster than a player RENDER_CANVAS mines blocks with tools; 0 means instantly, as in creative mode. |
| canvasVoxelsPerTick | 65536 | 1024 to 1048576 | Voxels that a drawing function, CANVAS_SCALE, CANVAS_ROTATE or CANVAS_MIRROR processes per tick. Larger operations are spread over several ticks, while the process waits. The shape is computed in the first tick. The other canvas operations work in one tick. |

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
