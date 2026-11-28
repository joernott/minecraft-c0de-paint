```json
{
  "title": "BLOCK",
  "icon": "c0de:type_struct_block",
  "category": "types",
  "ordinal": 3
}
```

A block of a style or of a canvas: `id` STRING, `properties` HASH STRING with block states as in PLACE\_BLOCK, `weight` INTEGER and `data` STRING. An id without a namespace gets `minecraft:`, so "stone" is "minecraft:stone". Weights are 1 or more\; they matter for random blocks and gradients, canvases ignore them. `data` is the data of the block entity as SNBT, the text that GET\_BLOCK of c0de/minecraft returns and PLACE\_BLOCK takes, like `{Items:[{Slot:0b,id:"minecraft:stone",count:3}]}` for a chest\; "" for none. Canvases, styles and transformations keep it\; see RENDER\_CANVAS for when it is placed.

```c0de
VARIABLE log STRUCT c0de_paint/cpaint/BLOCK
ASSIGN log.id "oak_log" ;
ASSIGN log.properties["axis"] "x" ;
ASSIGN log.weight 1 ;
```
