```json
{
  "title": "HTTP_PUT",
  "icon": "c0de:call_block",
  "category": "c0de_net_http",
  "ordinal": 8
}
```

Sends a PUT request: stores something on the server, replacing what was there.

The data is sent as UTF-8. Set its type in the `Content-Type` header of the connection.

**Parameters and results**

- `connection` STRUCT c0de\_net/http/HTTP\_CONNECTION: the server
- `endpoint` STRING: the path behind the base endpoint (optional, default empty)
- `data` STRING: the data sent with the request (optional, default empty)
- result `result` STRING: the raw data of the answer
- result `status` INTEGER: the HTTP status
- result `headers` HASH STRING: the headers of the answer

**Example**

```c0de
CALL c0de_net/http/HTTP_PUT
  PARAMETER connection api ;
  PARAMETER endpoint "/lamps/kitchen" ;
  PARAMETER data state ;
  RESULT status status
```
