# Concept review (2026-11-21)

This file records the review of [concept.md](concept.md). The concept has been rewritten with the answers to the first review. This file lists the decisions taken, the gaps that were filled with proposals, and the questions that are still open. Spelling and grammar fixes are not listed. Revert a proposal in the concept if it doesn't match your intent.

## 1. Decisions from the first review

- **Library types:** c0de gets library types besides structs; CANVAS and STYLE are library types. This means a sprint in c0de. c0de net doesn't need changes.
- **Optional parameters:** the gradient's "omitted coordinates" become the parameter `axes`.
- **Coordinates:** in the world, coordinates are POSITIONs; in a canvas, they are integers.
- **Names:** the constants are STYLE_NONE and STYLE_EMPTY. The other functions follow the same prefixes: STYLE_…, DRAW_…, CANVAS_….
- **Rights:** the rights and protected places are the same as for PLACE_BLOCK and MINE_BLOCK.
- **Speed:**
  - Rendering is configurable in blocks per tick, with a maximum.
  - Canvas operations are spread over several ticks (see Q2 for the client).
- **Seed:** random numbers use the seed of the process.
- **Runner library:** c0de paint gets one.
- **2D shapes:**
  - They lie in a plane given by three points.
  - The defining points are integers. With angles like 10°, they are rounded, which is good enough.
- **Rasterization:** as proposed.
  - An ellipsoid has only a face and an inside.
  - The lines of a cylinder are its rims.
- **Bodies:**
  - Tetrahedron, octahedron, dodecahedron and icosahedron.
  - Also a four-sided pyramid, a cone and an arc.
  - No functions of their own for the cube and the sphere.
- **Canvas operations:**
  - The scaling function is called CANVAS_SCALE.
  - Rotation works by any angle, without holes, with block states rounded to 90°.
  - Mirroring flips the block states.
  - Merging has an optional offset and the modes OVERWRITE and KEEP.
- **Styles:**
  - Weights must be 1 or more.
  - A gradient goes through the blocks in their order, and the weights set the shares of its length (stretched).
  - The distance is measured in a straight line.
  - Beyond `to`, the last block is used.
- **Rendering:**
  - It checks everything first.
  - It places blocks from bottom to top.
  - Liquids and falling blocks behave as if a player had placed them.
  - Blocks from the output inventory are used first.
- **Project:**
  - The handbook in English and German.
  - Modrinth "client and server".
  - The dependency range up to the next major version of c0de.
- **Canvas size:** −128 to 127 per axis by default, configurable.

## 2. Gaps filled with proposals

- **POINT and BLOCK structs.**
  - **POINT:** a struct of three integers, instead of three separate parameters, because polygons need an array of points and planes need three points.
  - **BLOCK:** a block with its block states and weight is a struct. Programs can then write lists of blocks by hand, and only STYLE is a library type.
- **The canvas grows by itself.**
  - **Drawing:** functions draw into a canvas passed by reference, and the canvas grows with its content. Both of your wishes are met: no fixed size and no copies.
  - **Declaring:** `DIM c AS CANVAS` gives an empty canvas, so no CANVAS_CREATE is needed.
  - **The limit:** drawing beyond the limit raises CANVAS_LIMIT and draws nothing.
- **Styles fall back inward.** A part with STYLE_NONE takes the style of the next part inward (line → face → inside). A box with only a fill style is therefore full, and a box with only a side style is hollow.
- **Shapes defined by points.**
  - **Ellipse and arc:** they are given by a center and two points (the ends of the semi-axes), not by rx and ry, so that they can lie in any plane.
  - **Rectangle:** it is given by three points.
  - **Polygon:** its points are projected onto the plane of its first three points.
- **One function for inventories.** INVENTORY_BLOCKS(position) returns the blocks of an inventory as an array. STYLE_RANDOM and STYLE_GRADIENT use it, which replaces CREATE_RANDOM_STYLE and CREATE_GRADIENT.
- **A formula for the gradient.**
  - **The rule:** a voxel at distance d gets the block whose share contains (d + ½) · W / (L + 1).
  - **Check:** this gives exactly your examples: 5/3/2 for 10 voxels and 10/6/4 for 20 voxels.
- **New canvas functions:**
  - CANVAS_COUNT, CANVAS_MIN, CANVAS_MAX and CANVAS_GET.
  - CANVAS_CLEAR, CANVAS_CROP, CANVAS_MOVE and CANVAS_REPLACE.
  - CANVAS_MATERIALS and CANVAS_FROM_WORLD.
- **No center restriction for rotation.** The rotation center no longer has to lie inside the canvas, because canvases have no fixed range.
- **Rendering details:**
  - Voxels whose block is already in place are skipped.
  - Blocks that hang on others (torches, ladders) come after the other blocks of their layer.
  - Without an output inventory, removed blocks are lost.
  - Errors during rendering say how many blocks were placed.
  - An unloaded chunk makes the process wait.
- **The changes to c0de:** library types, their conversions, and waiting library functions.

## 3. Questions of the second review

All questions are answered (2026-11-21); the answers are in the concept and in the [implementation plan](implementation_plan.md).

**Q1: Library name.**
- **Asked:** libraries are called `mod/library`, so `c0de_paint` alone doesn't fit. Which library name?
- **Answer:** `c0de_paint/cpaint`, a wink at Deluxe Paint (DPaint).

**Q2: Canvas operations on the client or on the server?**
- **Proposal:** on the server, spread over several ticks (`canvasVoxelsPerTick`). Programs run while their owner is offline, a client can't be trusted, and a client protocol would be a lot of work.
- **Answer:** yes.

**Q3: Mining while rendering.**
- **Proposal:** rendering doesn't wait for the break time, and the tools take damage.
- **Answer:** the speed is configurable with `renderMiningSpeed`:
  - 1 (the default) is the speed of a player;
  - higher numbers are faster;
  - 0 is instant, as in creative mode.

  The tools take damage at every speed.

**Q4: The order of the implementation.**
- **Proposal:**
  - Library types in c0de first.
  - Then c0de paint: canvas and styles, 2D shapes, bodies, transformations, rendering, handbook and release.
- **Answer:** agreed. A task updates c0de.at for the new version of c0de and for c0de paint.
