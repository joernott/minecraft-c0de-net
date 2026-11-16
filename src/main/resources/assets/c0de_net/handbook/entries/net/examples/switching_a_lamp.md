```json
{
  "title": "Switching a lamp",
  "icon": "c0de:program_block",
  "category": "examples",
  "ordinal": 1
}
```

Turns a lamp of a home automation on or off.

Put the trigger block next to a lever and give it a constant item `token` with the token of your home automation. The request is sent from the computer of the player who flips the lever, so only players in your home network can switch it.

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
