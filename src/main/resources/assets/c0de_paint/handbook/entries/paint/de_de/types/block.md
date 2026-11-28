```json
{
  "title": "BLOCK",
  "icon": "c0de:type_struct_block",
  "category": "types",
  "ordinal": 3
}
```

Ein Block eines Stils oder einer Leinwand: `id` STRING, `properties` HASH STRING mit Blockzuständen wie bei PLACE\_BLOCK, `weight` INTEGER und `data` STRING. Eine id ohne Namensraum bekommt `minecraft:`, "stone" ist also "minecraft:stone". Gewichte sind 1 oder größer\; sie zählen bei zufälligen Blöcken und Verläufen, Leinwände beachten sie nicht. `data` sind die Daten des Blockobjekts als SNBT, der Text, den GET\_BLOCK von c0de/minecraft liefert und PLACE\_BLOCK nimmt, etwa `{Items:[{Slot:0b,id:"minecraft:stone",count:3}]}` für eine Truhe\; "" für keine. Leinwände, Stile und Transformationen behalten sie\; wann sie gesetzt werden, steht bei RENDER\_CANVAS.

```c0de
VARIABLE log STRUCT c0de_paint/cpaint/BLOCK
ASSIGN log.id "oak_log" ;
ASSIGN log.properties["axis"] "x" ;
ASSIGN log.weight 1 ;
```
