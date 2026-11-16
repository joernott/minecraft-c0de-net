# c0de net

**The web for your c0de programs.** c0de net adds the libraries `c0de_net/http` and `c0de_net/mail` to [c0de](https://modrinth.com/mod/c0de), the programming language made of blocks. Your programs send HTTP requests to web servers and REST APIs and read the answers: check the weather, post a message to a chat bot, or switch the lamp of your home automation with a lever in Minecraft. And they send and read mail: a message when the farm is full, or orders read from a mailbox.

> This is an early version. Expect rough edges, and please report bugs and ideas on GitHub.

## Features

- **One function per HTTP method**: HTTP_GET, HTTP_HEAD, HTTP_OPTIONS, HTTP_TRACE, HTTP_DELETE, HTTP_PATCH, HTTP_POST and HTTP_PUT. Each returns the raw data of the answer, its status and its headers. Read JSON into your variables with FROM_JSON of c0de/strings.
- **HTTP_CONNECTION**: one struct holds the server, port, base endpoint, user and password, headers like an API token, an HTTP proxy, the timeout and whether to accept self-signed certificates.
- **Mail**: SMTP_SEND sends plain text messages through your mail provider. IMAP_LIST, IMAP_CREATE, IMAP_DELETE, IMAP_RENAME, IMAP_STATUS, IMAP_SELECT, IMAP_SEARCH, IMAP_FETCH, IMAP_STORE, IMAP_MOVE and IMAP_EXPUNGE read and manage mailboxes. Passwords are sensitive values the debugging tool doesn't show.
- **Your computer sends the request**: not the Minecraft server, but the game of the player who runs the program: whoever pressed the button, otherwise the owner of the trigger block. Your program reaches your home network, and another player who presses your button sends the request from their own computer, never from yours.
- **No freezing**: the program waits for the answer while the server and all other programs go on.
- **Errors you can catch**: an error status, a player who is offline, a server that doesn't answer or an answer that is too large each raise an error of its own. The answer to an error status is still delivered.
- **Settings**: the server limits the size of answers, the requests per player and the mail its players send and read, and blocks port 25 against spam. Every player can turn off requests and mail from their own computer.

## Example

```
PROGRAM weather
NOOP
INCLUDE c0de_net/http
NOOP
FUNCTION forecast
    RESULT answer STRING
BEGIN
    VARIABLE api STRUCT c0de_net/http/HTTP_CONNECTION
    ASSIGN api.use_ssl TRUE ;
    ASSIGN api.server "weather.example.com" ;
    CALL c0de_net/http/HTTP_GET
        PARAMETER connection api ;
        PARAMETER endpoint "/today?city=Berlin" ;
        RESULT result answer
END FUNCTION
```

## Documentation

- [Handbook](https://github.com/joernott/minecraft-c0de-net/blob/main/docs/reference/net.md): the connections, every function, every error, the settings and complete examples
- [Concept](https://github.com/joernott/minecraft-c0de-net/blob/main/docs/concept.md): how requests travel from the server to the client and back

## Requirements

- [c0de](https://modrinth.com/mod/c0de) 0.6.1 or newer, for the same Minecraft version and loader
- Minecraft 1.21.1, 1.21.11, 26.1.2, 26.2 or 26.3 (26.x needs Java 25)
- **Fabric** with [Fabric API](https://modrinth.com/mod/fabric-api), or **NeoForge**
- On the server **and** on the clients. On Fabric, a player without c0de net can join, but their programs can't send requests; a NeoForge server requires c0de net on every client.
- The handbook is in the game too, as a binder of the handbooks of c0de, crafted from an INCLUDE block surrounded by eight strings.
