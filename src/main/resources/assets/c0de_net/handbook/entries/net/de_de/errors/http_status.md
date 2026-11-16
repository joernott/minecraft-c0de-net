```json
{
  "title": "HTTP_STATUS",
  "icon": "c0de:type_error_block",
  "category": "errors",
  "ordinal": 1
}
```

Code 1000, `c0de_net/http/HTTP_STATUS`: Der Server hat mit einem Status von 400 oder höher geantwortet.

Die Meldung ist die Statuszeile, z. B. `404 Not Found`. Die Ergebnisse `result`, `status` und `headers` werden trotzdem geliefert, so kann ein Programm, das den Fehler abfängt, die Antwort lesen.

**Beispiel**

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
