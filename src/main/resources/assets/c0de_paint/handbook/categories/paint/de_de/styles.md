```json
{
  "title": "Stile",
  "icon": "minecraft:magenta_dye",
  "ordinal": 2
}
```

Jede Form hat bis zu drei Teile, jeder mit eigenem Stil-Parameter: die Linien (`line_style`, Umrisse und Kanten), die Flächen (`side_style`, die Oberfläche eines Körpers) und das Innere (`fill_style`). Die Stil-Parameter sind optional\; ihr Standard ist STYLE\_NONE.

Ein Teil mit STYLE\_NONE übernimmt den Stil des nächsten Teils weiter innen: Linien nehmen den Flächenstil, bei flachen Formen den Füllstil, und Flächen nehmen den Füllstil. Ein Quader mit nur einem Füllstil ist also voll, ein Flächenstil ohne Füllstil ergibt einen hohlen Quader, und nur ein Linienstil ergibt einen Rahmen.
