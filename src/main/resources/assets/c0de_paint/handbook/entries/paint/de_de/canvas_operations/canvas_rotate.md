```json
{
  "title": "CANVAS_ROTATE",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 10
}
```

Dreht die Leinwand um die Achse "x", "y" oder "z" durch `center`, um einen beliebigen Winkel in Grad. Positive Winkel folgen der Rechte-Hand-Regel: +90 um y dreht Osten nach Norden. Jedes Voxel nimmt das nächste Voxel des Originals, es entstehen also keine Löcher\; bei Winkeln, die keine Vielfachen von 90 sind, werden Kanten etwas unregelmäßig. Die Zustände der Blöcke drehen sich mit, soweit Minecraft es erlaubt (facing, axis, rotation, die Formen von Treppen und Schienen), gerundet auf 90°. Einen Zustand, den der Block nicht annehmen kann, behält er wie zuvor: Treppen können nicht nach oben zeigen.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand (eine Variable)
- `center` STRUCT c0de\_paint/cpaint/POINT: ein Punkt der Achse
- `axis` STRING: "x", "y" oder "z"
- `degrees` FLOAT: der Winkel

**Beispiel**

```c0de
CALL c0de_paint/cpaint/CANVAS_ROTATE
  PARAMETER canvas c ;
  PARAMETER center middle ;
  PARAMETER axis "y" ;
  PARAMETER degrees 90 ;
```
