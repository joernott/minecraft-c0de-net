# Prompts

This file collects all prompts used to work on this repository.

## 2026-10-28 – Concept and plan

Version 0.3.0 has arrived on Modrinth.
I want you to start a related project and created the repo minecraft-c0de-net for it. The rules should be identical to the c0de project: Support for the same versions, a patchouli book, dokumentation.

The goal is to create a mod containing a library for c0de named net. That library should contain network related functions and types:
Look at my golang library lra (https://github.com/joernott/lra/blob/master/lra.go) for inspiration.
HTTP_CONNECTION is a struct that holds the necessary base information and for every HTTP method, there is a function to call that returns the raw data, e.g. HTTP_GET, HTTP_PUT etc. There is no need to implement the <VERB>JSON variants though.

Answers to the questions: the library is named c0de_net/net; everyone may send requests without limits, but the requests should be executed on the client, not the server.

## 2026-10-28 – Review of the concept, sprint 1

1: The client of the person running the code should run the request. If I choose to target my home automation to turn off a lamp, I don't want another player to do that.
2: Remove the socks proxies part.
3.a c0de INCLUDE book surrounded by 8 strings. After all, it's about the worldwide web.
After implementing the changes, please push sprint 0 and start with sprint 1

I have created the project on modrinth, too. The modrinth ID for it is P2irzbnD and the modrinth token has been set. in the git secrets for github actions.
