```json
{
  "title": "IMAP_EXPUNGE",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 15
}
```

Removes the messages of the selected folder that have the flag `\Deleted`, and returns how many.

**Parameters and results**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: the server with the selected folder
- result `result` INTEGER: the number of removed messages

**Example**

```c0de
CALL c0de_net/mail/IMAP_EXPUNGE
  PARAMETER server imap ;
  RESULT result removed
```
