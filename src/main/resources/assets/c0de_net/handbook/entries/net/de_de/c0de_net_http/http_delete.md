```json
{
  "title": "HTTP_DELETE",
  "icon": "c0de:call_block",
  "category": "c0de_net_http",
  "ordinal": 5
}
```

Sendet eine DELETE-Anfrage: entfernt etwas auf dem Server.

Manche APIs erwarten dabei Daten, die meisten nicht.

**Parameter und Ergebnisse**

- `connection` STRUCT c0de\_net/http/HTTP\_CONNECTION: der Server
- `endpoint` STRING: der Pfad hinter dem Basis-Endpunkt (optional, Standard leer)
- `data` STRING: die Daten, die mit der Anfrage gesendet werden, in UTF-8 (optional, Standard leer)
- Ergebnis `result` STRING: die Rohdaten der Antwort
- Ergebnis `status` INTEGER: der HTTP-Status
- Ergebnis `headers` HASH STRING: die Header der Antwort

**Beispiel**

```c0de
CALL c0de_net/http/HTTP_DELETE
  PARAMETER connection api ;
  PARAMETER endpoint "/timers/3" ;
  RESULT status status
```
