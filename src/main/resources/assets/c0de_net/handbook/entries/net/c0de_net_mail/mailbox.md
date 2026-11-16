```json
{
  "title": "MAILBOX",
  "icon": "c0de:type_struct_block",
  "category": "c0de_net_mail",
  "ordinal": 3
}
```

A struct type with the status of a folder, from IMAP\_STATUS and IMAP\_SELECT.

- `name` STRING: the name of the folder
- `messages` INTEGER: the number of messages
- `recent` INTEGER: the number of new messages since the last look
- `unseen` INTEGER: the number of messages without the flag `\Seen`
- `uid_next` INTEGER: the UID the next message will get
- `uid_validity` INTEGER: changes when the UIDs of the folder are given anew
