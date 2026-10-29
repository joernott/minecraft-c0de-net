# Changelog

All notable changes of c0de net. The section of a version is also its changelog on Modrinth.

## 0.1.0

The first alpha of c0de net, for c0de 0.3.0 on Minecraft 1.21.1, 1.21.11, 26.1.2, 26.2 and 26.3, each for Fabric and NeoForge.

- **c0de_net/http**: a new c0de library for HTTP requests. `INCLUDE c0de_net/http` and call HTTP_GET, HTTP_HEAD, HTTP_OPTIONS, HTTP_TRACE, HTTP_DELETE, HTTP_PATCH, HTTP_POST or HTTP_PUT. Every function returns the raw data of the answer, its status and its headers.
- **HTTP_CONNECTION**: a struct with the server, port, base endpoint, basic authentication, HTTP proxy, headers, timeout and SSL settings, also for self-signed certificates.
- **Requests on the client**: the game of the player who runs the program sends the request, not the server: the player who pressed the button or flipped the lever, otherwise the owner of the trigger block. So programs reach what that player's computer reaches, like a home automation.
- **Waiting**: the CALL line waits for the answer while the server and other programs go on. Large answers are split for the network, up to 1 MiB by default.
- **Errors**: HTTP_STATUS (1000, with the answer still delivered), CLIENT_UNAVAILABLE (1001), REQUESTS_DISABLED (1002), REQUEST_FAILED (1003), REQUEST_TIMEOUT (1004) and RESPONSE_TOO_LARGE (1005).
- **Settings**: `config/c0de_net.properties` on the server (largest answer, requests per player) and `config/c0de_net-client.properties` for every player (`allowRequests`).
- **Handbook**: with Patchouli on Minecraft 1.21.1, a green book crafted from an INCLUDE block surrounded by eight strings.
