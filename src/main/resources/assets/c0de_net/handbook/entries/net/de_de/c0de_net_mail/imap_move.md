```json
{
  "title": "IMAP_MOVE",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 14
}
```

Verschiebt Nachrichten des ausgewählten Ordners in einen anderen Ordner.

**Parameter und Ergebnisse**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: der Server mit dem ausgewählten Ordner
- `uids` INTEGER oder ARRAY INTEGER: die UIDs der Nachrichten
- `folder` STRING: der Ordner, in den sie kommen

**Beispiel**

```c0de
CALL c0de_net/mail/IMAP_MOVE
  PARAMETER server imap ;
  PARAMETER uids uids ;
  PARAMETER folder "Orders" ;
```
