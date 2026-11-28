```json
{
  "title": "Schematics",
  "icon": "c0de:call_block",
  "ordinal": 6
}
```

The library c0de\_paint/schematics reads the schematics that players save with Litematica (`.litematic`) and Create (`.nbt`, the structure files of Minecraft) into canvases. A program includes it with INCLUDE c0de\_paint/schematics, and c0de\_paint/cpaint for the canvases.

**Where the files are.** Both mods save schematics in the folder `schematics` of the game folder of the player. On a server, that folder is on the computer of the player, so the **client of the player who runs the process** reads the file and sends it to the server\; the process waits on its CALL line meanwhile. The player must be online and have c0de paint installed (CLIENT\_UNAVAILABLE). Every player decides on their own computer whether their client reads schematics: `allowSchematics` in `config/c0de_paint-client.properties` (default true\; with false, SCHEMATICS\_DISABLED). In single player, server and client are the same computer.

**Names.** A file is named relative to the folder `schematics`, with `/` between folders, like "castles/keep.litematic". Names that leave the folder (`..`, absolute paths, drive letters) or contain hidden characters raise INVALID\_ARGUMENT.

**What a schematic becomes.** The blocks with their states and the data of their block entities (the text of signs, the items of chests, the settings of machines), the minimum corner at voxel 0,0,0\; the regions of a `.litematic` keep their places to each other. Air stays not set. Entities (item frames, armor stands) are left out and counted. Schematics of older Minecraft versions are upgraded like Minecraft upgrades its worlds, so renamed blocks come out with their new names\; files from before Minecraft 1.13 aren't read. A schematic larger than a canvas (canvasMin, canvasMax) raises SCHEMATIC\_TOO\_LARGE, a file larger than `maxSchematicSize` too.

**Replacing blocks.** Every function takes the optional parameter `replace`, a HASH STRING from block to block, applied while the file is read. A key is an id like "create:andesite\_casing" or an id with states like "oak\_stairs\[half=top\]"\; the key with the most matching states wins. The new block keeps the states of the old one that it also has, so stairs keep their direction, and the data unless the new block has data of its own. The value "" leaves the voxels not set.
