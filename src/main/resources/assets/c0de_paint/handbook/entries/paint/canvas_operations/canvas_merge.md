```json
{
  "title": "CANVAS_MERGE",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 6
}
```

Draws `source`, a canvas or an ARRAY of canvases, into `target`, moved by `offset`. With mode "OVERWRITE", set voxels of the source replace those of the target\; with "KEEP", they only fill voxels that are not set. Voxels that are not set in the source never change the target\; air does.

**Parameters and results**

- `target` TYPE c0de\_paint/cpaint/CANVAS: the canvas that changes (a variable)
- `source` TYPE c0de\_paint/cpaint/CANVAS or ARRAY TYPE c0de\_paint/cpaint/CANVAS: what is drawn
- `mode` STRING: "OVERWRITE" or "KEEP" (optional, default "OVERWRITE")
- `offset` STRUCT c0de\_paint/cpaint/POINT: the shift (optional, default 0,0,0)

**Example**

```c0de
CALL c0de_paint/cpaint/CANVAS_MERGE
  PARAMETER target castle ;
  PARAMETER source tower ;
  PARAMETER offset corner ;
```
