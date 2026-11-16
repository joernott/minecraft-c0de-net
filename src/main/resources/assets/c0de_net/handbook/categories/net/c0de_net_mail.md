```json
{
  "title": "c0de_net/mail",
  "icon": "minecraft:paper",
  "ordinal": 2
}
```

The mail library sends mail over SMTP and reads and manages mailboxes over IMAP. Mail is plain text, without HTML and attachments.

Every function opens a connection of its own, logs in, does its work and logs out. Nothing stays open between two calls. The functions for messages work in the folder that IMAP\_SELECT stored in the IMAP\_SERVER variable, so pass that variable to them.
