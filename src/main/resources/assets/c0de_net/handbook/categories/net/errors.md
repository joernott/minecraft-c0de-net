```json
{
  "title": "Errors",
  "icon": "c0de:type_error_block",
  "ordinal": 2
}
```

The functions raise these errors. Catch them with a variable of type ERROR on the CALL line, and compare its `code` with the constants of the library, like `c0de_net/http/HTTP_STATUS`. They hold the codes and come with INCLUDE c0de\_net/http, like the functions. INVALID\_ARGUMENT is a constant of c0de.
