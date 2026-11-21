```json
{
  "title": "STYLE_GRADIENT",
  "icon": "minecraft:magenta_dye",
  "category": "styles",
  "ordinal": 4
}
```

A gradient through the blocks in their order. The first block is drawn at `from`. The distance from `from` is measured along `axes`: "xyz" gives spheres, "y" horizontal layers, "xz" cylinders. Every block gets a share of the length in proportion to its weight: five black, three gray and two white blocks give a line of 10 voxels 5 black, 3 gray and 2 white voxels. Without `to`, the gradient ends at the voxel of the part that is farthest from `from`\; voxels beyond the end get the last block. `fuzzy` blurs the borders: 1 to 3 lines on each side take the neighbouring block with a chance of 50 \%, 25 \% and 12.5 \%.

**Parameters and results**

- `blocks` ARRAY STRUCT c0de\_paint/cpaint/BLOCK: the blocks in their order, with their weights
- `from` STRUCT c0de\_paint/cpaint/POINT: where the gradient starts
- `to` STRUCT c0de\_paint/cpaint/POINT: where it ends (optional)
- `axes` STRING: the axes of the distance (optional, default "xyz")
- `fuzzy` INTEGER: how blurred the borders are, 0 to 3 (optional, default 0)
- result `result` TYPE c0de\_paint/cpaint/STYLE: the style

**Example**

```c0de
CALL c0de_paint/cpaint/STYLE_GRADIENT
  PARAMETER blocks concrete ;
  PARAMETER from bottom ;
  PARAMETER axes "y" ;
  PARAMETER fuzzy 1 ;
  RESULT result sunset
```
