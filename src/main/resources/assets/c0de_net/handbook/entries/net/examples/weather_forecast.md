```json
{
  "title": "Weather forecast",
  "icon": "c0de:program_block",
  "category": "examples",
  "ordinal": 0
}
```

Reads a forecast as JSON and prints it.

FROM\_JSON of c0de/strings reads the answer into a struct. The elements of the struct must match the keys of the JSON object.

```c0de
PROGRAM weather
NOOP
INCLUDE c0de_net/http
INCLUDE c0de/strings
NOOP
STRUCT forecast
BEGIN
  sky STRING
  temperature FLOAT
END STRUCT
NOOP
FUNCTION show
BEGIN
  VARIABLE api STRUCT c0de_net/http/HTTP_CONNECTION
  VARIABLE answer STRING
  VARIABLE today STRUCT forecast
  ASSIGN api.use_ssl TRUE ;
  ASSIGN api.server "weather.example.com" ;
  CALL c0de_net/http/HTTP_GET
    PARAMETER connection api ;
    PARAMETER endpoint "/today?city=Berlin" ;
    RESULT result answer
  CALL c0de/strings/FROM_JSON
    PARAMETER source answer ;
    PARAMETER destination today ;
  CALL PRINT
    PARAMETER value today.sky ⊕ ", " ⊕ today.temperature ⊕ " °C" ;
END FUNCTION
```
