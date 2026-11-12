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

## 2026-10-28 – Library c0de_net/http

yes, please push the changes and start sprint 2 as soon as there are green results.

There is one change I'd like to implement. Change the library name to c0de_net/http, as we might add other libraries in this mod for other network related protocols like SMTP, IMAP etc.

## 2026-10-28 – Sprint 3

please go head with sprint 3

## 2026-10-29 – Sprint 4

yes, please

## 2026-10-29 – Logo with a cobweb, sprint 5

The logo is nice, please create an alternative version with the picture, Minecraft uses for a cobweb in an item frame instead of the globe. Then proceed with sprint 5

Please just take the cobweb without the item frame and put it on a black background partially covering the two slashes

Please let the cobweb fill the whole picture, almost covering the slashes is fine with me. And yes, I'd like the cobweb logo to ship with the mod

That picture is fine, thank you

## 2026-11-11 – Documentation sprint for the tutorials of c0de.at

To answer the questions:
T1, T2: The proposals make sense
T3: The proposal makes sense, please add a new sprint in the implementation plans for minecraft-c0de and minecraft-code-net, labeled documentation. Add this as task to these sprints, also add a task to generate the german translation to the existing documentation to these sprints.
T4: Add another step to the documentation sprint of minecraft-c0de to extract every block as a 3d models and use something like https://modelviewer.dev/ here to display the examples as 3d models. Screenshots still make sense though as a fallback and for displaying outputs.
T5: The proposal is fine, maybe also implement a version checker for c0de by checking the latest release version on github for the minecraft-c0de project.
T6: Yes, this should be stated somewhere explicitly.
T7: The proposal makes sense

## 2026-11-11 – Version constants

Add a task to the sprints on c0de and c0de-net to provide a constant C0DE_VERSION respective C0DE_NET_VERSION that will always contain the mod version. That should close the gap. After that, please push the three branches

## 2026-11-11 – Sprint 6

please continue with c0de-net. It also has a PR open from Dependabot, so please integrate it at the beginning of Sprint 6

## 2026-11-11 – Stuck workflow

The workflow is stuck on two tasks, I've tried to cancel it now

## 2026-11-11 – GitHub problems

The CI didn't even initialize the jobs, so it is most likely due to Github problems.

## 2026-11-11 – Releases

yes, please do (VM review and release 0.3.0 of the website, release 0.2.0 of c0de net)

## 2026-11-11 – Release 0.2.0

I have pushed the tag cor c0de-net

## 2026-11-12 – Feedback from the recipes

(No prompt of its own: the recipes of c0de.at (www-c0de sprint 9) found gaps in the handbook; sprint 7.)

## 2026-11-12 – CurseForge

add another task to both c0de and c0de-net: I have created a curseforge account and want to publish the mods on curseforge as well. This should be included into the release process. Please tell me, what I need to provide for you to achieve that.

## 2026-11-12 – CurseForge project IDs

OK c0de has the ID 1800913 and c0de-net has 1800920. Please add them to the stonecutter config. I have also added the secret CURSEFORGE_TOKEN to both repos.

Curseforge reviews only happen, once files are attached to the project. Please add the current release there as well, so that the review process can start.
