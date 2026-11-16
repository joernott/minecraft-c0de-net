```json
{
  "title": "HTTP_GET",
  "icon": "c0de:call_block",
  "category": "c0de_net_http",
  "ordinal": 1
}
```

Sendet eine GET-Anfrage und gibt die Antwort zurück.

GET liest Daten. Weiterleitungen werden verfolgt, außer von https zu http. Die Ergebnisse sind die der letzten Antwort. Ein Status von 400 oder höher löst HTTP\_STATUS aus, und die Ergebnisse werden trotzdem geliefert.

**Parameter und Ergebnisse**

- `connection` STRUCT c0de\_net/http/HTTP\_CONNECTION: der Server
- `endpoint` STRING: der Pfad hinter dem Basis-Endpunkt (optional, Standard leer)
- Ergebnis `result` STRING: die Rohdaten der Antwort
- Ergebnis `status` INTEGER: der HTTP-Status, z. B. 200
- Ergebnis `headers` HASH STRING: die Header der Antwort, mit kleingeschriebenen Namen

**Beispiel**

```c0de
CALL c0de_net/http/HTTP_GET
  PARAMETER connection api ;
  PARAMETER endpoint "/forecast?city=Berlin" ;
  RESULT result answer
```
