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
| 4 | Documentation and handbook | Handbook, Patchouli book, README, Modrinth page | ✅ |
| 5 | Release 0.1.0 | c0de net on Modrinth and GitHub for all nodes | ✅ |
| 6 | Documentation (0.2.0) | The handbook in German, and the programs of the c0de net tutorials on c0de.at are tested with every release | ✅ |
| 7 | Feedback from the recipes (0.3.0) | The handbook answers the questions of the recipes of c0de.at, the errors are constants, and c0de net is published on CurseForge |  |

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
| 4.1 | Handbook `docs/reference/net.md`: type, functions, results, errors, examples. | ✅ |
| 4.1.1 | GameTest: the complete programs of the handbook are valid with the real library. | ✅ |
| 4.2 | Patchouli book "c0de net handbook" generated from the handbook (1.21.1), with its recipe. | ✅ |
| 4.3 | README, CHANGELOG, `docs/modrinth.md`, logo. | ✅ |

## Sprint 5: Release 0.1.0

| Task | Description | Done |
|---|---|:---:|
| 5.1 | Modrinth project (created by the user: `P2irzbnD`, secret `MODRINTH_TOKEN` set; environment "client and server", c0de as required dependency) and release workflow as in c0de. | ✅ |
| 5.2 | Release 0.1.0 for all nodes. | ✅ |
| 5.3 | Alternative logo: the slashes on black, almost covered by the cobweb of Minecraft, which fills the picture (`docs/images/logo_cobweb.png`); it is the icon of the mod (user decision). | ✅ |

## Sprint 6: Documentation

**Goal:** the handbook reaches players who don't read English, and the programs of the c0de net tutorials on c0de.at are tested with every release. Planned on 2026-11-11 from the tutorial plan of www-c0de (`docs/tutorials.md` there), together with sprint 14 of c0de.

| Task | Description | Done |
|---|---|:---:|
| 6.1 | The program runner of c0de (c0de task 14.1) runs programs that include `c0de_net/http`: the release attaches the library of `core` as `c0de-net-runner-<version>.jar`, which the runner takes on its class path; requests are sent directly from the runner, like in the unit tests. The CI of www-c0de runs its c0de net tutorials with it against its own test service. Unit tests. | ✅ |
| 6.2 | German translation of the handbook: `docs/de/reference/net.md`, with the same `##` and `###` headings in the same order as the English file (the import of c0de.at matches sections by position). Names in programs stay English, comments and texts are translated. | ✅ |
| 6.3 | Tests of the translation: the GameTest of task 4.1.1 also validates the programs of `docs/de`, and a test checks that the headings match the English file. | ✅ |
| 6.4 | The Patchouli book in German (`de_de`), generated from `docs/de/reference/net.md`. | ✅ |
| 6.5 | Built-in STRING constant C0DE_NET_VERSION with the version of the installed c0de net, registered through the library API of c0de (c0de task 14.9), also in the program runner. Concept and handbook; unit tests and GameTest. Needed by the tutorial recipe "Is my c0de up to date?" of c0de.at. | ✅ |
| 6.6 | Release with the translation, the runner library and C0DE_NET_VERSION for all nodes, released on 2026-11-11 as 0.2.0 (GitHub pre-release with c0de-net-runner-0.2.0.jar, Modrinth). | ✅ |

## Sprint 7: Feedback from the recipes

**Goal:** the handbook answers the questions that the recipes of c0de.at (www-c0de sprint 9) ran into. Planned on 2026-11-12; the release is planned when the sprint starts.

| Task | Description | Done |
|---|---|:---:|
| 7.1 | INVALID_ARGUMENT is also raised for an endpoint that isn't a valid URL, e.g. with a space in its query (`?name=New York`); the handbook says so, says that the endpoint is used as it is and not encoded, and shows how to encode it (REPLACE of c0de/strings). The check was there already (`HttpConnection.uri`, tested); the handbook says it under HTTP_CONNECTION and INVALID_ARGUMENT and shows a loop of INSTR and REPLACE that turns every space into `+`. | ✅ |
| 7.2 | The examples of the handbook build their JSON with TO_JSON instead of string literals with `\"`, which the guide of c0de doesn't document (c0de task 15.9). HTTP_PATCH and the example Switching a lamp send a BOOLEAN variable with the tag `json: "on"` through TO_JSON. | ✅ |
| 7.3 | The handbook says whether the names of the errors (HTTP_STATUS, REQUEST_FAILED, …) can be used in programs like DIVISION_BY_ZERO; if not, c0de net registers them as constants through the library API of c0de. They can't. c0de net adds them as constants of the library, `c0de_net/http/HTTP_STATUS` to `c0de_net/http/RESPONSE_TOO_LARGE`, available with INCLUDE c0de_net/http like `c0de/math/PI`, so they don't need the prefix of the mod that global constants of other mods must have (ModLibraries.registerConstant of c0de). INVALID_ARGUMENT stays the constant of c0de. Runner test; the handbook uses them in its examples. | ✅ |
| 7.4 | The release publishes the mod on CurseForge as well as on Modrinth (like c0de task 15.11): the `curseforge {}` block of the mod-publish-plugin in both builds, `publish.curseforge_id` in `stonecutter.properties.toml`, the same files, versions, changelog and release type, required on client and server, Fabric API and c0de required and Patchouli optional; `release.yml` needs the repository secret `CURSEFORGE_TOKEN`; `development_environment.md` describes it. Needs the CurseForge project of c0de first, because c0de net depends on it there. Checked with a dry run before the next release. Done on 2026-11-12: 0.2.0 is on CurseForge (10 files, curseforge.yml with `without_c0de`, because CurseForge refuses c0de as a dependency while its project is under review). | ✅ |
| 7.4.1 | When the CurseForge project of c0de is approved: add c0de as a required dependency to the 10 files of 0.2.0 on curseforge.com. The next release has it again by itself. |  |
| 7.5 | c0de net is built and tested against c0de 0.5.0 (`deps.c0de`), which validates programs with c0de/minecraft in the program runner and prints BOOLEANs as TRUE and FALSE; 0.3.0 requires it. |  |
| 7.6 | Release 0.3.0 for all Minecraft versions (GitHub pre-release with the runner library, Modrinth and CurseForge). The CurseForge files require c0de, so the project of c0de must be approved first (7.4.1); otherwise CurseForge refuses the upload. |  |

## Risks

| Risk | Mitigation |
|---|---|
| The player who runs a process is offline, e.g. the owner of a timer. | CLIENT_UNAVAILABLE is a normal error that programs catch; the handbook shows how. |
| Answers are larger than the network allows in one packet. | Answers are sent in parts and limited by `maxResponseSize`. |
| GameTests have no real client. | The client side is behind an interface; GameTests use a stand-in that sends the request in the server JVM. The Fabric client GameTests cover the real path. |
| A new c0de version changes the library API. | The API of c0de is documented for other mods; c0de net is built against a fixed c0de version and tested in CI. |
