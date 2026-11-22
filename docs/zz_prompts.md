# Prompts

This file collects all prompts used to work on this repository.

## 2026-11-21 – Sprint 0: concept review

I have added a new minecraft-c0de-paint repository, that will contain another library for c0de. Please review the docs/concept.md document for it

## 2026-11-21 – Answers to the review

1: Fit with c0de
1: Creating a fixed canvas introduces problems when drawing out of bounds, while a canvas dynamically created by a function will be sized to its content. similarly, it allows canvasses with different sets of coordinates to be placed next to each other if the coordinates inside don't overlap. I would like to give libraries to add new types instead of structs, even if that means reworking c0de and c0de-net. 
2: then define an additional parameter "axes" or similar as needed
3: coordinates in the world are always of the type POSITION, coordinates in canvasses are integers.
4. c0de_paint is the name I want to go for, for the styles, we should use constants like STYLE_NONE to prevent confusion with something else that might be named "NONE"
5: yes, similar behaviour
6. Rendering should be faster than a lot of MINE_BLOCK and PLACE_BLOCK operations, The speed should be configurable within the tickBudget. Canvas operations can be spread out to minimize load impact on client and server. Ideally, they happen on the client to minimize the impact in multiplayer 
7. Yes, using the same seed is ideal for reproducible results. Having a runner library is a good idea, please implement it
2: Drawing
* Planes: Yes, add a plane parameter. People can rotate a canvas if they don't want them flat
* Rasterization: follow your suggestions
* Styles per shape: An ellipsoid does not have a line, it only has an "inside" to fill and a face. The rims of a cylinder are the lines.
* Platonic solids: leave the cube out, as well as sphere as it is covered by ellipsoid, TETRAHEDON, OCTAHEDRON, DODECAHEDRON, ICOSAHEDRON should be in it but having four sided pyramids, cones  and arcs is useful, so please add all of them and add a radius parameter where needed.

3: Canvas operations:
* Yes, use SCALE
* We should either allow free rotations with a cleanup of duplicate voxels and accepting holes or we should be able to draw 2d shapes not only on planes defined by "xz" etc but also by a point ad vector. If I want a circle rotated by 10 degrees, paint should help me with that. It does not need to be perfect but is better than calculating everything by hand and placing blocks manually.
* Block states should align with the closest 90 degree angle when rotating and flip when mirroring.
* Merging two not overlapping canvasses should be possible but defining an offset is a good idea but it should be optional
* Use overwrite and keep
* The definition of none and empty is correct

4: Styles:
* Inconsistencies: I overlooked empty then in the first mention, but it is useful to create hollow objects with thick borders
* Own block struct: Yes, a block list is its own struct. I prefer "style" to be a new type defined by the library (see above), so,maybe block list can be one too
* Random: weight 0 should not be allowed. When creating a block list using the functions, you can't place 0 minecraft:stone into an inventory. The sum does not make sense to me as none of the blocks will have that value.
* Gradient: I got confused when writing this. You use the order of blocks but the weight determines the size of similar blocks: If I have 5 black concrete, 3 gray concrete and 2 white concrete in that order, (10 elements), the gradient should start with 5 black concrete before placing 3 gray and tehn finally two white blocks when I draw a 10 block line.
* Distance is measured in a straight line
* Gradients are "stretched" to fill the range between from and to coordinates. Using the above example, a 20 block line would start with 10 black blocks, followed by 6 gray and 4 white blocks, if no "to" coordinates are defined. If I define those coordinates, the gradient is stretched up to that coordinate and then, the remaining part of the shape is filled with the last element.
* Gradient order: Yes, that was my intention
* Non-blocks: Yes, ignoring them is the right choice

5. Rendering:
* none and empty: yes
* Supports: yes, from bottom to top
* Liquids and falling blocks: They will behave the same way as if I placed them.
* Errors: Yes, checking before makes sense
* Output inventory first: yes

6. Project
yes to all 3

7: Thank you, I hate spelling and grammatical errors

Decisions:
1: Yes, drawing into the same canvas is a good idea
2: The maximum canvas size should be configurable with a default of -128 to +127 in every dimension. Blocks per tick should be configurable  in the config (Similar to the MOVE_ITEM function). There should be a maximum for that config to avoid lag, though
3: I would prefer to be able to draw them in a 3 dimensional space, so defining a plane by 3 points would be ideal
4: By any angle with best effort for matching block states (see above)
5, 6,7: See above

## 2026-11-21 – Answers to the second review and implementation plan

1: c0de_paint/cpaint would be a subtle wink towards the famous Deluxe Paint (usually called dpaint)
2: Yes
3: This should be a configurable number. The default should be to use the players mining speed (1), but higher numbers allow for greater speed. Setting this to 0 would be as fast as a player breaks blocks in creative.
4: That sounds fine. Make sure to also add a task for update the documentation for c0de in www-code for both c0de and paint

## 2026-11-21 – Library types in c0de

push both and start c0de sprint 19, please test task 17.7. A Windows update on my machine drastically slowed down the VM and some other operations, so the performance of the VM should be better. If the test is only flaky on Github, please disregard that as github runners frequently have performance problems when their servers are crowded.

Answer to the question about the type block of library types: a new TYPE block (instead of reusing TYPE_STRUCT).

## 2026-11-21 – Sprint 19 choices confirmed, c0de paint goes ahead

I confirm the choices you made in c0de sprint 19. As long as there is no content change in c0de-net, don't release a new version. Please go ahead with as many sprints/tasks of c0de-paint as you can

## 2026-11-22 – Push, Modrinth, logo and test servers

yourdecisions are fine.
1: Yes, please do
2: On Modrinth, the slug is c0de-paint and the id is zuHwh3ir. For curseforge, I need a logo first before I can create a project. Please create a logo in 2 variants, similar to what we did with c0de-paint. The curseforge variant is a 400x400 png, the regular logo is 256x256 pixels large. The logo should have the two slashes for c0de in the background and a paintbrush placed diagonally in front of it with a head in multiple colors in the upper left corner and the handle pinting to the lower right corner. Also, I would like to test the library a bit myself before releasing it. Can you deploy a server on the VM with c0de, c0de-net and c0de-paint and the same combo for the local client in the "run" folder, similar to what we have for the other two mods?
3: Please delay that until I have tested c0de in game

Please generate a mod description for modrinth and curseforge, similar to modrinth.md in the other repos

## 2026-11-22 – CurseForge ID and findings of the first test in the game

the curseforge project id is 1822987.

Findings:
c0de-paint related:
-The first program from the handbook just draws a stone box, no windows and no door.
c0de-related:
- I created a program and could not right click with a disk to its "PROGRAM" block as the schema does not recognize TYPE
- I fed the trigger block two constants as input. After running, I got two uninitialized variables in the output. Constants should remain as they are, when they appear in the output, they're constant.
- I fed some wrong data types to function parameters by accident which was only discovered at runtime. I would expect a red line number if I do that.


## 2026-11-22 – Push, release 0.7.1 and the VM

1. You can push the sprints
2. c0de 0.7.1 can come out now
3. the vm lost its network and I had to turn it off and on again. Please deploy c0de 0.7.1 there and make it available in the local test VM


## 2026-11-22 – Tag v0.7.1 pushed

The tag has been pushed
