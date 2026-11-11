# c0de net

![c0de net logo](docs/images/logo_cobweb.png)

A Minecraft mod that adds network libraries to [c0de](https://github.com/joernott/minecraft-c0de). The first one, `c0de_net/http`, lets c0de programs send HTTP requests to web servers and REST APIs and read the answers. The requests are sent by the game client of the player who runs the program, not by the server.

The mod supports Fabric and NeoForge on Minecraft 1.21.1, 1.21.11, 26.1.2, 26.2 and 26.3, like c0de, and needs c0de 0.4.0 or newer.

## Documentation

  - [Handbook](docs/reference/net.md): the type HTTP_CONNECTION, the functions HTTP_GET to HTTP_PUT, the errors, the settings and examples. With Patchouli on Minecraft 1.21.1, it is also a green book in the game, crafted from an INCLUDE block surrounded by eight strings.
  - [Handbuch](docs/de/reference/net.md): the handbook in German
  - [Concept](docs/concept.md): how the library works and why requests run on the client
  - [Implementation plan](docs/implementation_plan.md): sprints and tasks
  - [Development environment](docs/development_environment.md): tools, VM, CI
  - [Changelog](CHANGELOG.md)

## Program runner

`c0de-net-runner-<version>.jar`, attached to every GitHub release, brings `c0de_net/http` to the [program runner](https://github.com/joernott/minecraft-c0de/blob/main/docs/runner.md) of c0de, which runs programs without Minecraft. Put both jars on the class path (on Windows, separate them with `;`):

```sh
java -cp c0de-runner-0.4.0.jar:c0de-net-runner-<version>.jar de.joernott.c0de.core.runner.Main program.c0de
```

In the runner, the requests are sent by the runner itself.

## Building

Requirements: JDK 21 or newer to run Gradle. Gradle downloads the JDKs and the c0de jars the build needs.

```sh
./gradlew build                         # builds and tests everything, jars in versions/<node>/build/libs/
./gradlew :core:check                   # formatting and unit tests of the HTTP code (no Minecraft needed)
./gradlew :runner:check :runner:jar     # the runner library c0de-net-runner-<version>.jar and its tests
./gradlew :1.21.1-fabric:runGametest    # runs the GameTests on Fabric
./gradlew :1.21.1-fabric:runClient      # starts the Fabric client with c0de and c0de net
python scripts/generate_book.py         # generates the Patchouli book from the handbook
```

The project uses [Stonecutter](https://stonecutter.kikugie.dev/) like c0de: every combination of Minecraft version and loader is a node, e.g. `1.21.1-fabric`. The committed sources are in the state of the node `1.21.1-fabric`.

## License

BSD 3-clause license, see [LICENSE](LICENSE).
