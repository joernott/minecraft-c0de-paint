# Development environment

c0de paint uses the development environment of c0de and c0de net: the same tools, Gradle setup, VM and CI. This page only describes what differs; everything else is in the [development environment of c0de](https://github.com/joernott/minecraft-c0de/blob/main/docs/development_environment.md). The build was taken over from [c0de net](https://github.com/joernott/minecraft-c0de-net/blob/main/docs/development_environment.md).

## Build

- **Nodes**: the same Stonecutter nodes as c0de (`versions/<minecraft>-<loader>`), with the same Minecraft, loader, Fabric API and Parchment versions in `stonecutter.properties.toml`. When c0de adds a Minecraft version, c0de paint adds the same node.
- **`core`**: the canvas, the styles, the drawing functions, the transformations and the library `c0de_paint/cpaint` itself, in plain Java on the interpreter of c0de, without Minecraft or a mod loader.
  - It is formatted with Spotless and tested with JUnit (`./gradlew :core:check`). The tests run programs with the library on the interpreter of c0de, from the runner jar of the c0de release `deps.c0de`.
  - The nodes compile its sources directly; there, c0de itself provides the interpreter.
  - What needs the world (blocks of inventories, rendering, copying the world into a canvas) is an interface of `core` that the mod implements.
- **c0de**: every node is compiled and tested against the c0de jar of its Minecraft version and loader, in the version `deps.c0de`. The jars come from the [GitHub releases of c0de](https://github.com/joernott/minecraft-c0de/releases) (`buildSrc/src/main/kotlin/C0deDependency.kt`), as in c0de net. The mod requires c0de from `deps.c0de_min` (0.7.0, which introduced library types) up to the next major version.

Common commands:

| Command | What it does |
|---|---|
| `./gradlew compileJava` | Compiles all nodes |
| `./gradlew :core:check` | Formatting and unit tests of `core` |
| `./gradlew :<node>:runGametest` | GameTests of a Fabric node, e.g. `:1.21.1-fabric:runGametest` |
| `./gradlew :<node>:runGameTestServer` | GameTests of a NeoForge node |
| `./gradlew :<node>:runClient` | A development client with c0de and c0de paint |
| `scripts/check-whitespace.sh` | Whitespace of all files outside of `core` |
| `./gradlew :runner:check :runner:jar` | Tests and builds the runner library `c0de-paint-runner-<version>.jar`; the tests also check the programs of the handbook |
| `python scripts/generate_book.py` | Generates the handbook in the game (the book `c0de_paint:paint` of the handbooks of c0de), its crafting grid and the recipe of its binder from `docs/reference/paint.md` and its German translation; the CI fails if the committed files differ |
| `python scripts/generate_logo.py` | Draws the logo (`docs/images/logo.png`, 256 × 256, also the icon of the mod) and its CurseForge variant (`docs/images/logo_curseforge.png`, 400 × 400) |
| `python scripts/generate_recipe_images.py` | Draws the recipe of the binder for the handbook (`docs/reference/images`); needs the jars of c0de and Minecraft from a build of `1.21.1-fabric` |

## Development VM

The repository is not on a shared folder of the VM. The VM has its own clone in `~/src/minecraft-c0de-paint` (with `receive.denyCurrentBranch = updateInstead`); the workstation pushes the branch it tests to it:

```sh
GIT_SSH_COMMAND="ssh -i ~/.ssh/id_ed25519_claude" \
  git push ssh://doener@192.168.178.103/home/doener/src/minecraft-c0de-paint sprint-2
```

The tests then run in that clone, e.g. `./gradlew :1.21.1-neoforge:runGameTestServer`.

## GitHub Actions

`.github/workflows/build.yml` runs on every push: whitespace, the version of the active node, formatting and unit tests of `core` (Linux and Windows), and for every node the build and the GameTests. The jars are uploaded as artifacts. `.github/workflows/release.yml` and `curseforge.yml` publish a version as in c0de net: pushing the tag `v<mod.version>` builds and tests every node, publishes to Modrinth and CurseForge (secrets `MODRINTH_TOKEN` and `CURSEFORGE_TOKEN`, project IDs `publish.modrinth_id` and `publish.curseforge_id` in `stonecutter.properties.toml`, still empty) and creates a GitHub release with the jars, the runner library and the changelog section.
