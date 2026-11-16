```json
{
  "title": "MAILBOX",
  "icon": "c0de:type_struct_block",
  "category": "c0de_net_mail",
  "ordinal": 3
}
```

Ein Strukturtyp mit dem Status eines Ordners, von IMAP\_STATUS und IMAP\_SELECT.

- `name` STRING: der Name des Ordners
- `messages` INTEGER: die Zahl der Nachrichten
- `recent` INTEGER: die Zahl der neuen Nachrichten seit dem letzten Blick
- `unseen` INTEGER: die Zahl der Nachrichten ohne das Flag `\Seen`
- `uid_next` INTEGER: die UID, die die nächste Nachricht bekommt
- `uid_validity` INTEGER: ändert sich, wenn die UIDs des Ordners neu vergeben werden
