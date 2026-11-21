```json
{
  "title": "BLOCK",
  "icon": "c0de:type_struct_block",
  "category": "types",
  "ordinal": 3
}
```

A block of a style or of a canvas: `id` STRING, `properties` HASH STRING with block states as in PLACE\_BLOCK, and `weight` INTEGER. An id without a namespace gets `minecraft:`, so "stone" is "minecraft:stone". Weights are 1 or more\; they matter for random blocks and gradients.

```c0de
VARIABLE log STRUCT c0de_paint/cpaint/BLOCK
ASSIGN log.id "oak_log" ;
ASSIGN log.properties["axis"] "x" ;
ASSIGN log.weight 1 ;
```
