```json
{
  "title": "IMAP_SERVER",
  "icon": "c0de:type_struct_block",
  "category": "c0de_net_mail",
  "ordinal": 1
}
```

Ein Strukturtyp mit der Verbindung zu einem Server mit Postfächern.

Deklariere ihn mit `VARIABLE imap STRUCT c0de_net/mail/IMAP_SERVER`. `server` und `user` sind nötig.

- `server` STRING: der Servername, z. B. imap.example.com
- `port` INTEGER: 1 bis 65535, oder 0 für 993 mit `tls` und sonst 143 (Standard 0)
- `security` STRING: `tls` (der Standard für einen leeren Text), `starttls` oder `none`
- `user` STRING: der Benutzername
- `password` STRING: das Passwort, vertraulich wie das von SMTP\_SERVER
- `timeout` INTEGER: wie bei SMTP\_SERVER
- `skip_ssl_validation` BOOLEAN: wie bei SMTP\_SERVER
- `folder` STRING: der ausgewählte Ordner\; IMAP\_SELECT setzt ihn

**Beispiel**

```c0de
VARIABLE imap STRUCT c0de_net/mail/IMAP_SERVER
ASSIGN imap.server "imap.example.com" ;
ASSIGN imap.user "steve@example.com" ;
ASSIGN imap.password password ;
```
