```json
{
  "title": "Settings",
  "icon": "minecraft:writable_book",
  "category": "getting_started",
  "ordinal": 2
}
```

The server has the file `config/c0de_net.properties`. `maxResponseSize` limits the size of an answer in bytes (1 KiB to 16 MiB, default 1 MiB). `maxRequestsPerPlayer` limits how many requests run at the same time on the client of one player (1 to 64, default 8)\; more requests wait.

Every player has the file `config/c0de_net-client.properties`. With `allowRequests=false`, their client sends no requests, and the requests of their processes raise REQUESTS\_DISABLED.

Both files are created with the default values. Invalid entries are reported in the log and replaced by the defaults.
