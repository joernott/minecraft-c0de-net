```json
{
  "title": "REQUEST_TIMEOUT",
  "icon": "c0de:type_error_block",
  "category": "errors",
  "ordinal": 5
}
```

Code 1004, `c0de_net/http/REQUEST_TIMEOUT`: there was no complete answer within the timeout of the connection.

The timeout covers the whole request, until the last byte of the answer. Raise `timeout` for slow servers, up to 300 seconds.
