```json
{
  "title": "HTTP_OPTIONS",
  "icon": "c0de:call_block",
  "category": "c0de_net_http",
  "ordinal": 3
}
```

Sendet eine OPTIONS-Anfrage: fragt den Server, welche Methoden ein Endpunkt unterstützt.

Viele Server antworten im Header `allow`.

**Parameter und Ergebnisse**

- `connection` STRUCT c0de\_net/http/HTTP\_CONNECTION: der Server
- `endpoint` STRING: der Pfad hinter dem Basis-Endpunkt (optional, Standard leer)
- Ergebnis `result` STRING: die Rohdaten der Antwort
- Ergebnis `status` INTEGER: der HTTP-Status
- Ergebnis `headers` HASH STRING: die Header der Antwort

**Beispiel**

```c0de
CALL c0de_net/http/HTTP_OPTIONS
  PARAMETER connection api ;
  PARAMETER endpoint "/lamps" ;
  RESULT headers answerHeaders
```
