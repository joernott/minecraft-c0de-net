```json
{
  "title": "IMAP_SEARCH",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 11
}
```

Gibt die UIDs der Nachrichten des ausgewählten Ordners zurück, die zu den Kriterien passen, die älteste zuerst.

Die Kriterien sind Wörter von IMAP, getrennt durch Leerzeichen, und alle müssen passen: `ALL`, `SEEN`, `UNSEEN`, `FLAGGED`, `UNFLAGGED`, `ANSWERED`, `UNANSWERED`, `DELETED`, `UNDELETED`, `DRAFT`, und mit einem Wert dahinter `FROM`, `TO`, `CC`, `SUBJECT` und `BODY` mit einem Text, `SINCE`, `BEFORE` und `ON` mit einem Datum wie 1-Nov-2026. Ein Text mit Leerzeichen kommt in doppelte Anführungszeichen, z. B. `SUBJECT \"wheat order\"` in der Textnotation. Bei mehr Treffern, als der Server erlaubt (100), kommen die neuesten zurück.

**Parameter und Ergebnisse**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: der Server mit dem ausgewählten Ordner
- `criteria` STRING: die Kriterien (optional, Standard `ALL`)
- Ergebnis `result` ARRAY INTEGER: die UIDs

**Beispiel**

```c0de
CALL c0de_net/mail/IMAP_SEARCH
  PARAMETER server imap ;
  PARAMETER criteria "UNSEEN FROM alex" ;
  RESULT result uids
```
