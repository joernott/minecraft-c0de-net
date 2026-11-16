```json
{
  "title": "REQUEST_FAILED",
  "icon": "c0de:type_error_block",
  "category": "errors",
  "ordinal": 4
}
```

Code 1003, `c0de_net/http/REQUEST_FAILED`: the request could not be sent, or there was no answer.

The message names the reason, like an unknown host, a refused connection, a failed SSL handshake or a proxy error. A self-signed certificate needs `skip_ssl_validation`.
