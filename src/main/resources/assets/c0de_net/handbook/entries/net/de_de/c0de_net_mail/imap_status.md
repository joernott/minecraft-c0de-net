```json
{
  "title": "IMAP_STATUS",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 9
}
```

Gibt den Status eines Ordners zurück, ohne ihn auszuwählen.

**Parameter und Ergebnisse**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: der Server
- `folder` STRING: der Ordner (optional, Standard leer: der ausgewählte Ordner)
- Ergebnis `result` STRUCT c0de\_net/mail/MAILBOX: sein Status

**Beispiel**

```c0de
CALL c0de_net/mail/IMAP_STATUS
  PARAMETER server imap ;
  PARAMETER folder "INBOX" ;
  RESULT result box
```
