```json
{
  "title": "Wettervorhersage",
  "icon": "c0de:program_block",
  "category": "examples",
  "ordinal": 0
}
```

Liest eine Vorhersage als JSON und gibt sie aus.

FROM\_JSON aus c0de/strings liest die Antwort in eine Struktur ein. Die Elemente der Struktur müssen zu den Schlüsseln des JSON-Objekts passen.

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
