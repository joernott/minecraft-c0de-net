```json
{
  "title": "RESPONSE_TOO_LARGE",
  "icon": "c0de:type_error_block",
  "category": "errors",
  "ordinal": 7
}
```

Code 1005, `c0de_net/http/RESPONSE_TOO_LARGE`: the answer is larger than the server allows.

The limit is `maxResponseSize` in `config/c0de_net.properties`, 1 MiB by default. Ask the API for less data, e.g. one page of a list.

For IMAP\_FETCH, the limit is `maxFetchSize`. Read fewer messages at once, or without their text.
