```json
{
  "title": "INVALID_ARGUMENT",
  "icon": "c0de:type_error_block",
  "category": "errors",
  "ordinal": 0
}
```

Code 309: the connection or the endpoint is not valid.

The server is empty, the port is not 0 to 65535, the timeout is not 0 to 300, the proxy is not an http:// URL, or a header can't be sent. The endpoint makes the URL invalid, e.g. with a space (see HTTP\_CONNECTION). It is also raised if the request is larger than 1,000,000 bytes.

For c0de\_net/mail: a connection without server, an SMTP\_SERVER without `from`, an IMAP\_SERVER without `user`, an unknown `security`, a timeout above the largest one, an address that isn't valid, unknown criteria of IMAP\_SEARCH, an unknown `mode` of IMAP\_STORE, or a function for messages without a selected folder.
