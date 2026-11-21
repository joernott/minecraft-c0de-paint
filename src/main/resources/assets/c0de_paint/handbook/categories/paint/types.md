```json
{
  "title": "Types",
  "icon": "c0de:type_library_block",
  "ordinal": 1
}
```

The library has two types of its own, CANVAS and STYLE, and two structs, POINT and BLOCK. Like every value of c0de, a canvas or a style is copied when it is assigned or passed. Copies are cheap: they share their content until one of them changes.
