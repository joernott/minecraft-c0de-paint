```json
{
  "title": "DRAW_BLOCK",
  "icon": "minecraft:brush",
  "category": "drawing",
  "ordinal": 0
}
```

Zeichnet ein Voxel.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand (eine Variable)
- `point` STRUCT c0de\_paint/cpaint/POINT: das Voxel
- `style` TYPE c0de\_paint/cpaint/STYLE: der Stil

**Beispiel**

```c0de
CALL c0de_paint/cpaint/DRAW_BLOCK
  PARAMETER canvas c ;
  PARAMETER point top ;
  PARAMETER style glowstone ;
```
