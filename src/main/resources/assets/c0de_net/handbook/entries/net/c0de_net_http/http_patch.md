```json
{
  "title": "HTTP_PATCH",
  "icon": "c0de:call_block",
  "category": "c0de_net_http",
  "ordinal": 6
}
```

Sends a PATCH request: changes a part of something on the server.

The data is sent as UTF-8. Set its type in the `Content-Type` header of the connection, like `application/json`.

**Parameters and results**

- `connection` STRUCT c0de\_net/http/HTTP\_CONNECTION: the server
- `endpoint` STRING: the path behind the base endpoint (optional, default empty)
- `data` STRING: the data sent with the request (optional, default empty)
- result `result` STRING: the raw data of the answer
- result `status` INTEGER: the HTTP status
- result `headers` HASH STRING: the headers of the answer

**Example**

`state` is a BOOLEAN variable with the tag `json: "on"`, so TO\_JSON of c0de/strings writes `{"on":false}` (see the example Switching a lamp).

```c0de
CALL c0de/strings/TO_JSON
  PARAMETER value state ;
  RESULT result data
CALL c0de_net/http/HTTP_PATCH
  PARAMETER connection api ;
  PARAMETER endpoint "/lamps/kitchen" ;
  PARAMETER data data ;
  RESULT status status
```
