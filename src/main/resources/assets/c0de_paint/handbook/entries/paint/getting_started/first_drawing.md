```json
{
  "title": "First drawing",
  "icon": "c0de:include_block",
  "category": "getting_started",
  "ordinal": 0
}
```

This program draws a hut of stone bricks of 7 by 5 by 7 voxels with a glass window on two sides and an oak door, and builds it next to the trigger block. It needs allowBlockChanges or an administrator, because it doesn't take the blocks from an inventory.

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
  VARIABLE top STRUCT c0de_paint/cpaint/POINT
  VARIABLE stone STRUCT c0de_paint/cpaint/BLOCK
  VARIABLE pane STRUCT c0de_paint/cpaint/BLOCK
  VARIABLE door STRUCT c0de_paint/cpaint/BLOCK
  VARIABLE walls TYPE c0de_paint/cpaint/STYLE
  VARIABLE empty TYPE c0de_paint/cpaint/STYLE
  VARIABLE glass TYPE c0de_paint/cpaint/STYLE
  VARIABLE lower TYPE c0de_paint/cpaint/STYLE
  VARIABLE upper TYPE c0de_paint/cpaint/STYLE
  ASSIGN stone.id "stone_bricks" ;
  ASSIGN pane.id "glass" ;
  ASSIGN door.id "oak_door" ;
  ASSIGN door.properties["facing"] "north" ;
  ASSIGN door.properties["half"] "lower" ;
  CALL c0de_paint/cpaint/STYLE_SOLID
    PARAMETER block stone ;
    RESULT result walls
  CALL c0de_paint/cpaint/STYLE_EMPTY
    RESULT result empty
  CALL c0de_paint/cpaint/STYLE_SOLID
    PARAMETER block pane ;
    RESULT result glass
  CALL c0de_paint/cpaint/STYLE_SOLID
    PARAMETER block door ;
    RESULT result lower
  ASSIGN door.properties["half"] "upper" ;
  CALL c0de_paint/cpaint/STYLE_SOLID
    PARAMETER block door ;
    RESULT result upper
  NOOP
  ASSIGN b.x 6 ;
  ASSIGN b.y 4 ;
  ASSIGN b.z 6 ;
  CALL c0de_paint/cpaint/DRAW_BOX
    PARAMETER canvas c ;
    PARAMETER from a ;
    PARAMETER to b ;
    PARAMETER side_style walls ;
    PARAMETER fill_style empty ;
  NOOP
  ASSIGN a.x 0 ;
  ASSIGN a.y 2 ;
  ASSIGN a.z 2 ;
  ASSIGN b.x 0 ;
  ASSIGN b.y 2 ;
  ASSIGN b.z 4 ;
  ASSIGN top.x 0 ;
  ASSIGN top.y 3 ;
  ASSIGN top.z 2 ;
  CALL c0de_paint/cpaint/DRAW_RECTANGLE
    PARAMETER canvas c ;
    PARAMETER a a ;
    PARAMETER b b ;
    PARAMETER c top ;
    PARAMETER fill_style glass ;
  ASSIGN a.x 6 ;
  ASSIGN b.x 6 ;
  ASSIGN top.x 6 ;
  CALL c0de_paint/cpaint/DRAW_RECTANGLE
    PARAMETER canvas c ;
    PARAMETER a a ;
    PARAMETER b b ;
    PARAMETER c top ;
    PARAMETER fill_style glass ;
  NOOP
  ASSIGN a.x 3 ;
  ASSIGN a.y 1 ;
  ASSIGN a.z 0 ;
  CALL c0de_paint/cpaint/DRAW_LINE
    PARAMETER canvas c ;
    PARAMETER from a ;
    PARAMETER to a ;
    PARAMETER style lower ;
  ASSIGN a.y 2 ;
  CALL c0de_paint/cpaint/DRAW_LINE
    PARAMETER canvas c ;
    PARAMETER from a ;
    PARAMETER to a ;
    PARAMETER style upper ;
  NOOP
  CALL c0de_paint/cpaint/RENDER_CANVAS
    PARAMETER canvas c ;
    PARAMETER position at ;
END FUNCTION
```
