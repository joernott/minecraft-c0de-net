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
| 7 | Feedback from the recipes (0.3.0) | The handbook answers the questions of the recipes of c0de.at, the errors are constants, and c0de net is published on CurseForge | ✅ |
| 8 | c0de 0.6.x and mail (0.4.0) | c0de net runs on c0de 0.6.1, its handbook is a binder of c0de's handbooks instead of a Patchouli book, the build tools are up to date, and the new library c0de_net/mail sends mail over SMTP and reads it over IMAP |  |

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
| 7.4.1 | When the CurseForge project of c0de is approved: add c0de as a required dependency to the 10 files of 0.2.0 on curseforge.com. The next release has it again by itself. Done by the user on 2026-11-13, after CurseForge approved c0de; c0de is also a default dependency of the project. | ✅ |
| 7.5 | c0de net is built and tested against c0de 0.5.0 (`deps.c0de`), which validates programs with c0de/minecraft in the program runner and prints BOOLEANs as TRUE and FALSE; 0.3.0 requires it. CI on 2026-11-13: unit tests and the GameTests of all 10 nodes passed with c0de 0.5.0. | ✅ |
| 7.6 | Release 0.3.0 for all Minecraft versions (GitHub pre-release with the runner library, Modrinth and CurseForge). The CurseForge files require c0de, so the project of c0de must be approved first (7.4.1); otherwise CurseForge refuses the upload. Released on 2026-11-13: the release workflow published 10 files on Modrinth and CurseForge, now with c0de as a dependency on CurseForge, and the GitHub pre-release with c0de-net-runner-0.3.0.jar. | ✅ |

## Sprint 8: c0de 0.6.x and mail

**Goal:** c0de net is built and tested against c0de 0.6.1, and its handbook is one of c0de's own handbooks instead of a Patchouli book: c0de 0.6.0 replaced Patchouli by handbooks of its own (c0de tasks 16.17–16.25), and 0.6.1 lets other mods ship them without code (c0de sprint 17). The update of the build tools (Dependabot) is included. A new library c0de_net/mail sends mail over SMTP and reads and manages mailboxes over IMAP. Planned on 2026-11-15, extended on 2026-11-15 with the update and the mail library (the modifications the user wanted before the release); version 0.4.0.

