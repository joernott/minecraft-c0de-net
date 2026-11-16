```json
{
  "title": "IMAP_STORE",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 13
}
```

Changes the flags of messages of the selected folder.

The flags of IMAP are `\Seen`, `\Answered`, `\Flagged`, `\Deleted` and `\Draft`\; other words are keywords, if the server supports them. The mode `add` adds the flags, `remove` removes them and `set` replaces all flags. In the text notation, a backslash is written twice: `"\\Seen"`.

**Parameters and results**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: the server with the selected folder
- `uids` INTEGER or ARRAY INTEGER: the UIDs of the messages
- `flags` STRING or ARRAY STRING: the flags
- `mode` STRING: `add`, `remove` or `set` (optional, default `add`)

**Example**

```c0de
CALL c0de_net/mail/IMAP_STORE
  PARAMETER server imap ;
  PARAMETER uids uids ;
  PARAMETER flags "\\Seen" ;
```
