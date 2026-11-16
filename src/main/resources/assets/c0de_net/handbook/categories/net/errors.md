```json
{
  "title": "Errors",
  "icon": "c0de:type_error_block",
  "ordinal": 3
}
```

The functions raise these errors. Catch them with a variable of type ERROR on the CALL line, and compare its `code` with the constants of the library, like `c0de_net/http/HTTP_STATUS`. They hold the codes and come with the INCLUDE, like the functions. c0de\_net/mail has the constants of the errors it raises, like `c0de_net/mail/REQUEST_FAILED`. INVALID\_ARGUMENT and PERMISSION\_DENIED are constants of c0de.
