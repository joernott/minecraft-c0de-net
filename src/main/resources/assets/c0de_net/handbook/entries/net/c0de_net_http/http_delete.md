```json
{
  "title": "HTTP_DELETE",
  "icon": "c0de:call_block",
  "category": "c0de_net_http",
  "ordinal": 5
}
```

Sends a DELETE request: removes something on the server.

Some APIs expect data with it, most don't.

**Parameters and results**

- `connection` STRUCT c0de\_net/http/HTTP\_CONNECTION: the server
- `endpoint` STRING: the path behind the base endpoint (optional, default empty)
- `data` STRING: the data sent with the request, in UTF-8 (optional, default empty)
- result `result` STRING: the raw data of the answer
- result `status` INTEGER: the HTTP status
- result `headers` HASH STRING: the headers of the answer

**Example**

```c0de
CALL c0de_net/http/HTTP_DELETE
  PARAMETER connection api ;
  PARAMETER endpoint "/timers/3" ;
  RESULT status status
```
