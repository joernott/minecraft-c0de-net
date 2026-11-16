```json
{
  "title": "HTTP_STATUS",
  "icon": "c0de:type_error_block",
  "category": "errors",
  "ordinal": 1
}
```

Code 1000, `c0de_net/http/HTTP_STATUS`: the server answered with a status of 400 or higher.

The message is the status line, like `404 Not Found`. The results `result`, `status` and `headers` are delivered as well, so a program that catches the error can read the answer.

**Example**

```c0de
CALL c0de_net/http/HTTP_GET problem
  PARAMETER connection api ;
  PARAMETER endpoint "/lamps/attic" ;
  RESULT status status
IF problem.code = c0de_net/http/HTTP_STATUS AND status = 404 ;
BEGIN
  CALL PRINT
    PARAMETER value "There is no lamp in the attic" ;
END IF
```
