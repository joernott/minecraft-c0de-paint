```json
{
  "title": "STYLE_PATTERN",
  "icon": "minecraft:magenta_dye",
  "category": "styles",
  "ordinal": 5
}
```

Repeats a canvas along all three axes. One repetition has the size of the bounds of the pattern, and its minimum corner lies at `from`. Voxels that are not set in the pattern are not drawn. An empty pattern raises CANVAS\_EMPTY.

**Parameters and results**

- `pattern` TYPE c0de\_paint/cpaint/CANVAS: the pattern
- `from` STRUCT c0de\_paint/cpaint/POINT: where a repetition starts (optional, default 0,0,0)
- result `result` TYPE c0de\_paint/cpaint/STYLE: the style

**Example**

```c0de
CALL c0de_paint/cpaint/STYLE_PATTERN
  PARAMETER pattern checkers ;
  RESULT result floor
```
