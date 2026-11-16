```json
{
  "title": "IMAP_SEARCH",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 11
}
```

Returns the UIDs of the messages of the selected folder that match the criteria, oldest first.

The criteria are words of IMAP, separated by spaces, and all of them must match: `ALL`, `SEEN`, `UNSEEN`, `FLAGGED`, `UNFLAGGED`, `ANSWERED`, `UNANSWERED`, `DELETED`, `UNDELETED`, `DRAFT`, and with a value behind them `FROM`, `TO`, `CC`, `SUBJECT` and `BODY` with a text, `SINCE`, `BEFORE` and `ON` with a date like 1-Nov-2026. A text with spaces is put in double quotes, like `SUBJECT \"wheat order\"` in the text notation. With more matches than the server allows (100), the newest are returned.

**Parameters and results**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: the server with the selected folder
- `criteria` STRING: the criteria (optional, default `ALL`)
- result `result` ARRAY INTEGER: the UIDs

**Example**

```c0de
CALL c0de_net/mail/IMAP_SEARCH
  PARAMETER server imap ;
  PARAMETER criteria "UNSEEN FROM alex" ;
  RESULT result uids
```
