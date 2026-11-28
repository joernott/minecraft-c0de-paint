```json
{
  "title": "CANVAS_SCALE",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 9
}
```

Scales the canvas around `center` along every axis by a percentage of 1 or more. Every voxel takes the nearest voxel of the original, so blocks keep their states.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas (a variable)
- `x`, `y`, `z` INTEGER: the percentages
- `center` STRUCT c0de\_paint/cpaint/POINT: the fixed point (optional, default 0,0,0)

**Example**

```c0de
CALL c0de_paint/cpaint/CANVAS_SCALE
  PARAMETER canvas c ;
  PARAMETER x 200 ;
  PARAMETER y 200 ;
  PARAMETER z 200 ;
```
