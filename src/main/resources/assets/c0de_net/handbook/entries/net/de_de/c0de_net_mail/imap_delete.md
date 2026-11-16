```json
{
  "title": "IMAP_DELETE",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 7
}
```

Löscht einen Ordner mit seinen Nachrichten.

**Parameter und Ergebnisse**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: der Server
- `folder` STRING: der Name des Ordners

**Beispiel**

```c0de
CALL c0de_net/mail/IMAP_DELETE
  PARAMETER server imap ;
  PARAMETER folder "Old orders" ;
```
