```json
{
  "title": "SCHEMATIC_LOAD",
  "icon": "c0de:call_block",
  "category": "schematics",
  "ordinal": 0
}
```

The schematic as a canvas.

**Parameters and results**

- `file` STRING: the name of the schematic
- `replace` HASH STRING: blocks to replace (optional)
- `region` STRING: only this region of a `.litematic` (optional, default all)
- result `result` TYPE c0de\_paint/cpaint/CANVAS: the canvas
- result `entities` INTEGER: the number of entities that were left out

**Example**

```c0de
VARIABLE swap HASH STRING
ASSIGN swap["create:andesite_casing"] "stripped_spruce_log" ;
CALL c0de_paint/schematics/SCHEMATIC_LOAD
  PARAMETER file "castles/keep.litematic" ;
  PARAMETER replace swap ;
  RESULT result keep
```
