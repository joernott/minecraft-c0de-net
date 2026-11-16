```json
{
  "title": "HTTP_OPTIONS",
  "icon": "c0de:call_block",
  "category": "c0de_net_http",
  "ordinal": 3
}
```

Sends an OPTIONS request: asks the server which methods an endpoint supports.

Many servers answer in the header `allow`.

**Parameters and results**

- `connection` STRUCT c0de\_net/http/HTTP\_CONNECTION: the server
- `endpoint` STRING: the path behind the base endpoint (optional, default empty)
- result `result` STRING: the raw data of the answer
- result `status` INTEGER: the HTTP status
- result `headers` HASH STRING: the headers of the answer

**Example**

```c0de
CALL c0de_net/http/HTTP_OPTIONS
  PARAMETER connection api ;
  PARAMETER endpoint "/lamps" ;
  RESULT headers answerHeaders
```
