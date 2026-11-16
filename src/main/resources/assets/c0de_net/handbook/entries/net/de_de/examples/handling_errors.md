```json
{
  "title": "Fehler behandeln",
  "icon": "c0de:program_block",
  "category": "examples",
  "ordinal": 2
}
```

Prüft, ob ein Server antwortet, und gibt einen Exit-Code zurück, statt abzustürzen.

Der Exit-Code ist 0, wenn der Server antwortet, 1, wenn er mit einem Fehlerstatus antwortet, und 2, wenn keine Antwort kommt. Ein Komparator neben dem Auslöserblock liest ihn aus.

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
