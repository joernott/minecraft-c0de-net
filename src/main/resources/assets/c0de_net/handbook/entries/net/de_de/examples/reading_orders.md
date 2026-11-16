```json
{
  "title": "Bestellungen lesen",
  "icon": "c0de:program_block",
  "category": "examples",
  "ordinal": 4
}
```

Liest die ungelesenen Nachrichten mit "order" im Betreff, gibt ihre Betreffs aus und verschiebt sie in den Ordner Orders.

```c0de
PROGRAM orders
NOOP
INCLUDE c0de_net/mail
NOOP
FUNCTION read
  PARAMETER password STRING
  RESULT count INTEGER
BEGIN
  VARIABLE imap STRUCT c0de_net/mail/IMAP_SERVER
  VARIABLE box STRUCT c0de_net/mail/MAILBOX
  VARIABLE uids ARRAY INTEGER
  VARIABLE letters ARRAY STRUCT c0de_net/mail/MAIL
  ASSIGN imap.server "imap.example.com" ;
  ASSIGN imap.user "steve@example.com" ;
  ASSIGN imap.password password ;
  CALL c0de_net/mail/IMAP_SELECT
    PARAMETER server imap ;
    RESULT result box
  CALL c0de_net/mail/IMAP_SEARCH
    PARAMETER server imap ;
    PARAMETER criteria "UNSEEN SUBJECT order" ;
    RESULT result uids
  CALL c0de_net/mail/IMAP_FETCH
    PARAMETER server imap ;
    PARAMETER uids uids ;
    RESULT result letters
  EACH letter letters
  BEGIN
    CALL PRINT
      PARAMETER value letter.from ⊕ ": " ⊕ letter.subject ;
  END EACH
  CALL c0de_net/mail/IMAP_MOVE
    PARAMETER server imap ;
    PARAMETER uids uids ;
    PARAMETER folder "Orders" ;
  CALL LEN
    PARAMETER value uids ;
    RESULT result count
END FUNCTION
```
