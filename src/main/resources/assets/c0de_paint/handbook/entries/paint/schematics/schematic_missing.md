```json
{
  "title": "SCHEMATIC_MISSING",
  "icon": "c0de:call_block",
  "category": "schematics",
  "ordinal": 2
}
```

The blocks of the schematic that this server doesn't have, as a HASH from block id to number\; empty if the schematic can be built here. Blocks of mods that aren't installed, like the blocks of Create on a server without Create, and blocks of newer Minecraft versions show up here. Run it with the `replace` you plan to use to check it.

**Parameters and results**

- `file` STRING: the name of the schematic
- `replace` HASH STRING: blocks to replace (optional)
- result `result` HASH INTEGER: the missing blocks

**Example**

```c0de
CALL c0de_paint/schematics/SCHEMATIC_MISSING
  PARAMETER file "factory.nbt" ;
  PARAMETER replace swap ;
  RESULT result missing
```
