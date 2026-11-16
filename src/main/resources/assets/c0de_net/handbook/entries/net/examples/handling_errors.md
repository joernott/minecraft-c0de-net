```json
{
  "title": "Handling errors",
  "icon": "c0de:program_block",
  "category": "examples",
  "ordinal": 2
}
```

Checks whether a server answers and returns an exit code instead of crashing.

The exit code is 0 if the server answers, 1 if it answers with an error status and 2 if there is no answer. A comparator next to the trigger block reads it.

```c0de
PROGRAM ping
NOOP
INCLUDE c0de_net/http
NOOP
FUNCTION check
  PARAMETER host STRING
BEGIN
  VARIABLE api STRUCT c0de_net/http/HTTP_CONNECTION
  VARIABLE problem ERROR
  ASSIGN api.use_ssl TRUE ;
  ASSIGN api.server host ;
  ASSIGN api.timeout 5 ;
  CALL c0de_net/http/HTTP_HEAD problem
    PARAMETER connection api ;
  IF problem.code = c0de_net/http/HTTP_STATUS ;
  BEGIN
    EXIT 1 ;
  END IF
  IF problem.code ≠ 0 ;
  BEGIN
    EXIT 2 ;
  END IF
  EXIT 0 ;
END FUNCTION
```
