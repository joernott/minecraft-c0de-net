```json
{
  "title": "Settings",
  "icon": "minecraft:writable_book",
  "category": "getting_started",
  "ordinal": 2
}
```

The server has the file `config/c0de_net.properties`. `maxResponseSize` limits the size of an answer in bytes (1 KiB to 16 MiB, default 1 MiB). `maxRequestsPerPlayer` limits how many requests run at the same time on the client of one player (1 to 64, default 8)\; more requests wait.

For c0de\_net/mail, the server has these settings in the same file:

- `enableMail`: false switches the mail library off (default true)
- `maxMailSize`: the bytes of a message SMTP\_SEND sends (default 65536)
- `maxRecipients`: the recipients of one message, to, cc and bcc together (default 20)
- `maxMailsPerMinute`: the messages one player may send in a minute (default 10)
- `maxFetchSize`: the bytes one IMAP\_FETCH may read (default 1 MiB)
- `maxFetchMessages`: the messages of one IMAP\_FETCH or IMAP\_SEARCH (default 100)
- `mailTimeout`: the default and the largest timeout of a mail connection in seconds (default 30)
- `allowPort25`: true allows mail connections to port 25 (default false)

Every player has the file `config/c0de_net-client.properties`. With `allowRequests=false`, their client sends no requests, and the requests of their processes raise REQUESTS\_DISABLED. With `allowMail=false`, their client sends and reads no mail, and the mail functions of their processes raise MAIL\_DISABLED.

Both files are created with the default values. Invalid entries are reported in the log and replaced by the defaults.
