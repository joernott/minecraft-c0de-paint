```json
{
  "title": "BLOCK",
  "icon": "c0de:type_struct_block",
  "category": "types",
  "ordinal": 3
}
```

Ein Block eines Stils oder einer Leinwand: `id` STRING, `properties` HASH STRING mit Blockzuständen wie bei PLACE\_BLOCK, und `weight` INTEGER. Eine id ohne Namensraum bekommt `minecraft:`, "stone" ist also "minecraft:stone". Gewichte sind 1 oder größer\; sie zählen bei zufälligen Blöcken und Verläufen.

```c0de
VARIABLE log STRUCT c0de_paint/cpaint/BLOCK
ASSIGN log.id "oak_log" ;
ASSIGN log.properties["axis"] "x" ;
ASSIGN log.weight 1 ;
```
