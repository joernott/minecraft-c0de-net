```json
{
  "title": "IMAP_STORE",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 13
}
```

Ändert die Flags von Nachrichten des ausgewählten Ordners.

Die Flags von IMAP sind `\Seen`, `\Answered`, `\Flagged`, `\Deleted` und `\Draft`\; andere Wörter sind Schlüsselwörter, wenn der Server sie unterstützt. Der Modus `add` fügt die Flags hinzu, `remove` entfernt sie und `set` ersetzt alle Flags. In der Textnotation wird ein Backslash doppelt geschrieben: `"\\Seen"`.

**Parameter und Ergebnisse**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: der Server mit dem ausgewählten Ordner
- `uids` INTEGER oder ARRAY INTEGER: die UIDs der Nachrichten
- `flags` STRING oder ARRAY STRING: die Flags
- `mode` STRING: `add`, `remove` oder `set` (optional, Standard `add`)

**Beispiel**

```c0de
CALL c0de_net/mail/IMAP_STORE
  PARAMETER server imap ;
  PARAMETER uids uids ;
  PARAMETER flags "\\Seen" ;
```
