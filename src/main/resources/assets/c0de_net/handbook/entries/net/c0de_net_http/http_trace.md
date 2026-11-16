```json
{
  "title": "HTTP_TRACE",
  "icon": "c0de:call_block",
  "category": "c0de_net_http",
  "ordinal": 4
}
```

Sends a TRACE request: the server sends the request back as it received it.

Use it to see what a proxy changes. Many servers don't allow TRACE and answer with 405.

**Parameters and results**

- `connection` STRUCT c0de\_net/http/HTTP\_CONNECTION: the server
- `endpoint` STRING: the path behind the base endpoint (optional, default empty)
- result `result` STRING: the raw data of the answer
- result `status` INTEGER: the HTTP status
- result `headers` HASH STRING: the headers of the answer

**Example**

```c0de
CALL c0de_net/http/HTTP_TRACE
  PARAMETER connection api ;
  RESULT result echo
```
