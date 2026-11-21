```json
{
  "title": "Styles",
  "icon": "minecraft:magenta_dye",
  "ordinal": 2
}
```

Every shape has up to three parts, each with its own style parameter: the lines (`line_style`, outlines and edges), the faces (`side_style`, the surface of a body) and the inside (`fill_style`). The style parameters are optional\; their default is STYLE\_NONE.

A part with STYLE\_NONE takes the style of the next part inward: lines take the side style, or the fill style of flat shapes, and faces take the fill style. So a box with only a fill style is full, a side style without a fill style gives a hollow box, and only a line style gives a frame.
