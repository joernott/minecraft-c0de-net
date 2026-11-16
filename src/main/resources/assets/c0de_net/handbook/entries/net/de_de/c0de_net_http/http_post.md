```json
{
  "title": "HTTP_POST",
  "icon": "c0de:call_block",
  "category": "c0de_net_http",
  "ordinal": 7
}
```

Sendet eine POST-Anfrage: sendet Daten an den Server, z. B. um etwas anzulegen.

Die Daten werden als UTF-8 gesendet. Setze ihren Typ im Header `Content-Type` der Verbindung. Erzeuge die Daten mit TO\_JSON aus c0de/strings.

**Parameter und Ergebnisse**

- `connection` STRUCT c0de\_net/http/HTTP\_CONNECTION: der Server
- `endpoint` STRING: der Pfad hinter dem Basis-Endpunkt (optional, Standard leer)
- `data` STRING: die Daten, die mit der Anfrage gesendet werden (optional, Standard leer)
- Ergebnis `result` STRING: die Rohdaten der Antwort
- Ergebnis `status` INTEGER: der HTTP-Status
- Ergebnis `headers` HASH STRING: die Header der Antwort

**Beispiel**

```c0de
CALL c0de_net/http/HTTP_POST
  PARAMETER connection api ;
  PARAMETER endpoint "/messages" ;
  PARAMETER data message ;
  RESULT result answer
```
