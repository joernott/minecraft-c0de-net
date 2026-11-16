```json
{
  "title": "Einstellungen",
  "icon": "minecraft:writable_book",
  "category": "getting_started",
  "ordinal": 2
}
```

Der Server hat die Datei `config/c0de_net.properties`. `maxResponseSize` begrenzt die Größe einer Antwort in Bytes (1 KiB bis 16 MiB, Standard 1 MiB). `maxRequestsPerPlayer` begrenzt, wie viele Anfragen gleichzeitig auf dem Client eines Spielers laufen (1 bis 64, Standard 8)\; weitere Anfragen warten.

Für c0de\_net/mail hat der Server diese Einstellungen in derselben Datei:

- `enableMail`: false schaltet die Mail-Bibliothek ab (Standard true)
- `maxMailSize`: die Bytes einer Nachricht, die SMTP\_SEND sendet (Standard 65536)
- `maxRecipients`: die Empfänger einer Nachricht, to, cc und bcc zusammen (Standard 20)
- `maxMailsPerMinute`: die Nachrichten, die ein Spieler pro Minute senden darf (Standard 10)
- `maxFetchSize`: die Bytes, die ein IMAP\_FETCH lesen darf (Standard 1 MiB)
- `maxFetchMessages`: die Nachrichten eines IMAP\_FETCH oder IMAP\_SEARCH (Standard 100)
- `mailTimeout`: das Standard- und das größte Zeitlimit einer Mail-Verbindung in Sekunden (Standard 30)
- `allowPort25`: true erlaubt Mail-Verbindungen zu Port 25 (Standard false)

Jeder Spieler hat die Datei `config/c0de_net-client.properties`. Mit `allowRequests=false` sendet sein Client keine Anfragen, und die Anfragen seiner Prozesse lösen REQUESTS\_DISABLED aus. Mit `allowMail=false` sendet und liest sein Client keine Mails, und die Mail-Funktionen seiner Prozesse lösen MAIL\_DISABLED aus.

Beide Dateien werden mit den Standardwerten angelegt. Ungültige Einträge werden im Log gemeldet und durch die Standardwerte ersetzt.
