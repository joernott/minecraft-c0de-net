```json
{
  "title": "IMAP_RENAME",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 8
}
```

Benennt einen Ordner um.

**Parameter und Ergebnisse**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: der Server
- `folder` STRING: der Name des Ordners
- `new_name` STRING: sein neuer Name

**Beispiel**

```c0de
CALL c0de_net/mail/IMAP_RENAME
  PARAMETER server imap ;
  PARAMETER folder "Orders" ;
  PARAMETER new_name "Old orders" ;
```
