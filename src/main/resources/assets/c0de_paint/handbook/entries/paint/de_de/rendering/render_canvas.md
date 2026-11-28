```json
{
  "title": "RENDER_CANVAS",
  "icon": "minecraft:bricks",
  "category": "rendering",
  "ordinal": 0
}
```

Baut die Leinwand in der Welt\; ihr Voxel 0,0,0 landet bei `position`. Nicht gesetzte Voxel lassen die Welt, wie sie ist, Luft entfernt den Block dort, und Blöcke, die schon an ihrem Platz sind, werden übersprungen.

Ohne `input` braucht das Programm allowBlockChanges (`config/c0de.properties` von c0de) oder einen Administrator als Besitzer, und Blöcke im Weg werden ersetzt. Mit `input` darf jeder Besitzer rendern: Die Gegenstände kommen zuerst aus `output`, dann aus `input`, so wie CANVAS\_ITEMS sie zählt (Wassereimer kommen leer zurück)\; Blöcke im Weg werden mit den Werkzeugen aus `tools` abgebaut, die dabei abnutzen, und was sie fallen lassen, kommt nach `output`. Blöcke, die ohne Werkzeug abgehen, wie Erde und Glas, werden mit der Hand abgebaut\; ohne `output` geht, was sie fallen lassen, verloren. Die Blätter, der Spawnschutz und Blöcke, die ein Spieler nicht abbauen kann, lösen PERMISSION\_DENIED aus.

**Blockdaten.** Die Daten der Blöcke (das Element `data`) werden mit ihnen gesetzt. Blöcke mit Inventar behalten ihren Inhalt: Ohne `input` kommen die Gegenstände mit den Daten\; mit `input` werden sie wie die Blöcke aus den Inventaren genommen, und `contents` entscheidet, was passiert, wenn welche fehlen: "REQUIRED" (Standard) löst ITEM\_MISSING vor dem ersten Block aus, "AVAILABLE" füllt ein, was da ist, "NONE" setzt die Inventare leer. Mit `input` bleibt weg, was aus dem Nichts Dinge erzeugen würde: Beutetabellen, die Bienen von Bienenstöcken, der Gegenstand in seltsamem Sand und Kies, das Buch eines Lesepults. Die Daten von Befehlsblöcken, Konstruktionsblöcken und Verbundblöcken brauchen einen Administrator (PERMISSION\_DENIED).

**Fehlende Blöcke.** Mit `blocks` "AVAILABLE" werden die Blöcke gesetzt, die die Inventare haben, und die Voxel der anderen übersprungen, statt ITEM\_MISSING auszulösen. Das Ergebnis `missing` nennt nach Gegenstands-ID, was weggelassen wurde, Blöcke und Inhalte, damit das Programm es holen und noch einmal rendern kann: Blöcke, die schon an ihrem Platz sind, werden übersprungen und behalten ihren Inhalt.

Vor dem ersten Block prüft RENDER\_CANVAS die Rechte, die Chunks (CHUNK\_NOT\_LOADED), die Blöcke in den Inventaren (ITEM\_MISSING) und ein Werkzeug für jeden Block, der eines braucht, damit ein Fehler kein halbes Gebäude hinterlässt. Ein volles `output` (INVENTORY\_FULL) oder ein zerbrochenes Werkzeug halten es später an\; die Meldung sagt, wie viele Blöcke gesetzt wurden. Der Prozess wartet während des Renderns: Es setzt `renderBlocksPerTick` Blöcke pro Tick, von unten nach oben, und Fackeln, Leitern und Ähnliches nach den anderen Blöcken ihrer Schicht. Das Abbauen dauert so lange wie bei einem Spieler, geteilt durch `renderMiningSpeed`.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand
- `position` STRUCT c0de/minecraft/POSITION: wo das Voxel 0,0,0 landet
- `input` STRUCT c0de/minecraft/POSITION: das Inventar mit den Blöcken (optional)
- `tools` STRUCT c0de/minecraft/POSITION: das Inventar mit den Werkzeugen (optional)
- `output` STRUCT c0de/minecraft/POSITION: das Inventar für entfernte Blöcke, wird zuerst für Blöcke benutzt (optional)
- `contents` STRING: "REQUIRED", "AVAILABLE" oder "NONE", für den Inhalt von Inventaren (optional, Standard "REQUIRED")
- `blocks` STRING: "REQUIRED" oder "AVAILABLE", für fehlende Blöcke (optional, Standard "REQUIRED")
- Ergebnis `missing` HASH INTEGER: was weggelassen wurde, nach Gegenstands-ID

**Beispiel**

```c0de
CALL c0de_paint/cpaint/RENDER_CANVAS
  PARAMETER canvas c ;
  PARAMETER position site ;
  PARAMETER input supplies ;
  PARAMETER tools toolbox ;
  PARAMETER output rubble ;
```
