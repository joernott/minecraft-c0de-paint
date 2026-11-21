```json
{
  "title": "Drawing",
  "icon": "minecraft:brush",
  "ordinal": 3
}
```

The drawing functions draw into a canvas, which they get as a reference parameter, like INPUT gets its variable. A voxel belongs to a shape when its center lies inside. Lines and faces are one voxel thick, and lines connect through edges and corners.

Flat shapes lie in any plane, given by three points, usually points of the shape itself. If they lie on one line, the function raises NOT\_A\_PLANE. Bodies are axis-aligned or have one of the directions "+x", "-x", "+y", "-y", "+z" and "-z"\; other orientations come from CANVAS\_ROTATE.
