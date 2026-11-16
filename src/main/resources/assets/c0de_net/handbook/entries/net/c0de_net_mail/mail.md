```json
{
  "title": "MAIL",
  "icon": "c0de:type_struct_block",
  "category": "c0de_net_mail",
  "ordinal": 2
}
```

A struct type with a message that IMAP\_FETCH read.

- `uid` INTEGER: the number of the message in its folder
- `from` STRING: the sender
- `to` ARRAY STRING: the recipients
- `cc` ARRAY STRING: the recipients of copies
- `subject` STRING: the subject
- `date` TIME: when the message was sent
- `text` STRING: the plain text of the message, empty for a message without one or if IMAP\_FETCH didn't read it
- `flags` ARRAY STRING: the flags, like `\Seen`
- `size` INTEGER: the size in bytes
