```json
{
  "title": "HTTP_PUT",
  "icon": "c0de:call_block",
  "category": "c0de_net_http",
  "ordinal": 8
}
```

Sendet eine PUT-Anfrage: speichert etwas auf dem Server und ersetzt, was vorher da war.

Die Daten werden als UTF-8 gesendet. Setze ihren Typ im Header `Content-Type` der Verbindung.

**Parameter und Ergebnisse**

- `connection` STRUCT c0de\_net/http/HTTP\_CONNECTION: der Server
- `endpoint` STRING: der Pfad hinter dem Basis-Endpunkt (optional, Standard leer)
- `data` STRING: die Daten, die mit der Anfrage gesendet werden (optional, Standard leer)
- Ergebnis `result` STRING: die Rohdaten der Antwort
- Ergebnis `status` INTEGER: der HTTP-Status
- Ergebnis `headers` HASH STRING: die Header der Antwort

**Beispiel**

```c0de
CALL c0de_net/http/HTTP_PUT
  PARAMETER connection api ;
  PARAMETER endpoint "/lamps/kitchen" ;
  PARAMETER data state ;
  RESULT status status
```
