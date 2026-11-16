```json
{
  "title": "IMAP_CREATE",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 6
}
```

Legt einen Ordner an.

Ein Ordner, den es schon gibt, löst MAIL\_REJECTED aus.

**Parameter und Ergebnisse**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: der Server
- `folder` STRING: der Name des neuen Ordners

**Beispiel**

```c0de
CALL c0de_net/mail/IMAP_CREATE
  PARAMETER server imap ;
  PARAMETER folder "Orders" ;
```
