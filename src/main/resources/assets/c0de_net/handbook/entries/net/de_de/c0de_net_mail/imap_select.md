```json
{
  "title": "IMAP_SELECT",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 10
}
```

Wählt einen Ordner aus: prüft, dass es ihn gibt, und speichert seinen Namen in `folder` der Variable.

Übergib die Variable selbst, keine Berechnung, denn die Funktion ändert sie. Die Funktionen für Nachrichten, IMAP\_SEARCH, IMAP\_FETCH, IMAP\_STORE, IMAP\_MOVE und IMAP\_EXPUNGE, arbeiten im ausgewählten Ordner.

**Parameter und Ergebnisse**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: eine Variable mit dem Server
- `folder` STRING: der Ordner (optional, Standard `INBOX`)
- Ergebnis `result` STRUCT c0de\_net/mail/MAILBOX: sein Status

**Beispiel**

```c0de
CALL c0de_net/mail/IMAP_SELECT
  PARAMETER server imap ;
  PARAMETER folder "INBOX" ;
  RESULT result box
```
