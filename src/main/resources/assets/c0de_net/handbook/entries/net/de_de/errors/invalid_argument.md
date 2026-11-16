```json
{
  "title": "INVALID_ARGUMENT",
  "icon": "c0de:type_error_block",
  "category": "errors",
  "ordinal": 0
}
```

Code 309: Die Verbindung oder der Endpunkt ist ungültig.

Der Server ist leer, der Port liegt nicht zwischen 0 und 65535, das Zeitlimit liegt nicht zwischen 0 und 300, der Proxy ist keine http://-URL, oder ein Header kann nicht gesendet werden. Der Endpunkt macht die URL ungültig, z. B. mit einem Leerzeichen (siehe HTTP\_CONNECTION). Der Fehler wird auch ausgelöst, wenn die Anfrage größer als 1.000.000 Bytes ist.

Für c0de\_net/mail: eine Verbindung ohne Server, ein SMTP\_SERVER ohne `from`, ein IMAP\_SERVER ohne `user`, ein unbekanntes `security`, ein Zeitlimit über dem größten, eine ungültige Adresse, unbekannte Kriterien von IMAP\_SEARCH, ein unbekannter `mode` von IMAP\_STORE oder eine Funktion für Nachrichten ohne ausgewählten Ordner.
