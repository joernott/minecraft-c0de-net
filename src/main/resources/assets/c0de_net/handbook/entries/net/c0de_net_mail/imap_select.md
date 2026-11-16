```json
{
  "title": "IMAP_SELECT",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 10
}
```

Selects a folder: checks that it exists and stores its name in `folder` of the variable.

Pass the variable itself, not a calculation, because the function changes it. The functions for messages, IMAP\_SEARCH, IMAP\_FETCH, IMAP\_STORE, IMAP\_MOVE and IMAP\_EXPUNGE, work in the selected folder.

**Parameters and results**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: a variable with the server
- `folder` STRING: the folder (optional, default `INBOX`)
- result `result` STRUCT c0de\_net/mail/MAILBOX: its status

**Example**

```c0de
CALL c0de_net/mail/IMAP_SELECT
  PARAMETER server imap ;
  PARAMETER folder "INBOX" ;
  RESULT result box
```
