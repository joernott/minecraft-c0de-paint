```json
{
  "title": "STYLE_SOLID",
  "icon": "minecraft:magenta_dye",
  "category": "styles",
  "ordinal": 2
}
```

Always the same block. Its weight doesn't matter.

**Parameters and results**

- `block` STRUCT c0de\_paint/cpaint/BLOCK: the block
- result `result` TYPE c0de\_paint/cpaint/STYLE: the style

**Example**

```c0de
CALL c0de_paint/cpaint/STYLE_SOLID
  PARAMETER block stone ;
  RESULT result walls
```
