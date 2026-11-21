```json
{
  "title": "CANVAS_MIRROR",
  "icon": "c0de:call_block",
  "category": "canvas_operations",
  "ordinal": 10
}
```

Spiegelt die Leinwand an der Ebene senkrecht zur Achse bei `position`, etwa 4.5 für die Ebene zwischen den Voxeln 4 und 5. Ohne `position` spiegelt sie in der Mitte der Grenzen, die Grenzen bleiben also gleich. Die Zustände der Blöcke werden mitgespiegelt, auch die Scharniere von Türen.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand (eine Variable)
- `axis` STRING: "x", "y" oder "z"
- `position` FLOAT: die Ebene (optional)

**Beispiel**

```c0de
CALL c0de_paint/cpaint/CANVAS_MIRROR
  PARAMETER canvas c ;
  PARAMETER axis "x" ;
```
