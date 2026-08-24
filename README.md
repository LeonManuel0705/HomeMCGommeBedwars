# HomeMCGommeBedwars

Bedwars for HomeMC, a Minecraft 1.8 server network I built in late 2025.

Full game loop: team assignment, bed destruction and elimination, generators, the shop NPCs,
join signs, spectating, private rounds, and persistent stats in MySQL.

## Where this went

This was the largest of the HomeMC plugins and the one that made the architectural problem
obvious. Game logic, database access and scoreboard rendering all live in the same manager
classes, so nothing here can be moved to another game mode.

The successor is [PRISMARIN](https://github.com/LeonManuel0705/prismarin), which separates
the game engine from the game mode specifically so that Bedwars becomes a set of rules on top
of shared machinery rather than a plugin that reimplements everything.

This repository is kept as it was. Archived.
