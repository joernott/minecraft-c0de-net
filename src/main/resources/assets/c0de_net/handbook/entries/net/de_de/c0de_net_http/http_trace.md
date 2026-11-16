```json
{
  "title": "HTTP_TRACE",
  "icon": "c0de:call_block",
  "category": "c0de_net_http",
  "ordinal": 4
}
```

Sendet eine TRACE-Anfrage: Der Server schickt die Anfrage so zurück, wie er sie empfangen hat.

Damit siehst du, was ein Proxy ändert. Viele Server erlauben TRACE nicht und antworten mit 405.

**Parameter und Ergebnisse**

- `connection` STRUCT c0de\_net/http/HTTP\_CONNECTION: der Server
- `endpoint` STRING: der Pfad hinter dem Basis-Endpunkt (optional, Standard leer)
- Ergebnis `result` STRING: die Rohdaten der Antwort
- Ergebnis `status` INTEGER: der HTTP-Status
- Ergebnis `headers` HASH STRING: die Header der Antwort

**Beispiel**

```c0de
CALL c0de_net/http/HTTP_TRACE
  PARAMETER connection api ;
  RESULT result echo
```
