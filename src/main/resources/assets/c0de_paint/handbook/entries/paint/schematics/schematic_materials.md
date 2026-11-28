```json
{
  "title": "SCHEMATIC_MATERIALS",
  "icon": "c0de:call_block",
  "category": "schematics",
  "ordinal": 1
}
```

The items that RENDER\_CANVAS takes to build the schematic, as a HASH from item id to number, counted like CANVAS\_ITEMS. It works without a canvas, so it also counts schematics that are larger than a canvas.

**Parameters and results**

- `file` STRING: the name of the schematic
- `replace` HASH STRING: blocks to replace (optional)
- result `result` HASH INTEGER: the items
- result `without_item` HASH INTEGER: the blocks by id that no item places, like fire

**Example**

```c0de
CALL c0de_paint/schematics/SCHEMATIC_MATERIALS
  PARAMETER file "castles/keep.litematic" ;
  RESULT result shopping_list
```
