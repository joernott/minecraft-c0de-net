```json
{
  "title": "HTTP_GET",
  "icon": "c0de:call_block",
  "category": "c0de_net_http",
  "ordinal": 1
}
```

Sends a GET request and returns the answer.

GET reads data. Redirects are followed, except from https to http. The results are those of the last answer. A status of 400 or higher raises HTTP\_STATUS, and the results are delivered as well.

**Parameters and results**

- `connection` STRUCT c0de\_net/http/HTTP\_CONNECTION: the server
- `endpoint` STRING: the path behind the base endpoint (optional, default empty)
- result `result` STRING: the raw data of the answer
- result `status` INTEGER: the HTTP status, like 200
- result `headers` HASH STRING: the headers of the answer, with lower-case names

**Example**

```c0de
CALL c0de_net/http/HTTP_GET
  PARAMETER connection api ;
  PARAMETER endpoint "/forecast?city=Berlin" ;
  RESULT result answer
```
