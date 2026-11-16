```json
{
  "title": "RESPONSE_TOO_LARGE",
  "icon": "c0de:type_error_block",
  "category": "errors",
  "ordinal": 6
}
```

Code 1005, `c0de_net/http/RESPONSE_TOO_LARGE`: Die Antwort ist größer, als der Server erlaubt.

Die Grenze ist `maxResponseSize` in `config/c0de_net.properties`, standardmäßig 1 MiB. Frag bei der API weniger Daten an, z. B. eine Seite einer Liste.
