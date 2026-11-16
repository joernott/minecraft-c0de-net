```json
{
  "title": "IMAP_MOVE",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 14
}
```

Moves messages of the selected folder into another folder.

**Parameters and results**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: the server with the selected folder
- `uids` INTEGER or ARRAY INTEGER: the UIDs of the messages
- `folder` STRING: the folder they move to

**Example**

```c0de
CALL c0de_net/mail/IMAP_MOVE
  PARAMETER server imap ;
  PARAMETER uids uids ;
  PARAMETER folder "Orders" ;
```
