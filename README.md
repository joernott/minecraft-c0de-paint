# c0de paint

A companion mod of [c0de](https://github.com/joernott/minecraft-c0de) for Minecraft (Fabric and NeoForge). Its library `c0de_paint/cpaint` draws lines, flat shapes and bodies into canvases, combines and transforms them, and builds them in the world. Its library `c0de_paint/schematics` reads the schematics of Litematica and Create into canvases and lists the items to build them.

```
INCLUDE c0de_paint/cpaint
VARIABLE c TYPE c0de_paint/cpaint/CANVAS
```

- [Handbook](docs/reference/paint.md) ([Deutsch](docs/de/reference/paint.md))
- [Concept](docs/concept.md)
- [Implementation plan](docs/implementation_plan.md)
- [Development environment](docs/development_environment.md)

c0de paint requires c0de 0.7.0 or later. In the game, its handbook is a binder of the handbooks of c0de. For the program runner of c0de, the runner library `c0de-paint-runner-<version>.jar` adds the library; there, RENDER_CANVAS writes the placed blocks to a YAML file.

## License

BSD 3-Clause, see [LICENSE](LICENSE).
