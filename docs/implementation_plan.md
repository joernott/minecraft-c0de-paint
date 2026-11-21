# Implementation plan

This plan implements the [c0de paint concept](concept.md) in sprints. Each sprint has a goal, numbered tasks and a result that can be demonstrated. The rules are the same as in [c0de](https://github.com/joernott/minecraft-c0de/blob/main/docs/implementation_plan.md) and [c0de net](https://github.com/joernott/minecraft-c0de-net/blob/main/docs/implementation_plan.md).

## Principles

- **The same versions as c0de from the start.** Every sprint delivers all nodes of c0de: 1.21.1, 1.21.11, 26.1.2, 26.2 and 26.3, each for Fabric and NeoForge. The build (Stonecutter, Loom, ModDevGradle) is taken over from c0de net.
- **Plain Java where possible.** The module `core` contains everything that doesn't need Minecraft:
  - the canvas and the styles;
  - the drawing functions and the transformations;
  - the plan of a rendering.

  It is developed test-first with JUnit.
- **Thin Minecraft layer.** The mod registers the library, reads inventories and the world, places and mines blocks, and turns block states.
- **Every sprint ends with a working mod.** CI is green on all nodes.

## Branching and commits

- `sprint-0` contains the concept and this plan and shares its commit with `main`.
- Every sprint n gets a branch `sprint-n`, created from the tip of `sprint-(n-1)`. It contains exactly the commits of that sprint.
- Commit messages start with the task they close: `Task 3.2: Store canvases in sections with a palette`.
- At the end of a sprint, the branch is merged into `main` once CI is green.
- Every prompt is logged in [zz_prompts.md](zz_prompts.md).

## Tracking

- Completed tasks are marked with ✅ in the column "Done" of their sprint table. A sprint is marked as done in the sprint overview when all of its tasks are done.
- If an additional task turns out to be necessary to complete a task, it is added below that task with the next level number: an additional task for 1.1 becomes 1.1.1.

## Definition of done

A task is done when
- the code works on all nodes;
- unit tests (`core`) or GameTests (mod) cover the behavior described in the concept, including the error cases;
- CI is green;
- the concept is updated if the implementation required a decision that the concept doesn't cover yet.

## Sprint overview

| Sprint | Goal | Result | Done |
|---|---|---|:---:|
| 0 | Concept and plan | Concept, review and implementation plan | ✅ |
| 1 | Library types in c0de | c0de 0.7.0: libraries can define types of their own (sprint 19 of c0de) |✅ |
| 2 | Project skeleton | The mod loads on all nodes with c0de 0.7.0; programs can include `c0de_paint/cpaint`; CI and VM are ready |  |
| 3 | Canvas and styles | Programs declare canvases and styles, set and read voxels, and merge canvases |  |
| 4 | 2D shapes | Lines, rectangles, polygons, ellipses and arcs in any plane |  |
| 5 | Bodies | Boxes, ellipsoids, cylinders, cones, pyramids and the Platonic solids |  |
| 6 | Transformations | Scaling, rotating and mirroring, with block states, spread over several ticks |  |
| 7 | Rendering | RENDER_CANVAS puts canvases into the world; canvases are copied from the world |  |
| 8 | Documentation and release 0.1.0 | Handbook in English and German, runner library, release, c0de.at |  |

## Sprint 0: Concept and plan

| Task | Description | Done |
|---|---|:---:|
| 0.1 | Concept: types, styles, drawing functions, canvas operations, rendering, configuration, changes to c0de. | ✅ |
| 0.2 | Review of the concept with the user in two rounds ([concept_review.md](concept_review.md)). | ✅ |
| 0.3 | Implementation plan; sprint 19 of c0de for the library types. | ✅ |

## Sprint 1: Library types in c0de

**Goal:** c0de can do everything that c0de paint needs from it. The work is done in the repository of c0de as its sprint 19 and released as c0de 0.7.0; this sprint only tracks it.

| Task | Description | Done |
|---|---|:---:|
| 1.1 | Sprint 19 of c0de is done, and c0de 0.7.0 is released: library types, their saving with the cursor, functions of other mods that wait for several ticks   Done on 2026-11-21: c0de 0.7.0 is released on GitHub, Modrinth and CurseForge. | ✅ |

## Sprint 2: Project skeleton

**Goal:** the empty mod builds, loads and is tested on all nodes, like c0de net.

| Task | Description | Done |
|---|---|:---:|
| 2.1 | **Build taken over from c0de net:**<ul><li>Stonecutter with the ten nodes;</li><li>the `core` module;</li><li>the Gradle wrapper;</li><li>Spotless and the whitespace check;</li><li>`.gitignore` and `.gitattributes`;</li><li>license BSD 3-Clause.</li></ul>   Done on 2026-11-21: copied from c0de net without HTTP, mail, client and runner; the release workflows follow with the first release (8.3). | ✅ |
| 2.2 | **c0de as a dependency:**<ul><li>For every node, from the Modrinth Maven repository.</li><li>Built and tested with `deps.c0de`.</li><li>Required as `>= deps.c0de_min` and `< the next major version` in `fabric.mod.json` and `neoforge.mods.toml`.</li></ul>   Done on 2026-11-21: from the GitHub releases of c0de, as in c0de net (c0de is not on the Modrinth Maven repository); `deps.c0de` 0.7.0, required `>=0.7.0 <1.0.0`. | ✅ |
| 2.3 | **Entry points:**<ul><li>Mod entry points for Fabric and NeoForge.</li><li>The library `c0de_paint/cpaint` is registered with the types CANVAS and STYLE and the structs POINT and BLOCK.</li><li>The configuration `config/c0de_paint.properties` with its four settings.</li></ul>   Done on 2026-11-21: decided that `core` contains the library itself, compiled against the interpreter of c0de (its runner jar), so programs with the library run in the unit tests; what needs the world becomes an interface of `core`. The canvas storage of 3.2 came with the type CANVAS. Five settings (with `canvasVoxelsPerTick`). | ✅ |
| 2.4 | **GameTests on every node:**<ul><li>The library is registered.</li><li>A program with `INCLUDE c0de_paint/cpaint` and `VARIABLE c TYPE c0de_paint/cpaint/CANVAS` is valid.</li></ul>   Done on 2026-11-21: `libraryIsRegistered` and `programsIncludeTheLibrary` pass on all ten nodes on the VM. | ✅ |
| 2.5 | **CI and VM:**<ul><li>GitHub Actions: checks, unit tests (Linux and Windows), build and GameTests per node.</li><li>Dependabot.</li><li>A clone on the VM builds and tests all nodes.</li><li>`docs/development_environment.md`.</li></ul>   On 2026-11-21: workflow `build.yml` without the handbook, runner and client jobs; Dependabot; the clone on the VM builds and tests all nodes; `docs/development_environment.md`. Open: the first CI run, after the branch is pushed. |  |

## Sprint 3: Canvas and styles

**Goal:** programs can declare canvases and styles, set and read single voxels, and combine canvases.

| Task | Description | Done |
|---|---|:---:|
| 3.1 | POINT and BLOCK; the errors of the library (CANVAS_LIMIT, CANVAS_EMPTY, NOT_A_PLANE and invalid arguments) as constants with messages.   Done on 2026-11-21: conversions in `Args`; the errors in `PaintError` (CANVAS_LIMIT 1100, CANVAS_EMPTY 1101, NOT_A_PLANE 1102, from 1100 on because c0de net uses 1000 to 1099) as constants. | ✅ |
| 3.2 | **The canvas in `core`:**<ul><li>Voxels in sections of 16³, each with a palette of blocks; only sections with content take memory.</li><li>Copies are copy on write.</li><li>The bounds.</li><li>The limits from the configuration (CANVAS_LIMIT draws nothing).</li><li>Comparison, the text for PRINT and the debugger.</li><li>Compact saving with the cursor; a canvas survives a server restart.</li></ul>   Done on 2026-11-21: `Canvas` with sections, palettes, copy on write, bounds, `Limits`, text, equality and saving as compressed Base64; unit tests `CanvasTest`, saving with the cursor in `CanvasFunctionsTest`. | ✅ |
| 3.3 | **Styles:**<ul><li>STYLE_NONE, STYLE_EMPTY, STYLE_SOLID and STYLE_RANDOM.</li><li>Weights of 1 or more.</li><li>Random numbers from the process, so a fixed seed gives the same drawing.</li></ul>   Done on 2026-11-21: STYLE_NONE and STYLE_EMPTY are functions without parameters (c0de has no constants of library types); ids without namespace get `minecraft:`. | ✅ |
| 3.4 | **STYLE_GRADIENT:**<ul><li>`axes`, `from` and `to`.</li><li>The shares of the weights; the examples 5/3/2 and 10/6/4 of the concept are unit tests.</li><li>The last block beyond the end.</li><li>`fuzzy` 0 to 3.</li></ul>   Done on 2026-11-21: without `to`, the length is measured to the farthest voxel of the part; the examples are unit tests (`StyleTest`). | ✅ |
| 3.5 | STYLE_PATTERN: the pattern repeated along all axes, aligned at `from`, gaps not drawn.   Done on 2026-11-21: the style keeps a copy of the pattern. | ✅ |
| 3.6 | **Canvas functions:**<ul><li>DRAW_BLOCK.</li><li>CANVAS_COUNT, CANVAS_MIN, CANVAS_MAX, CANVAS_GET.</li><li>CANVAS_CLEAR, CANVAS_CROP, CANVAS_MOVE, CANVAS_REPLACE.</li><li>CANVAS_MERGE: one canvas or an array, the modes OVERWRITE and KEEP, the offset.</li><li>CANVAS_MATERIALS.</li></ul>   Done on 2026-11-21: `CanvasFunctionsTest`; CANVAS_REPLACE and CANVAS_MATERIALS ignore the states of blocks. | ✅ |
| 3.7 | INVENTORY_BLOCKS in the mod: slot order, counts as weights, a block in several slots listed once, items that aren't blocks ignored. GameTests with chests.   Done on 2026-11-21: through the interface `PaintWorld` of `core`, implemented by `MinecraftWorld`; GameTest `inventoryBlocksFollowTheSlots` on all ten nodes on the VM. | ✅ |

## Sprint 4: 2D shapes

**Goal:** lines and flat shapes can be drawn in any plane.

| Task | Description | Done |
|---|---|:---:|
| 4.1 | **Rasterization in `core`:**<ul><li>3D lines that connect through edges and corners.</li><li>A voxel belongs to a shape when its center is inside.</li><li>Lines and faces one voxel thick.</li><li>Planes from three points (NOT_A_PLANE).</li><li>The fallback of STYLE_NONE inward (line → face → inside).</li></ul>   Done on 2026-11-21: `Raster` (lines, planes, flat regions column by column along the steepest axis of the plane, bodies, surfaces) and `Painter` (inside, faces, lines in this order; limits checked before the first voxel). Unit tests `ShapesTest`. | ✅ |
| 4.2 | DRAW_LINE and DRAW_RECTANGLE.   Done on 2026-11-21: `c` only gives the distance of the second side. | ✅ |
| 4.3 | DRAW_POLYGON: closed automatically, the plane from the first three points not on one line, other points projected onto it.   Done on 2026-11-21. | ✅ |
| 4.4 | DRAW_ELLIPSE and DRAW_ARC: semi-axes from points, angles in degrees, the pie slice of the fill style. Tests with tilted planes (such as a circle tilted by 10°).   Done on 2026-11-21: the angles of DRAW_ARC are those of the parameter of the ellipse, so the pie slice ends on the arc; the line of an arc is the curve only. A circle tilted by about 10° is a unit test. | ✅ |

## Sprint 5: Bodies

**Goal:** all bodies of the concept can be drawn.

| Task | Description | Done |
|---|---|:---:|
| 5.1 | DRAW_BOX with edges, sides and inside.   Done on 2026-11-21: the faces are the voxels of the body with a neighbour outside through a face (`Raster.surface`). | ✅ |
| 5.2 | DRAW_ELLIPSOID with face and inside.   Done on 2026-11-21: a radius of 0 makes the ellipsoid flat on that axis. | ✅ |
| 5.3 | DRAW_CYLINDER and DRAW_CONE: the six directions, rims as lines.   Done on 2026-11-21: a cylinder has h layers (0 to h−1), the tip of a cone lies at h; the rims are the outlines of the end layers. | ✅ |
| 5.4 | DRAW_PYRAMID with a square base.   Done on 2026-11-21. | ✅ |
| 5.5 | DRAW_TETRAHEDRON, DRAW_OCTAHEDRON, DRAW_DODECAHEDRON and DRAW_ICOSAHEDRON: corners at the distance r, fixed orientations (pictures for the handbook).   Done on 2026-11-21: the faces are found as the planes through three corners that have all corners on one side; the orientations are the classic ones (tetrahedron in alternate corners of a cube, octahedron on the axes, icosahedron and dodecahedron with the golden ratio). The pictures follow with the handbook (8.1). | ✅ |

## Sprint 6: Transformations

**Goal:** canvases can be scaled, rotated and mirrored without stalling the server.

| Task | Description | Done |
|---|---|:---:|
| 6.1 | CANVAS_SCALE: percentages of 1 or more, around `center`, nearest voxel.   Done on 2026-11-21: every voxel of the box of the result takes the nearest voxel of the source; the result is built in a canvas of its own, so an error (CANVAS_LIMIT) leaves the canvas unchanged. | ✅ |
| 6.2 | CANVAS_ROTATE: any angle about x, y or z, every target voxel takes the nearest source voxel (no holes, no doubled voxels).   Done on 2026-11-21: right-hand rule (+90° about y turns east to north); exact for multiples of 90°; a filled square turned by 30° has no holes (unit test). | ✅ |
| 6.3 | CANVAS_MIRROR at a plane, by default at the middle of the bounds.   Done on 2026-11-21: `position` may be FLOAT, e.g. 4.5 for the plane between two voxels. | ✅ |
| 6.4 | **Block states in the mod:**<ul><li>When rotating: `facing`, `axis`, `rotation` and the shapes of stairs and rails, rounded to 90° (16 steps for `rotation`).</li><li>When mirroring: the same states, and door hinges.</li><li>A state the block can't take stays unchanged.</li><li>GameTests with stairs, logs and doors.</li></ul>   Done on 2026-11-21: the new states are computed in `core` (`BlockStates`, also `half`, the `type` of slabs and the sides of fences); the mod checks them against the state definition of the block (`MinecraftWorld.valid`). GameTest `statesTurnAndMirror` on all ten nodes on the VM. | ✅ |
| 6.5 | Canvas operations and drawing functions process at most `canvasVoxelsPerTick` voxels per tick; the process waits for the rest. A GameTest with a large canvas checks that the ticks stay within the budget.   Done on 2026-11-21: `Job` with `call.state()`; the drawing functions and the three transformations; the other canvas operations work in one tick (decision, in the concept). Checked by the unit test `largeDrawingsAreSpreadOverSeveralTicks` instead of a GameTest: it counts the steps of the cursor. | ✅ |

## Sprint 7: Rendering

**Goal:** canvases come into the world and out of it.

| Task | Description | Done |
|---|---|:---:|
| 7.1 | **The plan of a rendering in `core`:**<ul><li>Bottom to top, layer by layer.</li><li>Blocks hanging on others after the other blocks of their layer.</li><li>Voxels already in place are skipped.</li><li>The needs in blocks and tools.</li></ul> |  |
| 7.2 | **The checks before the first block:**<ul><li>Rights, as in PLACE_BLOCK and MINE_BLOCK (PERMISSION_DENIED).</li><li>Protected places: sheets, spawn protection, unbreakable blocks.</li><li>Loaded chunks (CHUNK_NOT_LOADED).</li><li>Enough blocks in `output` and `input` (ITEM_MISSING with the first missing block).</li><li>A fitting tool for every block to remove.</li></ul> |  |
| 7.3 | **RENDER_CANVAS:**<ul><li>Blocks from `output` first.</li><li>Mining with tools, with damage and the speed `renderMiningSpeed`.</li><li>`renderBlocksPerTick` within the tickBudget.</li><li>Liquids and falling blocks as placed by a player.</li><li>INVENTORY_FULL and broken tools during rendering, with the number of placed blocks.</li><li>Waiting for unloaded chunks.</li></ul><br>GameTests for administrators, allowBlockChanges and players with inventories. |  |
| 7.4 | CANVAS_FROM_WORLD: the region between two POSITIONs, air as not set or as air, no block data, CHUNK_NOT_LOADED. |  |

## Sprint 8: Documentation and release 0.1.0

**Goal:** c0de paint is documented, tested on c0de.at and published.

| Task | Description | Done |
|---|---|:---:|
| 8.1 | **Handbook:**<ul><li>The handbook of `c0de_paint/cpaint` as a binder of c0de's handbooks, in English and German.</li><li>Pictures of the shapes, styles and Platonic solids.</li><li>README and CHANGELOG.</li></ul> |  |
| 8.2 | **Runner library for the program runner of c0de:**<ul><li>The library without Minecraft, `c0de-paint-runner-<version>.jar`.</li><li>RENDER_CANVAS writes the placed blocks layer by layer to a YAML file, which the tests compare with the expected result.</li></ul> |  |
| 8.3 | **Release 0.1.0 for all nodes:**<ul><li>GitHub, Modrinth (environment "client and server") and CurseForge.</li><li>Only with the consent of the user, who pushes the tag.</li></ul> |  |
| 8.4 | **c0de.at (in www-c0de, as its own sprint there):**<ul><li>The documentation of c0de 0.7.0 with the library types.</li><li>The new library `c0de_paint/cpaint` with its handbook in English and German.</li><li>Recipes for c0de paint, tested with the runner library.</li><li>Screenshots.</li></ul> |  |

## Risks

| Risk | Mitigation |
|---|---|
| Large canvases take a lot of memory and make the saved cursors big. | Sections with a palette, only for content; compact saving; the limits of the configuration. |
| Rotating by any angle makes shapes look ragged. | Nearest-voxel sampling; the handbook recommends drawing 2D shapes in tilted planes instead of rotating them. |
| Block states can't be rotated about x and z with the means of Minecraft, which only rotates about y. | Own mapping of `facing`, `axis` and `half`; states that don't fit stay unchanged (best effort, decided in the review). |
| Rendering big canvases lags the server. | `renderBlocksPerTick` with a maximum of 1024, inside the tickBudget. |
| c0de 0.7.0 changes the interface of libraries. | Only additions; c0de net keeps working without changes. |
