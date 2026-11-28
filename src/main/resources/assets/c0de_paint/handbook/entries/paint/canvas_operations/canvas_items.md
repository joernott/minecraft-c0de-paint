```json
{
  "title": "CANVAS_ITEMS",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 8
}
```

The items that RENDER\_CANVAS takes to build the canvas, as a HASH from item id to number: two slabs for a double slab, three candles for a block of three candles, a flower pot and the plant for a potted plant, a water bucket for water and for every waterlogged block. The upper halves of doors and plants, the heads of beds and piston heads take nothing, because the other part places them.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas
- result `result` HASH INTEGER: the items
- result `without_item` HASH INTEGER: the blocks by id that no item places, like fire

**Example**

```c0de
CALL c0de_paint/cpaint/CANVAS_ITEMS
  PARAMETER canvas c ;
  RESULT result shopping_list
```
