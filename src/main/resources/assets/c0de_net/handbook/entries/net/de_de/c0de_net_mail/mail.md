```json
{
  "title": "MAIL",
  "icon": "c0de:type_struct_block",
  "category": "c0de_net_mail",
  "ordinal": 2
}
```

Ein Strukturtyp mit einer Nachricht, die IMAP\_FETCH gelesen hat.

- `uid` INTEGER: die Nummer der Nachricht in ihrem Ordner
- `from` STRING: der Absender
- `to` ARRAY STRING: die Empfänger
- `cc` ARRAY STRING: die Empfänger von Kopien
- `subject` STRING: der Betreff
- `date` TIME: wann die Nachricht gesendet wurde
- `text` STRING: der reine Text der Nachricht, leer bei einer Nachricht ohne ihn oder wenn IMAP\_FETCH ihn nicht gelesen hat
- `flags` ARRAY STRING: die Flags, z. B. `\Seen`
- `size` INTEGER: die Größe in Bytes
