```json
{
  "title": "IMAP_CREATE",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 6
}
```

Creates a folder.

A folder that already exists raises MAIL\_REJECTED.

**Parameters and results**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: the server
- `folder` STRING: the name of the new folder

**Example**

```c0de
CALL c0de_net/mail/IMAP_CREATE
  PARAMETER server imap ;
  PARAMETER folder "Orders" ;
```
