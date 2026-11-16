```json
{
  "title": "IMAP_STATUS",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 9
}
```

Returns the status of a folder without selecting it.

**Parameters and results**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: the server
- `folder` STRING: the folder (optional, default empty: the selected folder)
- result `result` STRUCT c0de\_net/mail/MAILBOX: its status

**Example**

```c0de
CALL c0de_net/mail/IMAP_STATUS
  PARAMETER server imap ;
  PARAMETER folder "INBOX" ;
  RESULT result box
```
