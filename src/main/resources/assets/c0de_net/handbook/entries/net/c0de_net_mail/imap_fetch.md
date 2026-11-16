```json
{
  "title": "IMAP_FETCH",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 12
}
```

Reads messages of the selected folder.

A UID that doesn't exist is left out. Reading the text marks a message as seen, like a mail program does\; with `text` FALSE, only the other elements are read and the message stays unseen. One call reads at most 100 messages and 1 MiB, or raises RESPONSE\_TOO\_LARGE.

**Parameters and results**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: the server with the selected folder
- `uids` INTEGER or ARRAY INTEGER: the UIDs of the messages
- `text` BOOLEAN: whether the text is read (optional, default TRUE)
- result `result` ARRAY STRUCT c0de\_net/mail/MAIL: the messages

**Example**

```c0de
CALL c0de_net/mail/IMAP_FETCH
  PARAMETER server imap ;
  PARAMETER uids uids ;
  RESULT result letters
```
