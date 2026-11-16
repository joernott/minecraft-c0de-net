```json
{
  "title": "Eine Lampe schalten",
  "icon": "c0de:program_block",
  "category": "examples",
  "ordinal": 1
}
```

Schaltet eine Lampe einer Hausautomation ein oder aus.

Stell den Auslöserblock neben einen Hebel und gib ihm eine Konstantenkarte `token` mit dem Token deiner Hausautomation. Die Anfrage wird vom Computer des Spielers gesendet, der den Hebel umlegt, also können nur Spieler in deinem Heimnetz die Lampe schalten.

```c0de
PROGRAM lamp
NOOP
INCLUDE c0de_net/http
INCLUDE c0de/strings
NOOP
FUNCTION switch
  PARAMETER token STRING
  PARAMETER on BOOLEAN TRUE
  RESULT status INTEGER
BEGIN
  VARIABLE api STRUCT c0de_net/http/HTTP_CONNECTION
  VARIABLE state BOOLEAN TAG(json: "on")
  VARIABLE data STRING
  ASSIGN api.server "192.168.1.20" ;
  ASSIGN api.port 8123 ;
  ASSIGN api.base_endpoint "/api" ;
  ASSIGN api.headers["Authorization"] "Bearer " ⊕ token ;
  ASSIGN api.headers["Content-Type"] "application/json" ;
  ASSIGN state on ;
  CALL c0de/strings/TO_JSON
    PARAMETER value state ;
    RESULT result data
  CALL c0de_net/http/HTTP_PUT
    PARAMETER connection api ;
    PARAMETER endpoint "/lamps/kitchen" ;
    PARAMETER data data ;
    RESULT status status
END FUNCTION
```
