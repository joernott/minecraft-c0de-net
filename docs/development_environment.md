# Development environment

c0de net uses the development environment of c0de: the same tools, Gradle setup, VM and CI. This page only describes what differs; everything else is in the [development environment of c0de](https://github.com/joernott/minecraft-c0de/blob/main/docs/development_environment.md).

## Build

  - **Nodes**: the same Stonecutter nodes as c0de (`versions/<minecraft>-<loader>`), with the same Minecraft, loader, Fabric API and Parchment versions in `stonecutter.properties.toml`. When c0de adds a Minecraft version, c0de net adds the same node.
  - **`core`**: requests and answers in plain Java, without Minecraft, a loader or c0de; formatted with Spotless and tested with JUnit (`./gradlew :core:check`). The nodes compile its sources directly.
  - **c0de**: every node is compiled and tested against the c0de jar of its Minecraft version and loader, in the version `deps.c0de`. The jars come from the [GitHub releases of c0de](https://github.com/joernott/minecraft-c0de/releases) (`buildSrc/src/main/kotlin/C0deDependency.kt`). A new c0de version is used by changing `deps.c0de`.
  - **c0de classes**: besides the library API (`ModLibraries`, `Library`, `NativeFunction`, values), c0de net uses the loader abstraction of c0de (`de.joernott.c0de.platform.Platform`) for networking, so it doesn't need one of its own.

Common commands:

| Command | What it does |
|---|---|
| `./gradlew compileJava` | Compiles all nodes |
| `./gradlew :core:check` | Formatting and unit tests of `core` |
| `./gradlew :<node>:runGametest` | GameTests of a Fabric node, e.g. `:1.21.1-fabric:runGametest` |
| `./gradlew :<node>:runGameTestServer` | GameTests of a NeoForge node |
| `./gradlew :<node>:runClient` | A development client with c0de and c0de net |
| `scripts/check-whitespace.sh` | Whitespace of all files outside of `core` |

## Development VM

The repository is not on a shared folder of the VM. The VM has its own clone in `~/src/minecraft-c0de-net` (with `receive.denyCurrentBranch = updateInstead`); the workstation pushes the branch it tests to it:

```sh
GIT_SSH_COMMAND="ssh -i ~/.ssh/id_ed25519_claude" \
  git push ssh://doener@192.168.178.101/home/doener/src/minecraft-c0de-net sprint-1
```

The tests then run in that clone, e.g. `./gradlew :1.21.1-neoforge:runGameTestServer`.

## GitHub Actions

`.github/workflows/build.yml` runs on every push: whitespace, formatting and unit tests of `core` (Linux and Windows), and for every node the build and the GameTests. The jars are uploaded as artifacts. The release workflow follows in sprint 5; it needs the secret `MODRINTH_TOKEN` and `publish.modrinth_id` (`P2irzbnD`).
