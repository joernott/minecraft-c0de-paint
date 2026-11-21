```json
{
  "title": "Zeichnen",
  "icon": "minecraft:brush",
  "ordinal": 3
}
```

Die Zeichenfunktionen zeichnen in eine Leinwand, die sie als Referenz-Parameter bekommen, so wie INPUT seine Variable bekommt. Ein Voxel gehört zu einer Form, wenn sein Mittelpunkt darin liegt. Linien und Flächen sind ein Voxel dick, und Linien hängen über Kanten und Ecken zusammen.

Flache Formen liegen in einer beliebigen Ebene, die drei Punkte festlegen, meist Punkte der Form selbst. Liegen sie auf einer Geraden, löst die Funktion NOT\_A\_PLANE aus. Körper sind an den Achsen ausgerichtet oder haben eine der Richtungen "+x", "-x", "+y", "-y", "+z" und "-z"\; andere Ausrichtungen entstehen mit CANVAS\_ROTATE.
