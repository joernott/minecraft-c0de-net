```json
{
  "title": "Fehler",
  "icon": "c0de:type_error_block",
  "ordinal": 3
}
```

Die Funktionen lösen diese Fehler aus. Fang sie mit einer Variable vom Typ ERROR in der CALL-Zeile ab und vergleiche ihren `code` mit den Konstanten der Bibliothek, z. B. `c0de_net/http/HTTP_STATUS`. Sie enthalten die Codes und kommen wie die Funktionen mit dem INCLUDE. c0de\_net/mail hat die Konstanten der Fehler, die es auslöst, z. B. `c0de_net/mail/REQUEST_FAILED`. INVALID\_ARGUMENT und PERMISSION\_DENIED sind Konstanten von c0de.
