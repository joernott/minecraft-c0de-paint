```json
{
  "title": "INVENTORY_BLOCKS",
  "icon": "minecraft:chest",
  "category": "styles",
  "ordinal": 6
}
```

Die Blöcke eines Inventars, für STYLE\_RANDOM und STYLE\_GRADIENT: in der Reihenfolge der Slots, mit der Zahl der Gegenstände als Gewicht. Ein Block in mehreren Slots erscheint einmal, an seinem ersten Slot, mit der Summe der Gegenstände. Gegenstände, die sich nicht als Block setzen lassen, werden übergangen.

**Parameter und Ergebnisse**

- `position` STRUCT c0de/minecraft/POSITION: der Block mit dem Inventar, etwa eine Truhe
- Ergebnis `result` ARRAY STRUCT c0de\_paint/cpaint/BLOCK: die Blöcke

**Beispiel**

```c0de
CALL c0de_paint/cpaint/INVENTORY_BLOCKS
  PARAMETER position chest ;
  RESULT result palette
```
