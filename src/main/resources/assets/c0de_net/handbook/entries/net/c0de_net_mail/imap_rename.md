```json
{
  "title": "IMAP_RENAME",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 8
}
```

Renames a folder.

**Parameters and results**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: the server
- `folder` STRING: the name of the folder
- `new_name` STRING: its new name

**Example**

```c0de
CALL c0de_net/mail/IMAP_RENAME
  PARAMETER server imap ;
  PARAMETER folder "Orders" ;
  PARAMETER new_name "Old orders" ;
```
