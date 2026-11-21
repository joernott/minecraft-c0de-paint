```json
{
  "title": "Typen",
  "icon": "c0de:type_library_block",
  "ordinal": 1
}
```

Die Bibliothek hat zwei eigene Typen, CANVAS und STYLE, und zwei Structs, POINT und BLOCK. Wie jeder Wert von c0de wird eine Leinwand oder ein Stil beim Zuweisen und Übergeben kopiert. Kopien sind billig: Sie teilen ihren Inhalt, bis eine von ihnen sich ändert.
