```json
{
  "title": "SMTP_SEND",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 4
}
```

Sendet eine Nachricht als reinen Text und gibt ihre Message-ID zurück.

Alle Empfänger zusammen, to, cc und bcc, dürfen höchstens 20 sein, und die Nachricht höchstens 64 KiB groß. Ein Spieler darf 10 Nachrichten pro Minute senden. Der Server kann diese Grenzen ändern.

**Parameter und Ergebnisse**

- `server` STRUCT c0de\_net/mail/SMTP\_SERVER: der Server
- `to` STRING oder ARRAY STRING: die Empfänger
- `subject` STRING: der Betreff (optional, Standard leer)
- `text` STRING: der Text (optional, Standard leer)
- `cc` ARRAY STRING: Empfänger von Kopien (optional, Standard keine)
- `bcc` ARRAY STRING: Empfänger von Kopien, die die anderen nicht sehen (optional, Standard keine)
- `reply_to` STRING: die Adresse für Antworten (optional, Standard leer: der Absender)
- Ergebnis `result` STRING: die Message-ID

**Beispiel**

```c0de
CALL c0de_net/mail/SMTP_SEND
  PARAMETER server smtp ;
  PARAMETER to "alex@example.com" ;
  PARAMETER subject "The farm" ;
  PARAMETER text "The wheat is ready." ;
```
