```json
{
  "title": "HTTP_CONNECTION",
  "icon": "c0de:type_struct_block",
  "category": "c0de_net_http",
  "ordinal": 0
}
```

A struct type with the base information of the requests to one server.

Declare it with `VARIABLE api STRUCT c0de_net/http/HTTP_CONNECTION` and set its elements with ASSIGN. Only `server` is needed, all other elements start with defaults that fit most servers.

- `use_ssl` BOOLEAN: true for https, false for http (default false)
- `server` STRING: the server name or IP address, must not be empty
- `port` INTEGER: 1 to 65535, or 0 for 80 or 443 (default 0)
- `base_endpoint` STRING: a path in front of every endpoint, like /api/v2
- `user` STRING: the user name for basic authentication, empty for none
- `password` STRING: the password for basic authentication
- `skip_ssl_validation` BOOLEAN: true to accept any certificate, e.g. a self-signed one (default false)
- `proxy` STRING: an HTTP proxy like http://proxy:3128, empty for none
- `headers` HASH STRING: headers sent with every request, like Content-Type
- `timeout` INTEGER: seconds a request may take, 1 to 300, or 0 for 60 (default 0)

The URL of a request is the protocol, the server, the port, the base endpoint and the endpoint, like `https://api.example.com/v2/users/7`. The endpoint is used as it is, with its query, like `/search?q=stone`. It isn't encoded: a space or another character that a URL can't contain raises INVALID\_ARGUMENT, like `/search?city=New York`. Encode such characters yourself, a space as `%20` or, in a query, as `+`. REPLACE of c0de/strings overwrites characters without changing the length of the text, so it can turn every space of `city` into `+`:

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

An `Authorization` header in `headers` replaces the basic authentication. The headers Host, Content-Length, Connection, Expect and Upgrade can't be set.

The password is stored in the program like any other value, and everybody who can read the sheet can read it. Pass secrets in as a parameter, from a constant item in the trigger block.

**Example**

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
