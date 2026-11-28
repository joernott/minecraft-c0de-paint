```json
{
  "title": "Schematics",
  "icon": "c0de:call_block",
  "ordinal": 6
}
```

Die Bibliothek c0de\_paint/schematics liest die Schematics, die Spieler mit Litematica (`.litematic`) und Create (`.nbt`, die Strukturdateien von Minecraft) speichern, in Leinwände. Ein Programm bindet sie mit INCLUDE c0de\_paint/schematics ein, und c0de\_paint/cpaint für die Leinwände.

**Wo die Dateien liegen.** Beide Mods speichern Schematics im Ordner `schematics` des Spielordners des Spielers. Auf einem Server liegt dieser Ordner auf dem Computer des Spielers, deshalb liest der **Client des Spielers, der den Prozess ausführt,** die Datei und schickt sie zum Server\; der Prozess wartet solange auf seiner CALL-Zeile. Der Spieler muss online sein und c0de paint installiert haben (CLIENT\_UNAVAILABLE). Jeder Spieler entscheidet auf seinem Computer, ob sein Client Schematics liest: `allowSchematics` in `config/c0de_paint-client.properties` (Vorgabe true\; mit false SCHEMATICS\_DISABLED). Im Einzelspieler sind Server und Client derselbe Computer.

**Namen.** Eine Datei wird relativ zum Ordner `schematics` benannt, mit `/` zwischen Ordnern, etwa "castles/keep.litematic". Namen, die den Ordner verlassen (`..`, absolute Pfade, Laufwerksbuchstaben) oder versteckte Zeichen enthalten, lösen INVALID\_ARGUMENT aus.

**Was aus einer Schematic wird.** Die Blöcke mit ihren Zuständen und den Daten ihrer Blockobjekte (der Text von Schildern, die Gegenstände von Truhen, die Einstellungen von Maschinen), die kleinste Ecke auf Voxel 0,0,0\; die Regionen einer `.litematic` behalten ihre Lage zueinander. Luft bleibt ungesetzt. Objekte (Rahmen, Rüstungsständer) werden weggelassen und gezählt. Schematics älterer Minecraft-Versionen werden aktualisiert, wie Minecraft seine Welten aktualisiert, umbenannte Blöcke kommen also mit ihren neuen Namen heraus\; Dateien von vor Minecraft 1.13 werden nicht gelesen. Eine Schematic, die größer als eine Leinwand ist (canvasMin, canvasMax\; mit den Standardwerten 256 Blöcke auf jeder Achse, von 0 bis 255), löst SCHEMATIC\_TOO\_LARGE aus, ebenso eine Datei, die größer als `maxSchematicSize` ist.

**Blöcke ersetzen.** Jede Funktion nimmt den optionalen Parameter `replace`, einen HASH STRING von Block zu Block, der beim Lesen der Datei angewendet wird. Ein Schlüssel ist eine ID wie "create:andesite\_casing" oder eine ID mit Zuständen wie "oak\_stairs\[half=top\]"\; der Schlüssel mit den meisten passenden Zuständen gewinnt. Der neue Block behält die Zustände des alten, die er auch hat, so behalten Treppen ihre Richtung, und die Daten, wenn der neue Block keine eigenen hat. Der Wert "" lässt die Voxel ungesetzt.
