```json
{
  "title": "IMAP_LIST",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 5
}
```

Returns the names of the folders.

In the pattern, `*` stands for any text and `%` for any text within one level of folders.

**Parameters and results**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: the server
- `pattern` STRING: the folders to list (optional, default `*`: all)
- result `result` ARRAY STRING: the names of the folders

**Example**

```c0de
CALL c0de_net/mail/IMAP_LIST
  PARAMETER server imap ;
  RESULT result folders
```
