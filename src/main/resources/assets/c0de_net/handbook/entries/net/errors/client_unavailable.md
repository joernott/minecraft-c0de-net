```json
{
  "title": "CLIENT_UNAVAILABLE",
  "icon": "c0de:type_error_block",
  "category": "errors",
  "ordinal": 3
}
```

Code 1001, `c0de_net/http/CLIENT_UNAVAILABLE`: there is no client to send the request.

The player who runs the process is not online, or their game doesn't have c0de net installed. A process that nobody started and whose trigger block has no owner can't send requests either.
