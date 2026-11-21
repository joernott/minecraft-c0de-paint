```json
{
  "title": "STYLE_SOLID",
  "icon": "minecraft:magenta_dye",
  "category": "styles",
  "ordinal": 2
}
```

Immer derselbe Block. Sein Gewicht spielt keine Rolle.

**Parameter und Ergebnisse**

- `block` STRUCT c0de\_paint/cpaint/BLOCK: der Block
- Ergebnis `result` TYPE c0de\_paint/cpaint/STYLE: der Stil

**Beispiel**

```c0de
CALL c0de_paint/cpaint/STYLE_SOLID
  PARAMETER block stone ;
  RESULT result walls
```
