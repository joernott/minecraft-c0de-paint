# Changelog

## 0.1.0

The first version of c0de paint, for Minecraft 1.21.1, 1.21.11, 26.1.2, 26.2 and 26.3 on Fabric and NeoForge. It requires c0de 0.7.0 or later.

- The library `c0de_paint/cpaint` with the types CANVAS and STYLE and the structs POINT and BLOCK.
- Styles: STYLE_NONE, STYLE_EMPTY, STYLE_SOLID, STYLE_RANDOM, STYLE_GRADIENT, STYLE_PATTERN and INVENTORY_BLOCKS.
- Drawing: DRAW_BLOCK, DRAW_LINE, DRAW_RECTANGLE, DRAW_POLYGON, DRAW_ELLIPSE, DRAW_ARC, DRAW_BOX, DRAW_ELLIPSOID, DRAW_CYLINDER, DRAW_CONE, DRAW_PYRAMID and the Platonic solids, in any plane.
- Canvas operations: CANVAS_COUNT, CANVAS_MIN, CANVAS_MAX, CANVAS_GET, CANVAS_CLEAR, CANVAS_CROP, CANVAS_MOVE, CANVAS_REPLACE, CANVAS_MERGE, CANVAS_MATERIALS, CANVAS_SCALE, CANVAS_ROTATE and CANVAS_MIRROR, with the states of blocks.
- Rendering: RENDER_CANVAS with blocks and tools from inventories, and CANVAS_FROM_WORLD.
- The handbook in English and German, as a binder of the handbooks of c0de.
- The runner library `c0de-paint-runner-0.1.0.jar` for the program runner of c0de.
