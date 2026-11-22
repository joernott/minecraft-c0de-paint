```json
{
  "title": "Platonic solids",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 12
}
```

DRAW\_TETRAHEDRON, DRAW\_OCTAHEDRON, DRAW\_DODECAHEDRON and DRAW\_ICOSAHEDRON draw the Platonic solid around `center`, with all corners at the distance `r` from it. The lines are their edges. Each has a fixed orientation: the tetrahedron has its corners in alternate corners of a cube, the octahedron on the axes, the icosahedron and the dodecahedron are aligned with the axes by the golden ratio. Other orientations come from CANVAS\_ROTATE. The cube is DRAW\_BOX.

**Parameters and results**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: the canvas (a variable)
- `center` STRUCT c0de\_paint/cpaint/POINT: the center
- `r` INTEGER: the distance of the corners
- `line_style`, `side_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: the styles (optional)

**Example**

```c0de
CALL c0de_paint/cpaint/DRAW_ICOSAHEDRON
  PARAMETER canvas c ;
  PARAMETER center middle ;
  PARAMETER r 12 ;
  PARAMETER line_style gold ;
```
