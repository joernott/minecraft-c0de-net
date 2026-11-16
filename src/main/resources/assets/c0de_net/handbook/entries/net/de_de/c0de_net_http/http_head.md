```json
{
  "title": "HTTP_HEAD",
  "icon": "c0de:call_block",
  "category": "c0de_net_http",
  "ordinal": 2
}
```

Sendet eine HEAD-Anfrage: wie GET, aber die Antwort hat keine Daten.

`result` enthält stattdessen die Header der Antwort als JSON-Objekt, mit kleingeschriebenen Namen in alphabetischer Reihenfolge, z. B. `{"content-length":"12"}`.

**Parameter und Ergebnisse**

- `connection` STRUCT c0de\_net/http/HTTP\_CONNECTION: der Server
- `endpoint` STRING: der Pfad hinter dem Basis-Endpunkt (optional, Standard leer)
- Ergebnis `result` STRING: die Header als JSON
- Ergebnis `status` INTEGER: der HTTP-Status
- Ergebnis `headers` HASH STRING: die Header der Antwort

**Beispiel**

```c0de
CALL c0de_net/http/HTTP_HEAD
  PARAMETER connection api ;
  PARAMETER endpoint "/status" ;
  RESULT status status
```
