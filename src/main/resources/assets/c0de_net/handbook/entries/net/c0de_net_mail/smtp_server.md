```json
{
  "title": "SMTP_SERVER",
  "icon": "c0de:type_struct_block",
  "category": "c0de_net_mail",
  "ordinal": 0
}
```

A struct type with the connection to a server that sends mail, like the one of your mail provider.

Declare it with `VARIABLE smtp STRUCT c0de_net/mail/SMTP_SERVER` and set its elements with ASSIGN. `server` and `from` are needed, the other elements start with defaults that fit most providers.

- `server` STRING: the server name, like smtp.example.com
- `port` INTEGER: 1 to 65535, or 0 for 465 with `tls` and 587 otherwise (default 0)
- `security` STRING: `starttls` (the default for an empty text), `tls` for a connection that is encrypted from the start, or `none`
- `user` STRING: the user name, empty to send without logging in
- `password` STRING: the password
- `from` STRING: the address of the sender, like `Base computer <steve@example.com>`
- `timeout` INTEGER: seconds a call may take, or 0 for the default of the server (30 seconds, which is also the largest value)
- `skip_ssl_validation` BOOLEAN: true to accept any certificate, e.g. of a server in your home network (default false)

Port 25 is blocked unless the server allows it. Use the submission port of your mail provider, 465 or 587.

The element `password` has the tag `security: "sensitive"`, so the debugging tool shows `***` instead of it. Like for HTTP\_CONNECTION, don't write it into the program: pass it in as a parameter, or read it from the vault of c0de.

**Example**

```c0de
PROGRAM postman
NOOP
INCLUDE c0de_net/mail
NOOP
FUNCTION run
  PARAMETER password STRING
BEGIN
  VARIABLE smtp STRUCT c0de_net/mail/SMTP_SERVER
  ASSIGN smtp.server "smtp.example.com" ;
  ASSIGN smtp.user "steve@example.com" ;
  ASSIGN smtp.password password ;
  ASSIGN smtp.from "Base computer <steve@example.com>" ;
END FUNCTION
```
