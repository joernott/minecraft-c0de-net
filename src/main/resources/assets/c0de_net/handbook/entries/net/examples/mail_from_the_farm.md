```json
{
  "title": "Mail from the farm",
  "icon": "c0de:program_block",
  "category": "examples",
  "ordinal": 3
}
```

Sends a message when the farm is full.

Put the trigger block next to a comparator of the chest of the farm, and give it a constant item `password` with the password of your mail account. The message is sent from the computer of the player who owns the trigger block.

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
