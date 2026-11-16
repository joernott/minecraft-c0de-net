```json
{
  "title": "IMAP_FETCH",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 12
}
```

Liest Nachrichten des ausgewählten Ordners.

Eine UID, die es nicht gibt, wird ausgelassen. Das Lesen des Texts markiert eine Nachricht als gelesen, wie ein Mailprogramm es tut\; mit `text` FALSE werden nur die anderen Elemente gelesen, und die Nachricht bleibt ungelesen. Ein Aufruf liest höchstens 100 Nachrichten und 1 MiB, sonst löst er RESPONSE\_TOO\_LARGE aus.

**Parameter und Ergebnisse**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: der Server mit dem ausgewählten Ordner
- `uids` INTEGER oder ARRAY INTEGER: die UIDs der Nachrichten
- `text` BOOLEAN: ob der Text gelesen wird (optional, Standard TRUE)
- Ergebnis `result` ARRAY STRUCT c0de\_net/mail/MAIL: die Nachrichten

**Beispiel**

```c0de
CALL c0de_net/mail/IMAP_FETCH
  PARAMETER server imap ;
  PARAMETER uids uids ;
  RESULT result letters
```
