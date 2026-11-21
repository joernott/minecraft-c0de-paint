```json
{
  "title": "Ein Turm mit spitzem Dach",
  "icon": "c0de:program_block",
  "category": "examples",
  "ordinal": 0
}
```

Ein runder Turm aus Steinziegeln mit einem Kegel aus Schwarzeichenbrettern obendrauf.

```c0de
PROGRAM tower
NOOP
INCLUDE c0de/minecraft
INCLUDE c0de_paint/cpaint
NOOP
FUNCTION main
  PARAMETER at STRUCT c0de/minecraft/POSITION
BEGIN
  VARIABLE c TYPE c0de_paint/cpaint/CANVAS
  VARIABLE base STRUCT c0de_paint/cpaint/POINT
  VARIABLE bricks STRUCT c0de_paint/cpaint/BLOCK
  VARIABLE planks STRUCT c0de_paint/cpaint/BLOCK
  VARIABLE walls TYPE c0de_paint/cpaint/STYLE
  VARIABLE roof TYPE c0de_paint/cpaint/STYLE
  VARIABLE empty TYPE c0de_paint/cpaint/STYLE
  ASSIGN bricks.id "stone_bricks" ;
  ASSIGN planks.id "dark_oak_planks" ;
  CALL c0de_paint/cpaint/STYLE_SOLID
    PARAMETER block bricks ;
    RESULT result walls
  CALL c0de_paint/cpaint/STYLE_SOLID
    PARAMETER block planks ;
    RESULT result roof
  CALL c0de_paint/cpaint/STYLE_EMPTY
    RESULT result empty
  CALL c0de_paint/cpaint/DRAW_CYLINDER
    PARAMETER canvas c ;
    PARAMETER center base ;
    PARAMETER r 4 ;
    PARAMETER h 12 ;
    PARAMETER direction "+y" ;
    PARAMETER side_style walls ;
    PARAMETER fill_style empty ;
  ASSIGN base.y 12 ;
  CALL c0de_paint/cpaint/DRAW_CONE
    PARAMETER canvas c ;
    PARAMETER center base ;
    PARAMETER r 5 ;
    PARAMETER h 6 ;
    PARAMETER direction "+y" ;
    PARAMETER fill_style roof ;
  CALL c0de_paint/cpaint/RENDER_CANVAS
    PARAMETER canvas c ;
    PARAMETER position at ;
END FUNCTION
```
