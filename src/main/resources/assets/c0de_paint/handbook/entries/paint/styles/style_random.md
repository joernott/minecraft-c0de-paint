```json
{
  "title": "STYLE_RANDOM",
  "icon": "minecraft:magenta_dye",
  "category": "styles",
  "ordinal": 3
}
```

For every voxel a randomly chosen block. The chance of a block is its weight divided by the sum of all weights. The random numbers come from the process, like RANDOM, so a program started with a fixed seed draws the same every time.

**Parameters and results**

- `blocks` ARRAY STRUCT c0de\_paint/cpaint/BLOCK: the blocks with their weights
- result `result` TYPE c0de\_paint/cpaint/STYLE: the style

**Example**

```c0de
CALL c0de_paint/cpaint/STYLE_RANDOM
  PARAMETER blocks stones ;
  RESULT result rubble
```
