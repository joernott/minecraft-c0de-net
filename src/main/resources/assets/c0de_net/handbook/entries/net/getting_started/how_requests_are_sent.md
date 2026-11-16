```json
{
  "title": "How requests are sent",
  "icon": "minecraft:string",
  "category": "getting_started",
  "ordinal": 0
}
```

The Minecraft server does not send the requests. The game client of the player who runs the process sends them, from that player's computer.

If a player started the process, with a button, a lever or a pressure plate at the trigger block, it is that player. If nobody did, like a timer block or a redstone clock, it is the owner of the trigger block.

That player must be online and have c0de net installed. So a program reaches what that player's computer can reach, including their home network. Other players who press the button send the request from their own computer, never from the owner's.

A request takes time. The CALL line waits until the answer arrives, an error occurs or the timeout has passed. Other processes and the server go on meanwhile.

If the process ends while a request is running, the request is cancelled. After a server restart, a restored process sends its request again. Keep this in mind for requests that change data, like POST and DELETE.

On a single-player world, the server and the client run on the same computer, and requests work the same way.

The functions of c0de\_net/mail work the same way: the client of that player connects to the mail server.
