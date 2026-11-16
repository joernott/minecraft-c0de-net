```json
{
  "title": "Mail von der Farm",
  "icon": "c0de:program_block",
  "category": "examples",
  "ordinal": 3
}
```

Sendet eine Nachricht, wenn die Farm voll ist.

Stell den Auslöserblock neben einen Komparator an der Truhe der Farm und gib ihm ein Konstanten-Item `password` mit dem Passwort deines Mail-Kontos. Die Nachricht wird vom Computer des Spielers gesendet, dem der Auslöserblock gehört.

```c0de
PROGRAM farm_mail
NOOP
INCLUDE c0de_net/mail
NOOP
FUNCTION full
  PARAMETER password STRING
BEGIN
  VARIABLE smtp STRUCT c0de_net/mail/SMTP_SERVER
  ASSIGN smtp.server "smtp.example.com" ;
  ASSIGN smtp.user "steve@example.com" ;
  ASSIGN smtp.password password ;
  ASSIGN smtp.from "Farm <steve@example.com>" ;
  CALL c0de_net/mail/SMTP_SEND
    PARAMETER server smtp ;
    PARAMETER to "steve@example.com" ;
    PARAMETER subject "The farm is full" ;
    PARAMETER text "Come and empty the chest." ;
END FUNCTION
```
