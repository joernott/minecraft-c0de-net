```json
{
  "title": "Wie Anfragen gesendet werden",
  "icon": "minecraft:string",
  "category": "getting_started",
  "ordinal": 0
}
```

Der Minecraft-Server sendet die Anfragen nicht. Der Spiel-Client des Spielers, der den Prozess ausführt, sendet sie, von dessen Computer aus.

Wenn ein Spieler den Prozess gestartet hat, mit einem Knopf, einem Hebel oder einer Druckplatte am Auslöserblock, ist es dieser Spieler. Wenn niemand ihn gestartet hat, z. B. bei einem Zeitgeberblock oder einer Redstone-Uhr, ist es der Besitzer des Auslöserblocks.

Dieser Spieler muss online sein und c0de net installiert haben. Ein Programm erreicht also, was der Computer dieses Spielers erreichen kann, auch sein Heimnetz. Andere Spieler, die den Knopf drücken, senden die Anfrage von ihrem eigenen Computer, nie von dem des Besitzers.

Eine Anfrage braucht Zeit. Die CALL-Zeile wartet, bis die Antwort ankommt, ein Fehler auftritt oder das Zeitlimit abgelaufen ist. Andere Prozesse und der Server laufen währenddessen weiter.

Wenn der Prozess endet, während eine Anfrage läuft, wird die Anfrage abgebrochen. Nach einem Neustart des Servers sendet ein wiederhergestellter Prozess seine Anfrage erneut. Denk daran bei Anfragen, die Daten ändern, z. B. POST und DELETE.

In einer Einzelspielerwelt laufen Server und Client auf demselben Computer, und Anfragen funktionieren genauso.

Die Funktionen von c0de\_net/mail arbeiten genauso: Der Client dieses Spielers verbindet sich mit dem Mailserver.
