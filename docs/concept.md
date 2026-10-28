# c0de net

## Abstract

c0de net is a Minecraft mod that adds the library `c0de_net/net` to the [c0de](https://github.com/joernott/minecraft-c0de) programming language. With it, c0de programs send HTTP requests to web servers and REST APIs and read the answers. It is inspired by the Go library [lra](https://github.com/joernott/lra), a low-level REST API client: a connection holds the base information (server, port, credentials, proxy, headers), and every HTTP method has a function that returns the raw data.

The requests are not sent by the Minecraft server but by the game client of the player who runs the process. The server's network can't be reached through programs, and a request only reaches what the computer of that player can reach: a program that switches the lamp of its owner's home automation can't be used by other players to do the same.

The requirement keywords (MUST, SHOULD, MAY, …) are used as in the [c0de concept](https://github.com/joernott/minecraft-c0de/blob/main/docs/concept.md#requirement-keywords).

## The mod

  - **Mod ID** `c0de_net`, **library** `c0de_net/net`. Programs include it with `INCLUDE c0de_net/net` and call its functions as `c0de_net/net/HTTP_GET`.
  - **Dependencies**: c0de is REQUIRED; without c0de, the mod has nothing to do. c0de net requires the version of c0de that was current at its release (0.3.0 for c0de net 0.1.0) or newer, for the same Minecraft version and loader.
  - **Versions**: the same Minecraft versions and loaders as c0de: 1.21.1, 1.21.11, 26.1.2, 26.2 and 26.3, each for Fabric and NeoForge. New Minecraft versions are added when c0de adds them.
  - **Environment**: client and server. The server runs the library, the client sends the requests. Both need the mod; a client without it can't send requests (see [Errors](#errors)).
  - **No blocks or items** of its own, except for its handbook (see [Documentation](#documentation)).

## Types

### HTTP_CONNECTION

`c0de_net/net/HTTP_CONNECTION` is a struct type with the base information of the requests to one server. Programs declare it with `VARIABLE api STRUCT c0de_net/net/HTTP_CONNECTION` and set its elements with ASSIGN. Every element has the default value of its type, and the defaults are the common case:

| Element | Type | Meaning | Default |
|---|---|---|---|
| `use_ssl` | BOOLEAN | true: `https`, false: `http` | false |
| `server` | STRING | Server name or IP address. MUST NOT be empty. | "" |
| `port` | INTEGER | Port, 1 to 65535. 0 means the default port of the protocol (80 or 443). | 0 |
| `base_endpoint` | STRING | Path added directly behind the port, e.g. `/api/v2`, so the functions only need the endpoint below it. | "" |
| `user` | STRING | User name for HTTP basic authentication; empty: no authentication. | "" |
| `password` | STRING | Password for HTTP basic authentication. | "" |
| `skip_ssl_validation` | BOOLEAN | true: certificates are not checked, which is needed for self-signed certificates. | false |
| `proxy` | STRING | URL of an HTTP proxy, e.g. `http://proxy.example.com:3128`; empty: no proxy. | "" |
| `headers` | HASH of STRING | Headers sent with every request, e.g. `Content-Type` → `application/json` or an `Authorization` header with a token. | empty |
| `timeout` | INTEGER | Seconds a request may take, 1 to 300. 0 means 60 seconds. | 0 |

Differences to lra:
  - lra's `ValidateSSL` becomes `skip_ssl_validation`, because a struct element starts with the default value of its type: a new connection validates certificates unless the program explicitly turns it off.
  - User and password are sent as an `Authorization: Basic …` header instead of being part of the URL (the Java HTTP client doesn't accept credentials in URLs). The effect is the same as in lra.
  - The derived fields of lra (`Protocol`, `BaseURL`, `Client`) are not part of the struct; they are computed for every request.
  - lra's SOCKS5 proxies (`ProxyIsSocks`) are not supported (user decision); `proxy` is always an HTTP proxy.

The URL of a request is `http[s]://server[:port]base_endpoint endpoint`, e.g. server `api.example.com`, base endpoint `/v2` and endpoint `/users/7` give `https://api.example.com/v2/users/7`. The endpoint is used as it is; it MAY contain a query (`/search?q=stone`). Programs encode special characters themselves (c0de/strings).

The password is stored in the program like every other value. Everybody who can read the sheet can read it; programs SHOULD read secrets from an input (INPUT, a laptop) instead of a literal.

## Functions

There is one function per HTTP method. All of them take the connection and an endpoint; the methods that send data also take the data:

| Function | Parameters | Sends data |
|---|---|---|
| `HTTP_GET` | connection, endpoint | no |
| `HTTP_HEAD` | connection, endpoint | no |
| `HTTP_OPTIONS` | connection, endpoint | no |
| `HTTP_TRACE` | connection, endpoint | no |
| `HTTP_DELETE` | connection, endpoint, data | yes |
| `HTTP_PATCH` | connection, endpoint, data | yes |
| `HTTP_POST` | connection, endpoint, data | yes |
| `HTTP_PUT` | connection, endpoint, data | yes |

Parameters:
  - `connection` (STRUCT c0de_net/net/HTTP_CONNECTION, required)
  - `endpoint` (STRING, default "")
  - `data` (STRING, default ""): the body of the request, sent as UTF-8. The content type is not set automatically; it comes from the headers of the connection.

Results:
  - `result` (STRING): the raw data of the response, decoded with the charset of its `Content-Type` header (UTF-8 if there is none; invalid bytes become �). For HEAD, which has no body, `result` contains the headers of the response as a JSON object, as in lra.
  - `status` (INTEGER): the HTTP status code, e.g. 200.
  - `headers` (HASH of STRING): the headers of the response, with lower-case names. Repeated headers are joined with ", ".

There are no `<VERB>JSON` variants: programs parse the raw data with JSON_PARSE of c0de/strings.

CONNECT, which lra also offers, is left out: it opens a tunnel through a proxy instead of returning data, and the Java HTTP client doesn't allow it.

### Example

```c0de
PROGRAM weather
NOOP
INCLUDE c0de_net/net
NOOP
FUNCTION forecast
    RESULT answer STRING
BEGIN
    VARIABLE api STRUCT c0de_net/net/HTTP_CONNECTION
    ASSIGN api.use_ssl TRUE ;
    ASSIGN api.server "api.example.com" ;
    ASSIGN api.base_endpoint "/v1" ;
    CALL c0de_net/net/HTTP_GET
        PARAMETER connection api ;
        PARAMETER endpoint "/forecast?city=Berlin" ;
        RESULT result answer
END FUNCTION
```

### Waiting for the answer

A request takes time, and the server MUST NOT wait for it. The function sends the request to the client and returns without results; the cursor stays on the CALL line (like INPUT, `NativeResult.waitForValue()`) until the answer arrives, an error occurs or the timeout of the connection has passed. Other processes and the server continue meanwhile.

A request that is still running when the process ends, is aborted or the server stops is cancelled. After a restart, a restored process calls the function again, so the request is sent a second time. Programs SHOULD keep this in mind for requests that change data (POST, PATCH, DELETE).

## Running requests on the client

  - The request is sent by the client of the **player who runs the process** (user decision):
    - If a player started the process (a button, a lever or a pressure plate at the trigger block, the activator of c0de), it is that player.
    - If nobody did (a timer block, a redstone clock, a mob on a pressure plate), it is the owner of the trigger block: the player who configured it and so chose to run the program this way.

    That player MUST be online and have c0de net installed. Another player who starts a program therefore never makes the computer of its owner send requests.
  - The server sends the description of the request (method, URL, headers, data, timeout, proxy, SSL setting) to that client. The client sends the request with the Java HTTP client in a thread of its own and sends the answer back.
  - Answers are sent to the server in parts, because a packet from a client to the server is limited to 32 KiB. The server puts them together.
  - The server limits the size of an answer (setting `maxResponseSize`, default 1 MiB). A larger answer is cancelled by the client and raises RESPONSE_TOO_LARGE.
  - Every player decides on their own computer whether their client sends requests: the client setting `allowRequests` (default true) in `c0de_net-client.properties`. With false, all requests of their processes raise REQUESTS_DISABLED.
  - On a single-player world, server and client run on the same computer; requests work the same way.
  - Apart from this, there are no limits: every program MAY call every host, including hosts in the network of the player who runs it (user decision).

## Errors

The functions raise the errors of c0de where they fit and errors of their own from 1000 on (as the [c0de library guide](https://github.com/joernott/minecraft-c0de/blob/main/docs/library-development.md#errors) asks):

| Code | Name | Handling | Description |
|---|---|---|---|
| 309 | INVALID_ARGUMENT | raise | The connection is not valid: empty server, port outside of 1 to 65535, timeout outside of 1 to 300, a proxy that is not an `http://` URL. |
| 1000 | HTTP_STATUS | raise | The server answered with a status of 400 or higher. The message is the status line, e.g. `404 Not Found`. The results (`result`, `status`, `headers`) are delivered as well, so a program that catches the error can read the answer. |
| 1001 | CLIENT_UNAVAILABLE | raise | The player who runs the process is not online, or their client doesn't have c0de net installed. Processes without such a player (e.g. a timer block without owner) can't send requests. |
| 1002 | REQUESTS_DISABLED | raise | The client of the player who runs the process doesn't send requests (`allowRequests` is false). |
| 1003 | REQUEST_FAILED | raise | The request could not be sent or had no answer: unknown host, connection refused, failed SSL handshake, proxy error. The message names the reason. |
| 1004 | REQUEST_TIMEOUT | raise | There was no complete answer within the timeout of the connection. |
| 1005 | RESPONSE_TOO_LARGE | raise | The answer is larger than `maxResponseSize`. |

## Settings

  - Server, `c0de_net.properties`: `maxResponseSize` (bytes, 1 KiB to 16 MiB, default 1 MiB), `maxRequestsPerPlayer` (requests that may run at the same time on the client of one player, 1 to 64, default 8; another one waits until one of them is done).
  - Client, `c0de_net-client.properties`: `allowRequests` (default true).

The files are created with the default values like `c0de.properties`; invalid entries are reported and replaced by the defaults.

## Documentation

As in c0de:
  - **Handbook** in the repository: `docs/reference/net.md` describes the type, the functions, the errors and examples, in the format of the c0de handbooks.
  - **Patchouli book** "c0de net handbook", generated from `docs/reference/net.md` like the handbooks of c0de, only on the Minecraft versions where c0de has Patchouli as optional dependency (1.21.1). It is crafted from a c0de INCLUDE block, the block that includes libraries, surrounded by eight strings: after all, it is about the World Wide Web (user decision).
  - **README**, **CHANGELOG** (whose sections are the changelogs on Modrinth) and the **Modrinth page** (`docs/modrinth.md`).
  - **Development environment** (`docs/development_environment.md`): the same tools, VM and CI as c0de.
