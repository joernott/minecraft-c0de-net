```json
{
  "title": "HTTP_HEAD",
  "icon": "c0de:call_block",
  "category": "c0de_net_http",
  "ordinal": 2
}
```

Sends a HEAD request: like GET, but the answer has no data.

`result` holds the headers of the answer as a JSON object instead, with lower-case names in alphabetical order, like `{"content-length":"12"}`.

**Parameters and results**

- `connection` STRUCT c0de\_net/http/HTTP\_CONNECTION: the server
- `endpoint` STRING: the path behind the base endpoint (optional, default empty)
- result `result` STRING: the headers as JSON
- result `status` INTEGER: the HTTP status
- result `headers` HASH STRING: the headers of the answer

**Example**

```c0de
CALL c0de_net/http/HTTP_HEAD
  PARAMETER connection api ;
  PARAMETER endpoint "/status" ;
  RESULT status status
```
