```json
{
  "title": "SMTP_SERVER",
  "icon": "c0de:type_struct_block",
  "category": "c0de_net_mail",
  "ordinal": 0
}
```

Ein Strukturtyp mit der Verbindung zu einem Server, der Mails sendet, z. B. dem deines Mail-Anbieters.

Deklariere ihn mit `VARIABLE smtp STRUCT c0de_net/mail/SMTP_SERVER` und setze seine Elemente mit ASSIGN. `server` und `from` sind nötig, die anderen Elemente beginnen mit Standardwerten, die zu den meisten Anbietern passen.

- `server` STRING: der Servername, z. B. smtp.example.com
- `port` INTEGER: 1 bis 65535, oder 0 für 465 mit `tls` und sonst 587 (Standard 0)
- `security` STRING: `starttls` (der Standard für einen leeren Text), `tls` für eine Verbindung, die von Anfang an verschlüsselt ist, oder `none`
- `user` STRING: der Benutzername, leer, um ohne Anmeldung zu senden
- `password` STRING: das Passwort
- `from` STRING: die Adresse des Absenders, z. B. `Base computer <steve@example.com>`
- `timeout` INTEGER: Sekunden, die ein Aufruf dauern darf, oder 0 für den Standard des Servers (30 Sekunden, das ist auch der größte Wert)
- `skip_ssl_validation` BOOLEAN: true, um jedes Zertifikat anzunehmen, z. B. eines Servers in deinem Heimnetz (Standard false)

Port 25 ist gesperrt, außer der Server erlaubt ihn. Verwende den Einlieferungsport deines Mail-Anbieters, 465 oder 587.

Das Element `password` hat das Tag `security: "sensitive"`, deshalb zeigt das Debugging-Werkzeug `***` statt seines Werts. Schreib es wie bei HTTP\_CONNECTION nicht ins Programm: Übergib es als Parameter oder lies es aus dem Tresor von c0de.

**Beispiel**

```c0de
PROGRAM postman
NOOP
INCLUDE c0de_net/mail
NOOP
FUNCTION run
  PARAMETER password STRING
BEGIN
  VARIABLE smtp STRUCT c0de_net/mail/SMTP_SERVER
  ASSIGN smtp.server "smtp.example.com" ;
  ASSIGN smtp.user "steve@example.com" ;
  ASSIGN smtp.password password ;
  ASSIGN smtp.from "Base computer <steve@example.com>" ;
END FUNCTION
```
