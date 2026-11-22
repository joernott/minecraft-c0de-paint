# c0de paint

**Draw with code, build in blocks.** c0de paint adds the library `c0de_paint/cpaint` to [c0de](https://modrinth.com/mod/c0de), the programming language made of blocks. Your programs draw lines, flat shapes and bodies into canvases, combine, scale, rotate and mirror them, and build them in the world: a tower with a pointed roof, a dome of glass, an arch in a tilted wall, or a path of random stones from a chest. The name is a wink at Deluxe Paint, which everybody called DPaint.

> This is an early version. Expect rough edges, and please report bugs and ideas on GitHub.

## Features

- **Canvases**: a three-dimensional grid of voxels that grows with its content. Every voxel is empty or holds a block with its states; air is a block too, so a drawing can carve out what is in the world.
- **Styles**: draw a line, a face or the inside of a shape with nothing, air, one block, random blocks by weight, a gradient (spheres, layers or cylinders, with fuzzy borders) or a repeating pattern. INVENTORY_BLOCKS takes the blocks and their weights from a chest.
- **Flat shapes in any plane**: lines, rectangles, polygons, ellipses, arcs and pie slices, also tilted, one voxel thick and without holes, and spirals that rise around an axis, with a ramp like a spiral staircase.
- **Bodies**: boxes, spheres and ellipsoids, cylinders, cones, pyramids and the Platonic solids: tetrahedron, octahedron, dodecahedron and icosahedron. Each has its own style for edges, faces and inside, so a box is full, hollow or just a frame.
- **Canvas operations**: count, read, clear, crop, move, replace and merge canvases, list the materials, and scale, rotate by any angle and mirror them. Stairs, logs, doors, rails and signs turn with the building.
- **Rendering**: RENDER_CANVAS builds a canvas in the world, from bottom to top, with torches and ladders after the walls they hang on. Operators build directly; every other player builds with blocks from a chest, mines what is in the way with the tools of a second chest and puts the drops into a third. Everything is checked before the first block, so an error doesn't leave half a building. CANVAS_FROM_WORLD copies a part of the world into a canvas.
- **No freezing**: large drawings and rendering are spread over several ticks while the server and all other programs go on.
- **Errors you can catch**: a voxel outside the limits, an empty canvas, points that don't define a plane, missing blocks or tools each raise an error of their own.
- **Settings**: the server limits the size of canvases, the blocks placed per tick, the mining speed and the work done per tick.

## Example

```
PROGRAM tower
NOOP
INCLUDE c0de/minecraft
INCLUDE c0de_paint/cpaint
NOOP
FUNCTION main
    PARAMETER at STRUCT c0de/minecraft/POSITION
BEGIN
    VARIABLE c TYPE c0de_paint/cpaint/CANVAS
    VARIABLE base STRUCT c0de_paint/cpaint/POINT
    VARIABLE bricks STRUCT c0de_paint/cpaint/BLOCK
    VARIABLE walls TYPE c0de_paint/cpaint/STYLE
    VARIABLE empty TYPE c0de_paint/cpaint/STYLE
    ASSIGN bricks.id "stone_bricks" ;
    CALL c0de_paint/cpaint/STYLE_SOLID
        PARAMETER block bricks ;
        RESULT result walls
    CALL c0de_paint/cpaint/STYLE_EMPTY
        RESULT result empty
    CALL c0de_paint/cpaint/DRAW_CYLINDER
        PARAMETER canvas c ;
        PARAMETER center base ;
        PARAMETER r 4 ;
        PARAMETER h 12 ;
        PARAMETER direction "+y" ;
        PARAMETER side_style walls ;
        PARAMETER fill_style empty ;
    CALL c0de_paint/cpaint/RENDER_CANVAS
        PARAMETER canvas c ;
        PARAMETER position at ;
END FUNCTION
```

## Documentation

- [Handbook](https://github.com/joernott/minecraft-c0de-paint/blob/main/docs/reference/paint.md) ([Deutsch](https://github.com/joernott/minecraft-c0de-paint/blob/main/docs/de/reference/paint.md)): the types, every style, drawing function and canvas operation, rendering, the errors, the settings and complete examples
- [Concept](https://github.com/joernott/minecraft-c0de-paint/blob/main/docs/concept.md): how canvases, styles and rendering work

## Requirements

- [c0de](https://modrinth.com/mod/c0de) 0.7.0 or newer, for the same Minecraft version and loader
- Minecraft 1.21.1, 1.21.11, 26.1.2, 26.2 or 26.3 (26.x needs Java 25)
- **Fabric** with [Fabric API](https://modrinth.com/mod/fabric-api), or **NeoForge**
- On the server **and** on the clients, like c0de.
- The handbook is in the game too, as a binder of the handbooks of c0de, crafted from an INCLUDE block surrounded by eight dyes in the colours of the rainbow.
- The runner library `c0de-paint-runner-<version>.jar` on GitHub adds the library to the program runner of c0de, so programs can be tested without Minecraft.
