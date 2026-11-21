```json
{
  "title": "INVENTORY_BLOCKS",
  "icon": "minecraft:chest",
  "category": "styles",
  "ordinal": 6
}
```

The blocks of an inventory, for STYLE\_RANDOM and STYLE\_GRADIENT: in the order of the slots, with the number of items as weight. A block in several slots is listed once, at its first slot, with the sum of the items. Items that can't be placed as blocks are ignored.

**Parameters and results**

- `position` STRUCT c0de/minecraft/POSITION: the block with the inventory, like a chest
- result `result` ARRAY STRUCT c0de\_paint/cpaint/BLOCK: the blocks

**Example**

```c0de
CALL c0de_paint/cpaint/INVENTORY_BLOCKS
  PARAMETER position chest ;
  RESULT result palette
```
