```json
{
  "title": "HTTP_PATCH",
  "icon": "c0de:call_block",
  "category": "c0de_net_http",
  "ordinal": 6
}
```

Sendet eine PATCH-Anfrage: ändert einen Teil von etwas auf dem Server.

Die Daten werden als UTF-8 gesendet. Setze ihren Typ im Header `Content-Type` der Verbindung, z. B. `application/json`.

**Parameter und Ergebnisse**

- `connection` STRUCT c0de\_net/http/HTTP\_CONNECTION: der Server
- `endpoint` STRING: der Pfad hinter dem Basis-Endpunkt (optional, Standard leer)
- `data` STRING: die Daten, die mit der Anfrage gesendet werden (optional, Standard leer)
- Ergebnis `result` STRING: die Rohdaten der Antwort
- Ergebnis `status` INTEGER: der HTTP-Status
- Ergebnis `headers` HASH STRING: die Header der Antwort

**Beispiel**

`state` ist eine BOOLEAN-Variable mit dem Tag `json: "on"`, darum schreibt TO\_JSON aus c0de/strings `{"on":false}` (siehe das Beispiel Eine Lampe schalten).

```c0de
CALL c0de/strings/TO_JSON
  PARAMETER value state ;
  RESULT result data
CALL c0de_net/http/HTTP_PATCH
  PARAMETER connection api ;
  PARAMETER endpoint "/lamps/kitchen" ;
  PARAMETER data data ;
  RESULT status status
```
