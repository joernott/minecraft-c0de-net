```json
{
  "title": "IMAP_LIST",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 5
}
```

Gibt die Namen der Ordner zurück.

Im Muster steht `*` für beliebigen Text und `%` für beliebigen Text innerhalb einer Ordnerebene.

**Parameter und Ergebnisse**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: der Server
- `pattern` STRING: die Ordner, die aufgelistet werden (optional, Standard `*`: alle)
- Ergebnis `result` ARRAY STRING: die Namen der Ordner

**Beispiel**

```c0de
CALL c0de_net/mail/IMAP_LIST
  PARAMETER server imap ;
  RESULT result folders
```
