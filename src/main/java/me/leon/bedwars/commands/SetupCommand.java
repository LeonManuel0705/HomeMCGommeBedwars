package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.Arena.GeneratorType;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
import me.leon.bedwars.managers.TeamManager.TeamColor;
import me.leon.core.managers.RankManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.leon.bedwars.lobby.LobbyManager;
import me.leon.bedwars.managers.TeamManager.TeamColor;

public class SetupCommand implements CommandExecutor {

    private final BedWars plugin;

    public SetupCommand(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl nutzen!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bedwars.setup")) {
            RankManager.RankData dev = plugin.getCore().getRankManager().getRankData("homemc.developer");
            String devPrefix = dev.prefix;
            String devColor = dev.color;
            String devName = dev.displayName;
            String devFormatted = devColor + devName;
            sender.sendMessage("§8§m=======§r §c§lZugriff verweigert §8§m=======§r");
            sender.sendMessage("§3Um diesen Befehl zu nutzen, benötigst du:");
            sender.sendMessage(" §8• §2Rang: " + devFormatted);
            sender.sendMessage("§8§m====================================");
            return true;
        }

        if (args.length == 0) {
            sendSetupHelp(player);
            return true;
        }

        String arenaName;

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 6) {
                    player.sendMessage("§c/setup create <name> <displayName> <minPlayers> <maxPlayers> <mode>");
                    player.sendMessage("§7Modes: SOLO, DUO, TEAM");
                    return true;
                }

                arenaName = args[1];
                String displayName = args[2].replace("_", " ");
                int minPlayers;
                int maxPlayers;

                try {
                    minPlayers = Integer.parseInt(args[3]);
                    maxPlayers = Integer.parseInt(args[4]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cMin- und Max-Spieler müssen Zahlen sein!");
                    return true;
                }

                LobbyManager.GameMode gameMode;
                try {
                    gameMode = LobbyManager.GameMode.valueOf(args[5].toUpperCase());
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cUngültiger GameMode!");
                    player.sendMessage("§7Verfügbar: SOLO, DUO, TEAM");
                    return true;
                }

                int teamsPerGame;

                switch (gameMode) {
                    case SOLO:
                        teamsPerGame = 8;
                        break;
                    case DUO:
                        teamsPerGame = 8;
                        break;
                    case TEAM:
                    default:
                        teamsPerGame = 4;
                        break;
                }

                Arena arena = new Arena(
                        arenaName,
                        displayName,
                        minPlayers,
                        maxPlayers,
                        teamsPerGame,
                        gameMode
                );

                plugin.getGameManager().addArena(arena);

                player.sendMessage("§aArena §e" + arenaName + " §aerstellt!");
                player.sendMessage("§7Mode: §e" + gameMode.name());
                break;

            case "setlobby":
                if (args.length < 2) {
                    player.sendMessage("§c/setup setlobby <arena>");
                    return true;
                }

                Arena lobbyArena = plugin.getGameManager().getArena(args[1]);
                if (lobbyArena == null) {
                    player.sendMessage("§cArena nicht gefunden!");
                    return true;
                }

                lobbyArena.setLobby(player.getLocation());
                plugin.getArenaManager().saveArena(lobbyArena);
                player.sendMessage("§aLobby gesetzt!");
                break;

            case "setspectator":
                if (args.length < 2) {
                    player.sendMessage("§c/setup setspectator <arena>");
                    return true;
                }

                Arena specArena = plugin.getGameManager().getArena(args[1]);
                if (specArena == null) {
                    player.sendMessage("§cArena nicht gefunden!");
                    return true;
                }

                specArena.setSpectator(player.getLocation());
                plugin.getArenaManager().saveArena(specArena);
                player.sendMessage("§aSpectator-Spawn gesetzt!");
                break;

            case "addteam":
                if (args.length < 4) {
                    player.sendMessage("§c/setup addteam <arena> <color> <maxPlayers>");
                    player.sendMessage("§7Farben: RED, BLUE, GREEN, YELLOW, AQUA, PINK, GRAY, WHITE");
                    return true;
                }

                Arena teamArena = plugin.getGameManager().getArena(args[1]);
                if (teamArena == null) {
                    player.sendMessage("§cArena nicht gefunden!");
                    return true;
                }

                TeamColor color;
                try {
                    color = TeamColor.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cUngültige Farbe! Verfügbar: RED, BLUE, GREEN, YELLOW, AQUA, PINK, GRAY, WHITE");
                    return true;
                }

                int teamSize = Integer.parseInt(args[3]);

                BedWarsTeam team = new BedWarsTeam(color, teamSize);
                teamArena.getTeams().put(color.name(), team);

                player.sendMessage("§aTeam " + color.getColorCode()
                        + color.getDisplayName()
                        + " §ahinzugefügt!");
                break;

            case "setspawn":
                if (args.length < 3) {
                    player.sendMessage("§c/setup setspawn <arena> <team>");
                    return true;
                }

                Arena spawnArena = plugin.getGameManager().getArena(args[1]);
                if (spawnArena == null) {
                    player.sendMessage("§cArena nicht gefunden!");
                    return true;
                }

                BedWarsTeam spawnTeam = spawnArena.getTeams().get(args[2].toUpperCase());
                if (spawnTeam == null) {
                    player.sendMessage("§cTeam nicht gefunden!");
                    return true;
                }

                spawnTeam.setSpawn(player.getLocation());
                plugin.getArenaManager().saveArena(spawnArena);
                player.sendMessage("§aSpawn für " + spawnTeam.getColoredName() + " §agesetzt!");
                break;

            case "setbed":
                if (args.length < 3) {
                    player.sendMessage("§c/setup setbed <arena> <team>");
                    return true;
                }

                Arena bedArena = plugin.getGameManager().getArena(args[1]);
                if (bedArena == null) {
                    player.sendMessage("§cArena nicht gefunden!");
                    return true;
                }

                BedWarsTeam bedTeam = bedArena.getTeams().get(args[2].toUpperCase());
                if (bedTeam == null) {
                    player.sendMessage("§cTeam nicht gefunden!");
                    return true;
                }

                bedTeam.setBedLocation(player.getLocation());
                plugin.getArenaManager().saveArena(bedArena);
                player.sendMessage("§aBett für " + bedTeam.getColoredName() + " §agesetzt!");
                break;

            case "setshop":
                if (args.length < 3) {
                    player.sendMessage("§c/setup setshop <arena> <team>");
                    return true;
                }

                Arena shopArena = plugin.getGameManager().getArena(args[1]);
                if (shopArena == null) {
                    player.sendMessage("§cArena nicht gefunden!");
                    return true;
                }

                BedWarsTeam shopTeam = shopArena.getTeams().get(args[2].toUpperCase());
                if (shopTeam == null) {
                    player.sendMessage("§cTeam nicht gefunden!");
                    return true;
                }

                shopTeam.setShopVillager(player.getLocation());
                plugin.getArenaManager().saveArena(shopArena);
                player.sendMessage("§aShop für " + shopTeam.getColoredName() + " §agesetzt!");
                break;

            case "addgenerator":
                if (args.length < 3) {
                    player.sendMessage("§c/setup addgenerator <arena> <type>");
                    player.sendMessage("§7Typen: BRONZE, IRON, GOLD");
                    return true;
                }

                Arena genArena = plugin.getGameManager().getArena(args[1]);
                if (genArena == null) {
                    player.sendMessage("§cArena nicht gefunden!");
                    return true;
                }

                GeneratorType genType;
                try {
                    genType = GeneratorType.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cUngültiger Typ! Verfügbar: BRONZE, IRON, GOLD");
                    return true;
                }

                plugin.getGeneratorManager().createGenerator(genArena, player.getLocation(), genType);
                plugin.getArenaManager().saveArena(genArena);
                player.sendMessage("§a" + genType.name() + "-Generator hinzugefügt!");
                break;

            case "save":
                if (args.length < 2) {
                    player.sendMessage("§c/setup save <arena>");
                    return true;
                }

                Arena saveArena = plugin.getGameManager().getArena(args[1]);
                if (saveArena == null) {
                    player.sendMessage("§cArena nicht gefunden!");
                    return true;
                }

                plugin.getArenaManager().saveArena(saveArena);
                player.sendMessage("§aArena gespeichert!");
                break;

            default:
                sendSetupHelp(player);
                break;
        }

        return true;
    }

    private void sendSetupHelp(Player player) {
        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§6§lBedWars Setup:");
        player.sendMessage("§e/setup create <name> <display> <min> <max> <mode>");
        player.sendMessage("§7§oModes: SOLO, DUO, TEAM");
        player.sendMessage("§e/setup setlobby <arena>");
        player.sendMessage("§e/setup setspectator <arena>");
        player.sendMessage("§e/setup addteam <arena> <color> <size>");
        player.sendMessage("§e/setup setspawn <arena> <team>");
        player.sendMessage("§e/setup setbed <arena> <team>");
        player.sendMessage("§e/setup setshop <arena> <team>");
        player.sendMessage("§e/setup addgenerator <arena> <type>");
        player.sendMessage("§e/setup save <arena>");
        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}