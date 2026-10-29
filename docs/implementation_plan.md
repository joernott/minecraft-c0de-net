# Implementation plan

This plan implements the [c0de net concept](concept.md) in sprints. Each sprint has a goal, numbered tasks and a result that can be demonstrated. The rules are the same as in [c0de](https://github.com/joernott/minecraft-c0de/blob/main/docs/implementation_plan.md).

## Principles

  - **The same versions as c0de from the start.** Every sprint delivers all nodes of c0de: 1.21.1, 1.21.11, 26.1.2, 26.2 and 26.3, each for Fabric and NeoForge. The build (Stonecutter, Loom, ModDevGradle) is taken over from c0de.
  - **Plain Java where possible.** The module `core` contains everything that doesn't need Minecraft: the connection, the request description, the HTTP client and the conversion of answers. It is developed test-first with JUnit against a local HTTP server.
  - **Thin Minecraft layer.** The mod only registers the library, carries requests to the client and answers back, and runs the client side.
  - **Every sprint ends with a working mod.** CI is green on all nodes.

## Branching and commits

  - `sprint-0` contains the concept and this plan and shares its commit with `main`.
  - Every sprint n gets a branch `sprint-n`, created from the tip of `sprint-(n-1)`. It contains exactly the commits of that sprint.
  - Commit messages start with the task they close: `Task 2.3: Send requests with the Java HTTP client`.
  - At the end of a sprint, the branch is merged into `main` once CI is green.
  - Every prompt is logged in [zz_prompts.md](zz_prompts.md).

## Tracking

  - Completed tasks are marked with ✅ in the column "Done" of their sprint table. A sprint is marked as done in the sprint overview when all of its tasks are done.
  - If an additional task turns out to be necessary to complete a task, it is added below that task with the next level number: an additional task for 1.1 becomes 1.1.1.

## Definition of done

A task is done when
  - the code works on all nodes,
  - unit tests (`core`) or GameTests (mod) cover the behavior described in the concept, including the error cases,
  - CI is green,
  - the concept is updated if the implementation required a decision that the concept doesn't cover yet.

## Sprint overview

| Sprint | Goal | Result | Done |
|---|---|---|:---:|
| 0 | Concept and plan | Concept and implementation plan | ✅ |
| 1 | Project skeleton | The mod loads on all nodes with c0de; programs can include `c0de_net/http`; CI and VM are ready | ✅ |
| 2 | Requests in `core` | Connections become requests, which are sent with the Java HTTP client; answers become results | ✅ |
| 3 | Requests on the client | The functions send requests through the client of the player who runs the process and wait for the answer | ✅ |
| 4 | Documentation and handbook | Handbook, Patchouli book, README, Modrinth page |  |
| 5 | Release 0.1.0 | c0de net on Modrinth and GitHub for all nodes |  |

## Sprint 0: Concept and plan

| Task | Description | Done |
|---|---|:---:|
| 0.1 | Concept: type, functions, client execution, errors, settings, documentation. | ✅ |
| 0.2 | Implementation plan. | ✅ |
| 0.3 | Review of the open questions of the concept with the user: the client of the player who runs the process sends the requests, no SOCKS5 proxies, the handbook is crafted from an INCLUDE block and eight strings. | ✅ |

## Sprint 1: Project skeleton

**Goal:** the empty mod builds, loads and is tested on all nodes, exactly like c0de.

| Task | Description | Done |
|---|---|:---:|
| 1.1 | Build of c0de taken over: Stonecutter with the ten nodes, `core` module, Gradle wrapper, Spotless, `.gitignore`, `.gitattributes`, whitespace check. || ✅ |
| 1.2 | c0de as dependency of every node from the Modrinth Maven repository (c0de 0.3.0 of the node's version and loader), required in `fabric.mod.json` and `neoforge.mods.toml`, available in the development runs. || ✅ |
| 1.3 | Mod entry points for Fabric and NeoForge (client and server); the library `c0de_net/http` is registered with `ModLibraries` (empty for now). || ✅ |
| 1.4 | GameTests on every node: the library is registered, a program with `INCLUDE c0de_net/http` is valid. || ✅ |
| 1.5 | GitHub Actions: checks, unit tests (Linux and Windows), build and GameTests per node; Dependabot as in c0de. The client GameTests follow with the client code (3.6). || ✅ |
| 1.6 | VM: a clone of the repository builds and tests all nodes; `docs/development_environment.md`. || ✅ |

## Sprint 2: Requests in `core`

**Goal:** everything about a request works in plain Java and is covered by unit tests.

| Task | Description | Done |
|---|---|:---:|
| 2.1 | Connection: the values of HTTP_CONNECTION, their validation (INVALID_ARGUMENT) and the URL of a request. || ✅ |
| 2.2 | Request description: method, URL, headers (including basic authentication), data, timeout, proxy, SSL setting; serializable for the network. || ✅ |
| 2.3 | Sending requests with `java.net.http.HttpClient`: all eight methods, HTTP proxies, skipped SSL validation (own trust manager, without hostname check), timeout, cancellation, size limit. Unit tests against `com.sun.net.httpserver.HttpServer` (also with a self-signed certificate). || ✅ |
| 2.4 | Answers: decoding with the charset of the answer, headers as HASH, headers as JSON for HEAD, HTTP_STATUS for status ≥ 400 with results. || ✅ |
| 2.5 | Error codes 1000 to 1005 with their messages. || ✅ |

## Sprint 3: Requests on the client

**Goal:** programs in the game send requests through the client of the player who runs them.

| Task | Description | Done |
|---|---|:---:|
| 3.1 | Type HTTP_CONNECTION and the functions HTTP_GET, HTTP_HEAD, HTTP_OPTIONS, HTTP_TRACE, HTTP_DELETE, HTTP_PATCH, HTTP_POST, HTTP_PUT in the library. || ✅ |
| 3.2 | Network: request payload to the client, answer in parts of at most 32 KiB to the server, cancellation; both loaders. || ✅ |
| 3.3 | Pending requests on the server: the function waits (`waitForValue`) until the answer, an error or the timeout; requests of ended processes are cancelled; `maxRequestsPerPlayer`. Check that results are delivered together with HTTP_STATUS. || ✅ |
| 3.4 | Client: the player who runs the process (activator, otherwise the owner of the trigger block); requests in a thread of their own, `allowRequests`; CLIENT_UNAVAILABLE for players that are offline or don't have the mod. || ✅ |
| 3.5 | Settings `c0de_net.properties` and `c0de_net-client.properties`. || ✅ |
| 3.6 | GameTests with a local HTTP server and a client stand-in on every node; client GameTests (Fabric) with the real client. || ✅ |

## Sprint 4: Documentation and handbook

**Goal:** players learn how to use the library in the game and on the web.

| Task | Description | Done |
|---|---|:---:|
| 4.1 | Handbook `docs/reference/net.md`: type, functions, results, errors, examples. |  |
| 4.1.1 | GameTest: the complete programs of the handbook are valid with the real library. |  |
| 4.2 | Patchouli book "c0de net handbook" generated from the handbook (1.21.1), with its recipe. |  |
| 4.3 | README, CHANGELOG, `docs/modrinth.md`, logo. |  |

## Sprint 5: Release 0.1.0

| Task | Description | Done |
|---|---|:---:|
| 5.1 | Modrinth project (created by the user: `P2irzbnD`, secret `MODRINTH_TOKEN` set; environment "client and server", c0de as required dependency) and release workflow as in c0de. |  |
| 5.2 | Release 0.1.0 for all nodes. |  |

## Risks

| Risk | Mitigation |
|---|---|
| The player who runs a process is offline, e.g. the owner of a timer. | CLIENT_UNAVAILABLE is a normal error that programs catch; the handbook shows how. |
| Answers are larger than the network allows in one packet. | Answers are sent in parts and limited by `maxResponseSize`. |
| GameTests have no real client. | The client side is behind an interface; GameTests use a stand-in that sends the request in the server JVM. The Fabric client GameTests cover the real path. |
| A new c0de version changes the library API. | The API of c0de is documented for other mods; c0de net is built against a fixed c0de version and tested in CI. |
