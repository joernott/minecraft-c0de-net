```json
{
  "title": "IMAP_EXPUNGE",
  "icon": "minecraft:paper",
  "category": "c0de_net_mail",
  "ordinal": 15
}
```

Entfernt die Nachrichten des ausgewählten Ordners, die das Flag `\Deleted` haben, und gibt zurück, wie viele.

**Parameter und Ergebnisse**

- `server` STRUCT c0de\_net/mail/IMAP\_SERVER: der Server mit dem ausgewählten Ordner
- Ergebnis `result` INTEGER: die Zahl der entfernten Nachrichten

**Beispiel**

```c0de
CALL c0de_net/mail/IMAP_EXPUNGE
  PARAMETER server imap ;
  RESULT result removed
```
