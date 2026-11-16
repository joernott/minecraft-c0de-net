```json
{
  "title": "SMTP_SEND",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 4
}
```

Sends a plain text message and returns its Message-ID.

All recipients together, to, cc and bcc, may be at most 20, and the message at most 64 KiB. One player may send 10 messages in a minute. The server can change these limits.

**Parameters and results**

- `server` STRUCT c0de\_net/mail/SMTP\_SERVER: the server
- `to` STRING or ARRAY STRING: the recipients
- `subject` STRING: the subject (optional, default empty)
- `text` STRING: the text (optional, default empty)
- `cc` ARRAY STRING: recipients of copies (optional, default none)
- `bcc` ARRAY STRING: recipients of copies that the others don't see (optional, default none)
- `reply_to` STRING: the address for answers (optional, default empty: the sender)
- result `result` STRING: the Message-ID

**Example**

```c0de
CALL c0de_net/mail/SMTP_SEND
  PARAMETER server smtp ;
  PARAMETER to "alex@example.com" ;
  PARAMETER subject "The farm" ;
  PARAMETER text "The wheat is ready." ;
```
