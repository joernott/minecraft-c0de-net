```json
{
  "title": "IMAP_SERVER",
  "icon": "c0de:type_struct_block",
  "category": "c0de_net_mail",
  "ordinal": 1
}
```

A struct type with the connection to a server with mailboxes.

Declare it with `VARIABLE imap STRUCT c0de_net/mail/IMAP_SERVER`. `server` and `user` are needed.

- `server` STRING: the server name, like imap.example.com
- `port` INTEGER: 1 to 65535, or 0 for 993 with `tls` and 143 otherwise (default 0)
- `security` STRING: `tls` (the default for an empty text), `starttls` or `none`
- `user` STRING: the user name
- `password` STRING: the password, sensitive like the one of SMTP\_SERVER
- `timeout` INTEGER: as for SMTP\_SERVER
- `skip_ssl_validation` BOOLEAN: as for SMTP\_SERVER
- `folder` STRING: the selected folder\; IMAP\_SELECT sets it

**Example**

```c0de
VARIABLE imap STRUCT c0de_net/mail/IMAP_SERVER
ASSIGN imap.server "imap.example.com" ;
ASSIGN imap.user "steve@example.com" ;
ASSIGN imap.password password ;
```
