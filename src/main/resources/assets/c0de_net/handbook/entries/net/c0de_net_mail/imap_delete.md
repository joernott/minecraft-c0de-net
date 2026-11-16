```json
{
  "title": "IMAP_DELETE",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 7
}
```

Deletes a folder with its messages.

**Parameters and results**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: the server
- `folder` STRING: the name of the folder

**Example**

```c0de
CALL c0de_net/mail/IMAP_DELETE
  PARAMETER server imap ;
  PARAMETER folder "Old orders" ;
```
