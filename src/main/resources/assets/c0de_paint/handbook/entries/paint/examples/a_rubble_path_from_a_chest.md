```json
{
  "title": "A rubble path from a chest",
  "icon": "c0de:program_block",
  "category": "examples",
  "ordinal": 1
}
```

A path of random blocks from a chest, built by any player: the blocks come from the chest, the tools from a second chest, and whatever is in the way goes into a third.

```c0de
PROGRAM path
NOOP
INCLUDE c0de/minecraft
INCLUDE c0de_paint/cpaint
NOOP
FUNCTION main
  PARAMETER at STRUCT c0de/minecraft/POSITION
  PARAMETER supplies STRUCT c0de/minecraft/POSITION
  PARAMETER toolbox STRUCT c0de/minecraft/POSITION
  PARAMETER rubble STRUCT c0de/minecraft/POSITION
BEGIN
  VARIABLE c TYPE c0de_paint/cpaint/CANVAS
  VARIABLE a STRUCT c0de_paint/cpaint/POINT
  VARIABLE b STRUCT c0de_paint/cpaint/POINT
  VARIABLE blocks ARRAY STRUCT c0de_paint/cpaint/BLOCK
  VARIABLE gravel TYPE c0de_paint/cpaint/STYLE
  ASSIGN b.x 15 ;
  ASSIGN b.z 2 ;
  CALL c0de_paint/cpaint/INVENTORY_BLOCKS
    PARAMETER position supplies ;
    RESULT result blocks
  CALL c0de_paint/cpaint/STYLE_RANDOM
    PARAMETER blocks blocks ;
    RESULT result gravel
  CALL c0de_paint/cpaint/DRAW_BOX
    PARAMETER canvas c ;
    PARAMETER from a ;
    PARAMETER to b ;
    PARAMETER fill_style gravel ;
  CALL c0de_paint/cpaint/RENDER_CANVAS
    PARAMETER canvas c ;
    PARAMETER position at ;
    PARAMETER input supplies ;
    PARAMETER tools toolbox ;
    PARAMETER output rubble ;
END FUNCTION
```
