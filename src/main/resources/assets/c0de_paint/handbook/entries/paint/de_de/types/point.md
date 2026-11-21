```json
{
  "title": "POINT",
  "icon": "c0de:type_struct_block",
  "category": "types",
  "ordinal": 2
}
```

Ein Voxel einer Leinwand: die Elemente `x`, `y` und `z`, alle INTEGER. Positionen in der Welt sind POSITIONs von c0de/minecraft.

```c0de
VARIABLE corner STRUCT c0de_paint/cpaint/POINT
ASSIGN corner.y 4 ;
```
