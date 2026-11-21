```json
{
  "title": "STYLE_PATTERN",
  "icon": "minecraft:magenta_dye",
  "category": "styles",
  "ordinal": 5
}
```

Wiederholt eine Leinwand entlang aller drei Achsen. Eine Wiederholung hat die Größe der Grenzen des Musters, und ihre kleinste Ecke liegt bei `from`. Voxel, die im Muster nicht gesetzt sind, werden nicht gezeichnet. Ein leeres Muster löst CANVAS\_EMPTY aus.

**Parameter und Ergebnisse**

- `pattern` TYPE c0de\_paint/cpaint/CANVAS: das Muster
- `from` STRUCT c0de\_paint/cpaint/POINT: wo eine Wiederholung beginnt (optional, Standard 0,0,0)
- Ergebnis `result` TYPE c0de\_paint/cpaint/STYLE: der Stil

**Beispiel**

```c0de
CALL c0de_paint/cpaint/STYLE_PATTERN
  PARAMETER pattern checkers ;
  RESULT result floor
```
