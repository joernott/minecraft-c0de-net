```json
{
  "title": "HTTP_POST",
  "icon": "c0de:call_block",
  "category": "c0de_net_http",
  "ordinal": 7
}
```

Sends a POST request: sends data to the server, e.g. to create something.

The data is sent as UTF-8. Set its type in the `Content-Type` header of the connection. Make the data with TO\_JSON of c0de/strings.

**Parameters and results**

- `connection` STRUCT c0de\_net/http/HTTP\_CONNECTION: the server
- `endpoint` STRING: the path behind the base endpoint (optional, default empty)
- `data` STRING: the data sent with the request (optional, default empty)
- result `result` STRING: the raw data of the answer
- result `status` INTEGER: the HTTP status
- result `headers` HASH STRING: the headers of the answer

**Example**

```c0de
CALL c0de_net/http/HTTP_POST
  PARAMETER connection api ;
  PARAMETER endpoint "/messages" ;
  PARAMETER data message ;
  RESULT result answer
```