| Task | Description | Done |
|---|---|:---:|
| 8.1 | c0de net is built and tested against c0de 0.6.0 (`deps.c0de`). The development runs of the nodes before 26.1 load the libraries that c0de embeds (jar-in-jar) only when they are on the runtime classpath: `localRuntime` (Fabric) and the runtime configuration of NeoForge get kdbx (`de.soderer:kdbx`) and Bouncy Castle (`bcprov-jdk18on`) like snakeyaml, otherwise the GameTests of Fabric 1.21.1 crash with a ClassNotFoundException (found on the VM on 2026-11-15, c0de task 16.16). Unit tests, runner and the GameTests of all nodes.  Built against c0de 0.6.0 on 2026-11-16 (0.6.1 is not released yet: its release failed on 2026-11-15 in a flaky GameTest of c0de). |  |
| 8.1.1 | Found on 2026-11-16: the jars of the active node (1.21.1-fabric) reported C0DE_NET_VERSION 0.1.0 in every release, because Stonecutter writes the swap `mod_version` only into the generated sources of the other nodes (the same as c0de task 16.16.2). C0deNet.java refreshed, `scripts/check-version.sh` in CI. | ✅ |
| 8.2 | The handbook as a binder of c0de's handbooks (c0de concept 21-handbooks.md, handbooks of other mods since c0de 0.6.1, c0de sprint 17): `scripts/generate_book.py` writes it as Markdown pages for lavender-md in the namespace of c0de net (book `c0de_net:net`, English and German), with its crafting grids, the label colour of its binder and the recipe of the binder (a book and an item of c0de net), all as resources and data of c0de net; no code. Until c0de 0.6.1 is released, this is developed against its working copy. 0.4.0 requires c0de 0.6.1.  Generator and resources done on 2026-11-16; open: the check in the game and the recipe image `docs/reference/images/handbook.png` (it needs the textures of the binder of c0de 0.6.1), once c0de 0.6.1 is released. |  |
| 8.3 | Patchouli is removed: the Patchouli book and its recipe, the development dependency (`-Ppatchouli`, the mods folder of the NeoForge client), the optional dependency on Modrinth and CurseForge, and its mentions in README, Modrinth description, concept and development environment.  Done on 2026-11-16. | ✅ |
| 8.4 | The update of the build tools from Dependabot (pull request #2): Stonecutter 0.9.8 → 0.10, Spotless 8.10.4 → 8.11.0, Gradle 9.8.2 → 9.9.1. Its CI fails because Stonecutter 0.10 changed its API: `properties.get<String>(…)` takes no type argument any more (`stonecutter.gradle.kts`, line 22, and the build scripts). The branch of the pull request is merged into `sprint-8` (so the pull request is closed when `main` gets the sprint), and the build scripts are adapted to Stonecutter 0.10; CI and the GameTests of all nodes are green. (c0de uses Stonecutter 0.9.8 too; its update follows when Dependabot proposes it there.)  Done on 2026-11-16: also loom-back-compat 0.4.3, which works with Stonecutter 0.10. | ✅ |
| 8.5 | Concept of the library c0de_net/mail (a new chapter of the concept, English handbook first): the types `c0de_net/mail/SMTP_SERVER` and `c0de_net/mail/IMAP_SERVER` with everything a connection needs (host, port, security: none, STARTTLS or TLS, user name, password, timeout, and for SMTP the sender), a type for a message (sender, recipients, CC, BCC, subject, date, text, flags, the UID) and for the status of a mailbox; the function SMTP_SEND; the functions IMAP_CREATE, IMAP_DELETE, IMAP_RENAME, IMAP_LIST, IMAP_STATUS, IMAP_SELECT, IMAP_FETCH, IMAP_SEARCH, IMAP_STORE, IMAP_MOVE and IMAP_EXPUNGE with their parameters and results; errors from 1006 on. To decide: where the connections are made (like HTTP on the client of the player, so the server's network can't be reached through programs), the client setting that allows mail, limits (size of messages and fetched mail, number of messages per minute), passwords from the vault of c0de (SECRET, sensitive values), how IMAP_SELECT fits into calls that don't keep a connection, whether c0de net implements the protocols itself or embeds a mail library (e.g. Angus Mail, the Jakarta Mail implementation) in the jar, plain text and HTML, attachments.  Decided on 2026-11-15: mail is sent and read by the client of the player, like HTTP requests; the password is an element of the SMTP_SERVER and IMAP_SERVER variables like all other connection data, tagged `security: sensitive`, and the program decides how it fills them (e.g. from a SECRET); IMAP_SELECT stores the selected folder in the IMAP_SERVER variable, and the program passes the variable to the following functions; Angus Mail is embedded (angus-mail, jakarta.mail-api, jakarta.activation-api and angus-activation, about 0.8 MB together, no further dependencies); plain text only for now. Settings and limits, decided on 2026-11-15: the client setting `allowMail` (default true, like `allowRequests`) and the server setting `enableMail` (default true); at most 64 KB per message sent, 20 recipients per SMTP_SEND, 10 messages per minute per player, 1 MB of data per IMAP_FETCH, 100 messages per IMAP_FETCH or IMAP_SEARCH, a timeout of 30 s by default and at most; port 25 is blocked unless the configuration allows it (connections go to the submission ports of a provider, e.g. 465 and 587, not directly to receiving mail servers).  Concept written on 2026-11-16: [The library c0de_net/mail](concept.md#the-library-c0de_netmail); IMAP_SELECT takes the variable by reference; errors 1006 to 1009 (MAIL_DISABLED, AUTHENTICATION_FAILED, MAIL_REJECTED, MAIL_LIMIT) and PERMISSION_DENIED of c0de for port 25. | ✅ |
| 8.6 | SMTP_SERVER and SMTP_SEND: implementation in `core` and on the client, unit tests against a local test mail server (e.g. GreenMail), GameTests. |  |
| 8.7 | IMAP_SERVER and the functions for mailboxes: IMAP_CREATE, IMAP_DELETE, IMAP_RENAME, IMAP_LIST, IMAP_STATUS, IMAP_SELECT. Unit tests against the test mail server, GameTests. |  |
| 8.8 | The functions for messages: IMAP_FETCH, IMAP_SEARCH, IMAP_STORE, IMAP_MOVE, IMAP_EXPUNGE. Unit tests against the test mail server, GameTests. |  |
| 8.9 | The program runner library knows c0de_net/mail (validation; runs without a client like c0de_net/http), the handbook (English and German, with examples) and its binder pages, README, Modrinth description, changelog. The tutorials of c0de.at get recipes for every function (www-c0de task 13.7). |  |
| 8.10 | Release 0.4.0 for all Minecraft versions (GitHub pre-release with the runner library, Modrinth and CurseForge), requiring c0de 0.6.1. |  |

## Risks

| Risk | Mitigation |
|---|---|
| The player who runs a process is offline, e.g. the owner of a timer. | CLIENT_UNAVAILABLE is a normal error that programs catch; the handbook shows how. |
| Answers are larger than the network allows in one packet. | Answers are sent in parts and limited by `maxResponseSize`. |
| GameTests have no real client. | The client side is behind an interface; GameTests use a stand-in that sends the request in the server JVM. The Fabric client GameTests cover the real path. |
| A new c0de version changes the library API. | The API of c0de is documented for other mods; c0de net is built against a fixed c0de version and tested in CI. |
