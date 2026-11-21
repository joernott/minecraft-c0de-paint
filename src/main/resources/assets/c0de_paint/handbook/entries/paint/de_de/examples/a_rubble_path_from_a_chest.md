```json
{
  "title": "Ein Schotterweg aus einer Truhe",
  "icon": "c0de:program_block",
  "category": "examples",
  "ordinal": 1
}
```

Ein Weg aus zufälligen Blöcken aus einer Truhe, den jeder Spieler bauen kann: Die Blöcke kommen aus der Truhe, die Werkzeuge aus einer zweiten Truhe, und was im Weg ist, landet in einer dritten.

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
