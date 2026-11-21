```json
{
  "title": "RENDER_CANVAS",
  "icon": "minecraft:bricks",
  "category": "rendering",
  "ordinal": 0
}
```

Baut die Leinwand in der Welt\; ihr Voxel 0,0,0 landet bei `position`. Nicht gesetzte Voxel lassen die Welt, wie sie ist, Luft entfernt den Block dort, und Blöcke, die schon an ihrem Platz sind, werden übersprungen.

Ohne `input` braucht das Programm allowBlockChanges oder einen Administrator als Besitzer, und Blöcke im Weg werden ersetzt. Mit `input` darf jeder Besitzer rendern: Die Blöcke kommen zuerst aus `output`, dann aus `input`\; Blöcke im Weg werden mit den Werkzeugen aus `tools` abgebaut, die dabei abnutzen, und was sie fallen lassen, kommt nach `output`. Die Blätter, der Spawnschutz und Blöcke, die ein Spieler nicht abbauen kann, lösen PERMISSION\_DENIED aus.

Vor dem ersten Block prüft RENDER\_CANVAS die Rechte, die Chunks (CHUNK\_NOT\_LOADED), die Blöcke in den Inventaren (ITEM\_MISSING) und ein Werkzeug für jeden Block, der eines braucht, damit ein Fehler kein halbes Gebäude hinterlässt. Ein volles `output` (INVENTORY\_FULL) oder ein zerbrochenes Werkzeug halten es später an\; die Meldung sagt, wie viele Blöcke gesetzt wurden. Der Prozess wartet während des Renderns: Es setzt `renderBlocksPerTick` Blöcke pro Tick, von unten nach oben, und Fackeln, Leitern und Ähnliches nach den anderen Blöcken ihrer Schicht. Das Abbauen dauert so lange wie bei einem Spieler, geteilt durch `renderMiningSpeed`.

**Parameter und Ergebnisse**

- `canvas` TYPE c0de\_paint/cpaint/CANVAS: die Leinwand
- `position` STRUCT c0de/minecraft/POSITION: wo das Voxel 0,0,0 landet
- `input` STRUCT c0de/minecraft/POSITION: das Inventar mit den Blöcken (optional)
- `tools` STRUCT c0de/minecraft/POSITION: das Inventar mit den Werkzeugen (optional)
- `output` STRUCT c0de/minecraft/POSITION: das Inventar für entfernte Blöcke, wird zuerst für Blöcke benutzt (optional)

**Beispiel**

```c0de
CALL c0de_paint/cpaint/RENDER_CANVAS
  PARAMETER canvas c ;
  PARAMETER position site ;
  PARAMETER input supplies ;
  PARAMETER tools toolbox ;
  PARAMETER output rubble ;
```
