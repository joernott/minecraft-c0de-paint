```json
{
  "title": "STYLE_GRADIENT",
  "icon": "minecraft:magenta_dye",
  "category": "styles",
  "ordinal": 4
}
```

Ein Verlauf durch die Blöcke in ihrer Reihenfolge. Bei `from` wird der erste Block gezeichnet. Der Abstand von `from` wird entlang `axes` gemessen: "xyz" ergibt Kugeln, "y" waagrechte Schichten, "xz" Zylinder. Jeder Block bekommt einen Anteil der Länge im Verhältnis seines Gewichts: Fünf schwarze, drei graue und zwei weiße Blöcke ergeben auf einer Linie von 10 Voxeln 5 schwarze, 3 graue und 2 weiße Voxel. Ohne `to` endet der Verlauf am Voxel des Teils, das am weitesten von `from` entfernt ist\; Voxel hinter dem Ende bekommen den letzten Block. `fuzzy` verwischt die Grenzen: 1 bis 3 Linien auf jeder Seite nehmen mit einer Chance von 50 \%, 25 \% und 12,5 \% den Nachbarblock.

**Parameter und Ergebnisse**

- `blocks` ARRAY STRUCT c0de\_paint/cpaint/BLOCK: die Blöcke in ihrer Reihenfolge, mit ihren Gewichten
- `from` STRUCT c0de\_paint/cpaint/POINT: wo der Verlauf beginnt
- `to` STRUCT c0de\_paint/cpaint/POINT: wo er endet (optional)
- `axes` STRING: die Achsen des Abstands (optional, Standard "xyz")
- `fuzzy` INTEGER: wie verwischt die Grenzen sind, 0 bis 3 (optional, Standard 0)
- Ergebnis `result` TYPE c0de\_paint/cpaint/STYLE: der Stil

**Beispiel**

```c0de
CALL c0de_paint/cpaint/STYLE_GRADIENT
  PARAMETER blocks concrete ;
  PARAMETER from bottom ;
  PARAMETER axes "y" ;
  PARAMETER fuzzy 1 ;
  RESULT result sunset
```
