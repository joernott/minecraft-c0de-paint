```json
{
  "title": "Platonische Körper",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 12
}
```

DRAW\_TETRAHEDRON, DRAW\_OCTAHEDRON, DRAW\_DODECAHEDRON und DRAW\_ICOSAHEDRON zeichnen den platonischen Körper um `center`, alle Ecken im Abstand `r` davon. Die Linien sind ihre Kanten. Jeder hat eine feste Ausrichtung: Das Tetraeder hat seine Ecken in jeder zweiten Ecke eines Würfels, das Oktaeder auf den Achsen, Ikosaeder und Dodekaeder sind über den goldenen Schnitt an den Achsen ausgerichtet. Andere Ausrichtungen entstehen mit CANVAS\_ROTATE. Der Würfel ist DRAW\_BOX. Weil alle Ecken im Abstand `r` liegen, stehen die Körper nicht auf `center`: Die unterste Ecke des Tetraeders liegt r/√3 (0,58 r) unter dem Mittelpunkt, die der anderen r darunter\; verschieb die Leinwand mit CANVAS\_MOVE oder nimm CANVAS\_MIN, um einen Körper auf den Boden zu stellen. Die Kanten kleiner Körper bleiben auf ihrer Oberfläche.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand (eine Variable)
- `center` STRUCT c0de\_paint/cpaint/POINT: der Mittelpunkt
- `r` INTEGER: der Abstand der Ecken
- `line_style`, `side_style`, `fill_style` TYPE c0de\_paint/cpaint/STYLE: die Stile (optional)

**Beispiel**

```c0de
CALL c0de_paint/cpaint/DRAW_ICOSAHEDRON
  PARAMETER canvas c ;
  PARAMETER center middle ;
  PARAMETER r 12 ;
  PARAMETER line_style gold ;
```
