```json
{
  "title": "c0de_net/mail",
  "icon": "minecraft:paper",
  "ordinal": 2
}
```

Die Mail-Bibliothek sendet Mails über SMTP und liest und verwaltet Postfächer über IMAP. Mails sind reiner Text, ohne HTML und Anhänge.

Jede Funktion öffnet eine eigene Verbindung, meldet sich an, erledigt ihre Arbeit und meldet sich ab. Zwischen zwei Aufrufen bleibt nichts offen. Die Funktionen für Nachrichten arbeiten in dem Ordner, den IMAP\_SELECT in der IMAP\_SERVER-Variable gespeichert hat\; gib ihnen also diese Variable.
