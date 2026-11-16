```json
{
  "title": "HTTP_CONNECTION",
  "icon": "c0de:type_struct_block",
  "category": "c0de_net_http",
  "ordinal": 0
}
```

Ein Strukturtyp mit den Grundangaben der Anfragen an einen Server.

Deklariere ihn mit `VARIABLE api STRUCT c0de_net/http/HTTP_CONNECTION` und setze seine Elemente mit ASSIGN. Nur `server` ist nötig, alle anderen Elemente beginnen mit Standardwerten, die zu den meisten Servern passen.

- `use_ssl` BOOLEAN: wahr für https, falsch für http (Standard falsch)
- `server` STRING: der Servername oder die IP-Adresse, darf nicht leer sein
- `port` INTEGER: 1 bis 65535, oder 0 für 80 bzw. 443 (Standard 0)
- `base_endpoint` STRING: ein Pfad vor jedem Endpunkt, z. B. /api/v2
- `user` STRING: der Benutzername für die Basic-Authentifizierung, leer für keine
- `password` STRING: das Passwort für die Basic-Authentifizierung
- `skip_ssl_validation` BOOLEAN: wahr, um jedes Zertifikat zu akzeptieren, z. B. ein selbstsigniertes (Standard falsch)
- `proxy` STRING: ein HTTP-Proxy wie http://proxy:3128, leer für keinen
- `headers` HASH STRING: Header, die mit jeder Anfrage gesendet werden, z. B. Content-Type
- `timeout` INTEGER: Sekunden, die eine Anfrage dauern darf, 1 bis 300, oder 0 für 60 (Standard 0)

Die URL einer Anfrage besteht aus dem Protokoll, dem Server, dem Port, dem Basis-Endpunkt und dem Endpunkt, z. B. `https://api.example.com/v2/users/7`. Der Endpunkt wird so verwendet, wie er ist, mit seiner Query, z. B. `/search?q=stone`. Er wird nicht codiert: Ein Leerzeichen oder ein anderes Zeichen, das in einer URL nicht stehen darf, löst INVALID\_ARGUMENT aus, z. B. `/search?city=New York`. Codiere solche Zeichen selbst, ein Leerzeichen als `%20` oder, in einer Query, als `+`. REPLACE aus c0de/strings überschreibt Zeichen, ohne die Länge des Textes zu ändern, und kann so jedes Leerzeichen von `city` in `+` verwandeln:

```c0de
CALL c0de/strings/INSTR
  PARAMETER text city ;
  PARAMETER search " " ;
  RESULT result gap
WHILE gap ≥ 0 ;
BEGIN
  CALL c0de/strings/REPLACE
    PARAMETER text city ;
    PARAMETER start gap ;
    PARAMETER insert "+" ;
    RESULT result city
  CALL c0de/strings/INSTR
    PARAMETER text city ;
    PARAMETER search " " ;
    RESULT result gap
END WHILE
```

Ein `Authorization`-Header in `headers` ersetzt die Basic-Authentifizierung. Die Header Host, Content-Length, Connection, Expect und Upgrade können nicht gesetzt werden.

Das Passwort wird im Programm wie jeder andere Wert gespeichert, und jeder, der das Blatt lesen kann, kann es lesen. Übergib Geheimnisse als Parameter, aus einer Konstantenkarte im Auslöserblock.

**Beispiel**

```c0de
PROGRAM connect
NOOP
INCLUDE c0de_net/http
NOOP
FUNCTION run
  PARAMETER token STRING
BEGIN
  VARIABLE api STRUCT c0de_net/http/HTTP_CONNECTION
  ASSIGN api.use_ssl TRUE ;
  ASSIGN api.server "api.example.com" ;
  ASSIGN api.base_endpoint "/v2" ;
  ASSIGN api.headers["Authorization"] "Bearer " ⊕ token ;
  ASSIGN api.headers["Content-Type"] "application/json" ;
  ASSIGN api.timeout 10 ;
END FUNCTION
```
