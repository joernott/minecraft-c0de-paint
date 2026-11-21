```json
{
  "title": "First drawing",
  "icon": "c0de:include_block",
  "category": "getting_started",
  "ordinal": 0
}
```

This program draws a hollow stone box with glass windows of 7 by 5 by 7 voxels and builds it next to the trigger block. It needs allowBlockChanges or an administrator, because it doesn't take the blocks from an inventory.

```c0de
PROGRAM hut
NOOP
INCLUDE c0de/minecraft
INCLUDE c0de_paint/cpaint
NOOP
FUNCTION main
  PARAMETER at STRUCT c0de/minecraft/POSITION
BEGIN
  VARIABLE c TYPE c0de_paint/cpaint/CANVAS
  VARIABLE a STRUCT c0de_paint/cpaint/POINT
  VARIABLE b STRUCT c0de_paint/cpaint/POINT
  VARIABLE stone STRUCT c0de_paint/cpaint/BLOCK
  VARIABLE walls TYPE c0de_paint/cpaint/STYLE
  VARIABLE empty TYPE c0de_paint/cpaint/STYLE
  ASSIGN stone.id "stone_bricks" ;
  ASSIGN b.x 6 ;
  ASSIGN b.y 4 ;
  ASSIGN b.z 6 ;
  CALL c0de_paint/cpaint/STYLE_SOLID
    PARAMETER block stone ;
    RESULT result walls
  CALL c0de_paint/cpaint/STYLE_EMPTY
    RESULT result empty
  CALL c0de_paint/cpaint/DRAW_BOX
    PARAMETER canvas c ;
    PARAMETER from a ;
    PARAMETER to b ;
    PARAMETER side_style walls ;
    PARAMETER fill_style empty ;
  CALL c0de_paint/cpaint/RENDER_CANVAS
    PARAMETER canvas c ;
    PARAMETER position at ;
END FUNCTION
```
