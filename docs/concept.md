# c0de net

## Abstract

c0de net is a Minecraft mod that adds the library `c0de_net/http` to the [c0de](https://github.com/joernott/minecraft-c0de) programming language. With it, c0de programs send HTTP requests to web servers and REST APIs and read the answers. It is inspired by the Go library [lra](https://github.com/joernott/lra), a low-level REST API client: a connection holds the base information (server, port, credentials, proxy, headers), and every HTTP method has a function that returns the raw data.

The requests are not sent by the Minecraft server but by the game client of the player who runs the process. The server's network can't be reached through programs, and a request only reaches what the computer of that player can reach: a program that switches the lamp of its owner's home automation can't be used by other players to do the same.

The requirement keywords (MUST, SHOULD, MAY, …) are used as in the [c0de concept](https://github.com/joernott/minecraft-c0de/blob/main/docs/concept.md#requirement-keywords).

## The mod

  - **Mod ID** `c0de_net`, **library** `c0de_net/http`. Since 0.4.0 also the **library** `c0de_net/mail` for SMTP and IMAP (see [The library c0de_net/mail](#the-library-c0de_netmail)); further libraries for other network protocols MAY follow (user decision); programs include them with `INCLUDE c0de_net/http` and `INCLUDE c0de_net/mail` and call their functions as `c0de_net/http/HTTP_GET` or `c0de_net/mail/SMTP_SEND`.
  - **Dependencies**: c0de is REQUIRED; without c0de, the mod has nothing to do. c0de net requires the version of c0de that was current at its release (0.3.0 for c0de net 0.1.0, 0.4.0 for 0.2.0, 0.5.0 for 0.3.0, 0.6.1 for 0.4.0) or newer, for the same Minecraft version and loader. Since c0de net 0.4.1 (task 9.3), it requires the oldest version of c0de it works with, `deps.c0de_min`, or any later version with the same major version (`>=0.6.1 <1.0.0`): the minimum is raised only when c0de net needs something new of c0de, like a changed API that requires a redesign. It is built and tested against `deps.c0de`, usually the newest c0de.
  - **Versions**: the same Minecraft versions and loaders as c0de: 1.21.1, 1.21.11, 26.1.2, 26.2 and 26.3, each for Fabric and NeoForge. New Minecraft versions are added when c0de adds them.
  - **Environment**: client and server. The server runs the library, the client sends the requests. Both need the mod; a client without it can't send requests (see [Errors](#errors)).
  - **No blocks or items** of its own, except for its handbook (see [Documentation](#documentation)).
  - **Version constant**: the mod registers the built-in constant `C0DE_NET_VERSION` (STRING) with its version, which every program reads without INCLUDE (since c0de 0.4.0, whose `ModLibraries.registerConstant` it uses).
  - **Program runner**: `c0de-net-runner-<version>.jar` brings `c0de_net/http` and `C0DE_NET_VERSION` to the program runner of c0de, which runs programs without Minecraft (e.g. to test the tutorials of c0de.at). There, the runner sends the requests itself.

## Types

### HTTP_CONNECTION

`c0de_net/http/HTTP_CONNECTION` is a struct type with the base information of the requests to one server. Programs declare it with `VARIABLE api STRUCT c0de_net/http/HTTP_CONNECTION` and set its elements with ASSIGN. Every element has the default value of its type, and the defaults are the common case:

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
| `headers` | HASH of STRING | Headers sent with every request, e.g. `Content-Type` → `application/json` or an `Authorization` header with a token. The headers that the HTTP client sets itself (`Host`, `Content-Length`, `Connection`, `Expect`, `Upgrade`) can't be set. | empty |
| `timeout` | INTEGER | Seconds a request may take, 1 to 300. 0 means 60 seconds. | 0 |

Differences to lra:
  - lra's `ValidateSSL` becomes `skip_ssl_validation`, because a struct element starts with the default value of its type: a new connection validates certificates unless the program explicitly turns it off.
  - User and password are sent as an `Authorization: Basic …` header instead of being part of the URL (the Java HTTP client doesn't accept credentials in URLs). The effect is the same as in lra; an `Authorization` header in `headers` wins, as in Go.
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
  - `connection` (STRUCT c0de_net/http/HTTP_CONNECTION, required)
  - `endpoint` (STRING, default "")
  - `data` (STRING, default ""): the body of the request, sent as UTF-8. The content type is not set automatically; it comes from the headers of the connection.

Results:
  - `result` (STRING): the raw data of the response, decoded with the charset of its `Content-Type` header (UTF-8 if there is none or it is unknown; invalid bytes become �). For HEAD, which has no body, `result` contains the headers of the response as a JSON object, as in lra, but with lower-case names in alphabetical order and one string per header, e.g. `{"content-length":"12","content-type":"text/plain"}`.
  - `status` (INTEGER): the HTTP status code, e.g. 200.
  - `headers` (HASH of STRING): the headers of the response, with lower-case names. Repeated headers are joined with ", ".

Redirects (301, 302, 303, 307, 308) are followed, except from `https` to `http`, as Go's HTTP client in lra does; the results are those of the last answer. The timeout of the connection covers the whole request, until the last byte of the answer.

There are no `<VERB>JSON` variants: programs read the raw data into variables with FROM_JSON of c0de/strings.

CONNECT, which lra also offers, is left out: it opens a tunnel through a proxy instead of returning data, and the Java HTTP client doesn't allow it.

### Example

```c0de
PROGRAM weather
NOOP
INCLUDE c0de_net/http
NOOP
FUNCTION forecast
    RESULT answer STRING
BEGIN
    VARIABLE api STRUCT c0de_net/http/HTTP_CONNECTION
    ASSIGN api.use_ssl TRUE ;
    ASSIGN api.server "api.example.com" ;
    ASSIGN api.base_endpoint "/v1" ;
    CALL c0de_net/http/HTTP_GET
        PARAMETER connection api ;
        PARAMETER endpoint "/forecast?city=Berlin" ;
        RESULT result answer
END FUNCTION
```

### Waiting for the answer

A request takes time, and the server MUST NOT wait for it. The function sends the request to the client and returns without results; the cursor stays on the CALL line (like INPUT, `NativeResult.waitForValue()`) until the answer arrives, an error occurs or the timeout of the connection has passed. Other processes and the server continue meanwhile.

A request that is still running when the process ends, is aborted or the server stops is cancelled: the server cancels a request whose process hasn't called its function for 100 ticks. This also happens to a process that the debugging tool pauses for longer; when it continues, it sends the request again. After a restart, a restored process calls the function again, so the request is sent a second time. Programs SHOULD keep this in mind for requests that change data (POST, PATCH, DELETE).

## Running requests on the client

  - The request is sent by the client of the **player who runs the process** (user decision):
    - If a player started the process (a button, a lever or a pressure plate at the trigger block, the activator of c0de), it is that player.
    - If nobody did (a timer block, a redstone clock, a mob on a pressure plate), it is the owner of the trigger block: the player who configured it and so chose to run the program this way.

    That player MUST be online and have c0de net installed. Another player who starts a program therefore never makes the computer of its owner send requests.
  - The server sends the description of the request (method, URL, headers, data, timeout, proxy, SSL setting) to that client. The client sends the request with the Java HTTP client in a thread of its own and sends the answer back.
  - Answers are sent to the server in parts, because a packet from a client to the server is limited to 32 KiB. The server puts them together.
  - The server limits the size of an answer (setting `maxResponseSize`, default 1 MiB). A larger answer is cancelled by the client and raises RESPONSE_TOO_LARGE.
  - The server waits for the answer for the timeout of the connection and 5 more seconds for the way back; then it raises REQUEST_TIMEOUT and cancels the request on the client.
  - Every player decides on their own computer whether their client sends requests: the client setting `allowRequests` (default true) in `c0de_net-client.properties`. With false, all requests of their processes raise REQUESTS_DISABLED.
  - On a single-player world, server and client run on the same computer; requests work the same way.
  - Apart from this, there are no limits: every program MAY call every host, including hosts in the network of the player who runs it (user decision).

## Errors

The functions raise the errors of c0de where they fit and errors of their own from 1000 on (as the [c0de library guide](https://github.com/joernott/minecraft-c0de/blob/main/docs/library-development.md#errors) asks):

| Code | Name | Handling | Description |
|---|---|---|---|
| 309 | INVALID_ARGUMENT | raise | The connection is not valid: empty server, port outside of 1 to 65535, timeout outside of 1 to 300, a proxy that is not an `http://` URL, a header that can't be sent. Or the request is larger than 1,000,000 bytes, the most a packet to a client may carry. |
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

## The library c0de_net/mail

Since 0.4.0, c0de net has a second library: `c0de_net/mail` sends mail over SMTP and reads and manages mailboxes over IMAP (decided on 2026-11-15). Like HTTP requests, the connections are made by the game client of the player who runs the process (see [Running requests on the client](#running-requests-on-the-client)), so the server's network can't be reached through programs. Mail is plain text; HTML and attachments are not supported (yet).

c0de net uses [Angus Mail](https://eclipse-ee4j.github.io/angus-mail/), the implementation of Jakarta Mail, embedded in its jar (angus-mail, jakarta.mail-api, jakarta.activation-api and angus-activation, about 0.8 MB together).

### Types

The connection data are struct types, like HTTP_CONNECTION. Programs declare them with `VARIABLE mail STRUCT c0de_net/mail/SMTP_SERVER` and set their elements with ASSIGN; every element has the default value of its type, and the defaults are the common case. The element `password` has the tag `security: "sensitive"` by its declaration, so the debugging tool shows `***` for it (c0de docs/concept/20-lib-security.md). The program decides how it fills it, e.g. from a SECRET of the vault of c0de.

**`c0de_net/mail/SMTP_SERVER`**: a server that accepts mail for sending.

| Element | Type | Meaning |
|---|---|---|
| `server` | STRING | the host name, e.g. `smtp.example.com` (required) |
| `port` | INTEGER | 0 (default): 465 for `tls`, 587 otherwise. Port 25 is blocked unless the server allows it (`allowPort25`) |
| `security` | STRING | `starttls` (default for ""), `tls` (an encrypted connection from the start) or `none` |
| `user` | STRING | the user name; "" sends without logging in |
| `password` | STRING | the password, sensitive |
| `from` | STRING | the address of the sender, e.g. `"Base computer <steve@example.com>"` (required) |
| `timeout` | INTEGER | seconds, 0 (default): 30; at most 30 (`mailTimeout`) |
| `skip_ssl_validation` | BOOLEAN | accept any certificate, e.g. of a server in the home network |

**`c0de_net/mail/IMAP_SERVER`**: a server with mailboxes.

| Element | Type | Meaning |
|---|---|---|
| `server` | STRING | the host name (required) |
| `port` | INTEGER | 0 (default): 993 for `tls`, 143 otherwise |
| `security` | STRING | `tls` (default for ""), `starttls` or `none` |
| `user` | STRING | the user name (required) |
| `password` | STRING | the password, sensitive |
| `timeout` | INTEGER | as above |
| `skip_ssl_validation` | BOOLEAN | as above |
| `folder` | STRING | the selected folder: IMAP_SELECT writes it into the variable; the functions for messages work in it |

**`c0de_net/mail/MAIL`**: a message that IMAP_FETCH read: `uid` INTEGER (its number in the folder), `from` STRING, `to` ARRAY STRING, `cc` ARRAY STRING, `subject` STRING, `date` TIME, `text` STRING (the plain text part; "" for a message without one), `flags` ARRAY STRING (e.g. `\Seen`), `size` INTEGER (bytes).

**`c0de_net/mail/MAILBOX`**: the status of a folder: `name` STRING, `messages` INTEGER, `recent` INTEGER, `unseen` INTEGER, `uid_next` INTEGER, `uid_validity` INTEGER.

### Functions

Every function opens a connection of its own, logs in, does its work and logs out; nothing stays open between two calls. The functions for messages work in the folder that IMAP_SELECT stored in the IMAP_SERVER variable; the program passes that variable to them. Without a selected folder they raise INVALID_ARGUMENT.

| Function | Parameters | Results | Description |
|---|---|---|---|
| SMTP_SEND | server SMTP_SERVER, to STRING or ARRAY STRING, subject STRING, text STRING, cc ARRAY STRING (default empty), bcc ARRAY STRING (default empty), reply_to STRING (default "") | result STRING | Sends a plain text message and returns its Message-ID. All recipients together (to, cc, bcc) are at most `maxRecipients`, the message at most `maxMailSize` bytes. |
| IMAP_LIST | server IMAP_SERVER, pattern STRING (default `*`) | result ARRAY STRING | The names of the folders that match the pattern (`*` any, `%` within one level). |
| IMAP_CREATE | server IMAP_SERVER, folder STRING | – | Creates a folder. |
| IMAP_DELETE | server IMAP_SERVER, folder STRING | – | Deletes a folder. |
| IMAP_RENAME | server IMAP_SERVER, folder STRING, new_name STRING | – | Renames a folder. |
| IMAP_STATUS | server IMAP_SERVER, folder STRING (default: the selected folder) | result MAILBOX | The status of a folder, without selecting it. |
| IMAP_SELECT | server IMAP_SERVER (a variable, by reference), folder STRING (default `INBOX`) | result MAILBOX | Checks that the folder exists and stores its name in `folder` of the variable. |
| IMAP_SEARCH | server IMAP_SERVER, criteria STRING (default `ALL`) | result ARRAY INTEGER | The UIDs of the messages of the selected folder that match the criteria, oldest first; at most `maxFetchMessages`, the newest. The criteria are those of IMAP: `ALL`, `UNSEEN`, `SEEN`, `FLAGGED`, `FROM text`, `TO text`, `SUBJECT text`, `BODY text`, `SINCE 1-Nov-2026`, `BEFORE 1-Nov-2026`, combined by spaces (all must match). |
| IMAP_FETCH | server IMAP_SERVER, uids INTEGER or ARRAY INTEGER, text BOOLEAN (default TRUE) | result ARRAY MAIL | The messages with these UIDs, without `text` if text is FALSE. At most `maxFetchMessages` messages and `maxFetchSize` bytes (RESPONSE_TOO_LARGE). Reading the text marks a message as seen, like a mail program does. |
| IMAP_STORE | server IMAP_SERVER, uids INTEGER or ARRAY INTEGER, flags STRING or ARRAY STRING, mode STRING (default `add`) | – | Adds, removes (`remove`) or replaces (`set`) flags: `\Seen`, `\Answered`, `\Flagged`, `\Deleted`, `\Draft` or keywords of the server. |
| IMAP_MOVE | server IMAP_SERVER, uids INTEGER or ARRAY INTEGER, folder STRING | – | Moves messages into another folder. |
| IMAP_EXPUNGE | server IMAP_SERVER | result INTEGER | Removes the messages of the selected folder that have the flag `\Deleted` and returns how many. |

A UID that doesn't exist is left out (IMAP_FETCH returns fewer messages); a folder that doesn't exist raises MAIL_REJECTED.

### Errors of the mail library

The library has the errors of c0de net as constants of its own, e.g. `c0de_net/mail/MAIL_REJECTED`, and four more:

| Code | Name | Handling | Description |
|---|---|---|---|
| 309 | INVALID_ARGUMENT | raise | A connection without server, a port outside of 1 to 65535, an unknown `security`, an SMTP_SERVER without `from`, an invalid address, a function for messages without selected folder, an unknown `mode`. |
| 311 | PERMISSION_DENIED | raise | Port 25 while `allowPort25` is false. |
| 1001 | CLIENT_UNAVAILABLE | raise | As for HTTP. |
| 1003 | REQUEST_FAILED | raise | The connection failed: unknown host, connection refused, failed TLS handshake. |
| 1004 | REQUEST_TIMEOUT | raise | No answer within the timeout. |
| 1005 | RESPONSE_TOO_LARGE | raise | IMAP_FETCH would read more than `maxFetchSize` bytes. |
| 1006 | MAIL_DISABLED | raise | Mail is switched off: by the server (`enableMail`) or by the client of the player (`allowMail`). |
| 1007 | AUTHENTICATION_FAILED | raise | The server didn't accept the user name or the password. |
| 1008 | MAIL_REJECTED | raise | The server refused the command: a recipient it doesn't accept, a folder that doesn't exist or already exists. The message is the answer of the server. |
| 1009 | MAIL_LIMIT | raise | More recipients than `maxRecipients`, a larger message than `maxMailSize`, or more than `maxMailsPerMinute` messages of one player in a minute. |

### Settings of the mail library

  - Server, `c0de_net.properties`: `enableMail` (default true), `maxMailSize` (bytes of a message sent, default 65536), `maxRecipients` (per SMTP_SEND, default 20), `maxMailsPerMinute` (messages sent per player, default 10), `maxFetchSize` (bytes per IMAP_FETCH, default 1 MiB), `maxFetchMessages` (messages per IMAP_FETCH and IMAP_SEARCH, default 100), `mailTimeout` (seconds, the default and the maximum of `timeout`, default 30), `allowPort25` (default false: connections go to the submission ports of a mail provider, e.g. 465 and 587, not directly to the servers that receive mail, which spammers use).
  - Client, `c0de_net-client.properties`: `allowMail` (default true): every player decides whether their client sends and reads mail.

The limits were decided on 2026-11-15. The program runner of c0de uses the defaults and connects directly, like for HTTP.

## Documentation

As in c0de:
  - **Handbook** in the repository: `docs/reference/net.md` describes the type, the functions, the errors and examples, in the format of the c0de handbooks.
  - **Handbook in the game** "c0de net Handbook", the book `c0de_net:net` of the handbooks of c0de (c0de docs/concept/21-handbooks.md; handbooks of other mods since c0de 0.6.1; until 0.3.0 a Patchouli book), generated from `docs/reference/net.md` like the handbooks of c0de, on every Minecraft version. Its binder is the generic binder of c0de with a teal label. It is crafted from a c0de INCLUDE block, the block that includes libraries, surrounded by eight strings: after all, it is about the World Wide Web (user decision).
  - **README**, **CHANGELOG** (whose sections are the changelogs on Modrinth) and the **Modrinth page** (`docs/modrinth.md`).
  - **Development environment** (`docs/development_environment.md`): the same tools, VM and CI as c0de.
