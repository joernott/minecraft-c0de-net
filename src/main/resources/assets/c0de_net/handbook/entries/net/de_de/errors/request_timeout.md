```json
{
  "title": "REQUEST_TIMEOUT",
  "icon": "c0de:type_error_block",
  "category": "errors",
  "ordinal": 6
}
```

Code 1004, `c0de_net/http/REQUEST_TIMEOUT`: Innerhalb des Zeitlimits der Verbindung kam keine vollständige Antwort.

Das Zeitlimit gilt für die ganze Anfrage, bis zum letzten Byte der Antwort. Erhöhe `timeout` für langsame Server, bis zu 300 Sekunden.
