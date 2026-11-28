```json
{
  "title": "SCHEMATIC_LIST",
  "icon": "c0de:call_block",
  "category": "schematics",
  "ordinal": 3
}
```

The schematics in a folder of `schematics` and its subfolders, sorted, as names that the other functions take.

**Parameters and results**

- `folder` STRING: the folder (optional, default the folder `schematics` itself)
- result `result` ARRAY STRING: the names

**Example**

```c0de
CALL c0de_paint/schematics/SCHEMATIC_LIST
  PARAMETER folder "castles" ;
  RESULT result names
```
