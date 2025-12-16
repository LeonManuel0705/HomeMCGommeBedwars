package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.managers.MapData;
import me.leon.bedwars.managers.TeamData;
import me.leon.core.managers.RankManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SetupCommand implements CommandExecutor {

    private final BedWars plugin;
    private final Map<UUID, MapSetup> setups;

    public SetupCommand(BedWars plugin) {
        this.plugin = plugin;
        this.setups = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl verwenden!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bedwars.admin")) {
            RankManager.RankData dev = plugin.getRankManager().getRankData("homemc.developer");
            String devPrefix = dev.prefix;
            String devColor = dev.color;
            String devName = dev.displayName;
            String devFormatted = devColor + devName;
            player.sendMessage("§8§m=======§r §c§lZugriff verweigert §8§m=======§r");
            player.sendMessage("§3Um diesen Befehl auszuführen, benötigst du:");
            player.sendMessage(" §8• §2Rang: " + devFormatted);
            player.sendMessage("§8§m====================================");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        UUID uuid = player.getUniqueId();

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage(BedWars.PREFIX + "§cNutzung: /setup create <MapName>");
                    return true;
                }
                String mapName = args[1];
                setups.put(uuid, new MapSetup(mapName, player.getWorld().getName()));
                player.sendMessage(BedWars.PREFIX + "§aMap-Setup für §e" + mapName + " §agestartet!");
                player.sendMessage(BedWars.PREFIX + "§7Nutze §e/setup help §7für weitere Befehle");
                break;

            case "setlobby":
                if (!setups.containsKey(uuid)) {
                    player.sendMessage(BedWars.PREFIX + "§cErstelle zuerst eine Map mit §e/setup create");
                    return true;
                }
                setups.get(uuid).lobby = player.getLocation();
                player.sendMessage(BedWars.PREFIX + "§aLobby-Spawn gesetzt!");
                break;

            case "setspec":
                if (!setups.containsKey(uuid)) {
                    player.sendMessage(BedWars.PREFIX + "§cErstelle zuerst eine Map!");
                    return true;
                }
                setups.get(uuid).spectator = player.getLocation();
                player.sendMessage(BedWars.PREFIX + "§aSpectator-Spawn gesetzt!");
                break;

            case "setminplayers":
                if (args.length < 2) {
                    player.sendMessage(BedWars.PREFIX + "§cNutzung: /setup setminplayers <Anzahl>");
                    return true;
                }
                if (!setups.containsKey(uuid)) {
                    player.sendMessage(BedWars.PREFIX + "§cErstelle zuerst eine Map!");
                    return true;
                }
                try {
                    int min = Integer.parseInt(args[1]);
                    setups.get(uuid).minPlayers = min;
                    player.sendMessage(BedWars.PREFIX + "§aMinimale Spieler auf §e" + min + " §agesetzt!");
                } catch (NumberFormatException e) {
                    player.sendMessage(BedWars.PREFIX + "§cUngültige Zahl!");
                }
                break;

            case "setmaxplayers":
                if (args.length < 2) {
                    player.sendMessage(BedWars.PREFIX + "§cNutzung: /setup setmaxplayers <Anzahl>");
                    return true;
                }
                if (!setups.containsKey(uuid)) {
                    player.sendMessage(BedWars.PREFIX + "§cErstelle zuerst eine Map!");
                    return true;
                }
                try {
                    int max = Integer.parseInt(args[1]);
                    setups.get(uuid).maxPlayers = max;
                    player.sendMessage(BedWars.PREFIX + "§aMaximale Spieler auf §e" + max + " §agesetzt!");
                } catch (NumberFormatException e) {
                    player.sendMessage(BedWars.PREFIX + "§cUngültige Zahl!");
                }
                break;

            case "addteam":
                if (args.length < 4) {
                    player.sendMessage(BedWars.PREFIX + "§cNutzung: /setup addteam <Name> <Kurzname> <Farbe>");
                    player.sendMessage(BedWars.PREFIX + "§7Farben: RED, BLUE, GREEN, YELLOW, AQUA, LIGHT_PURPLE, GRAY, WHITE");
                    return true;
                }
                if (!setups.containsKey(uuid)) {
                    player.sendMessage(BedWars.PREFIX + "§cErstelle zuerst eine Map!");
                    return true;
                }
                try {
                    String teamName = args[1];
                    String shortName = args[2];
                    ChatColor color = ChatColor.valueOf(args[3].toUpperCase());

                    MapSetup.TeamSetup teamSetup = new MapSetup.TeamSetup(teamName, shortName, color);
                    setups.get(uuid).teams.add(teamSetup);

                    player.sendMessage(BedWars.PREFIX + "§aTeam " + color + teamName + " §ahinzugefügt!");
                    player.sendMessage(BedWars.PREFIX + "§7Setze nun die Spawns mit §e/setup setteamspawn " + teamName);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(BedWars.PREFIX + "§cUngültige Farbe!");
                }
                break;

            case "setteamspawn":
                if (args.length < 2) {
                    player.sendMessage(BedWars.PREFIX + "§cNutzung: /setup setteamspawn <TeamName>");
                    return true;
                }
                if (!setups.containsKey(uuid)) {
                    player.sendMessage(BedWars.PREFIX + "§cErstelle zuerst eine Map!");
                    return true;
                }
                MapSetup.TeamSetup team = setups.get(uuid).getTeam(args[1]);
                if (team == null) {
                    player.sendMessage(BedWars.PREFIX + "§cTeam nicht gefunden!");
                    return true;
                }
                team.spawn = player.getLocation();
                player.sendMessage(BedWars.PREFIX + "§aTeam-Spawn für " + team.color + team.name + " §agesetzt!");
                break;

            case "setteambed":
                if (args.length < 2) {
                    player.sendMessage(BedWars.PREFIX + "§cNutzung: /setup setteambed <TeamName>");
                    return true;
                }
                if (!setups.containsKey(uuid)) {
                    player.sendMessage(BedWars.PREFIX + "§cErstelle zuerst eine Map!");
                    return true;
                }
                team = setups.get(uuid).getTeam(args[1]);
                if (team == null) {
                    player.sendMessage(BedWars.PREFIX + "§cTeam nicht gefunden!");
                    return true;
                }
                team.bed = player.getLocation();
                player.sendMessage(BedWars.PREFIX + "§aBett für " + team.color + team.name + " §agesetzt!");
                break;

            case "setteamshop":
                if (args.length < 2) {
                    player.sendMessage(BedWars.PREFIX + "§cNutzung: /setup setteamshop <TeamName>");
                    return true;
                }
                if (!setups.containsKey(uuid)) {
                    player.sendMessage(BedWars.PREFIX + "§cErstelle zuerst eine Map!");
                    return true;
                }
                team = setups.get(uuid).getTeam(args[1]);
                if (team == null) {
                    player.sendMessage(BedWars.PREFIX + "§cTeam nicht gefunden!");
                    return true;
                }
                team.shopNPC = player.getLocation();
                player.sendMessage(BedWars.PREFIX + "§aShop-NPC für " + team.color + team.name + " §agesetzt!");
                break;

            case "setteamupgrade":
                if (args.length < 2) {
                    player.sendMessage(BedWars.PREFIX + "§cNutzung: /setup setteamupgrade <TeamName>");
                    return true;
                }
                if (!setups.containsKey(uuid)) {
                    player.sendMessage(BedWars.PREFIX + "§cErstelle zuerst eine Map!");
                    return true;
                }
                team = setups.get(uuid).getTeam(args[1]);
                if (team == null) {
                    player.sendMessage(BedWars.PREFIX + "§cTeam nicht gefunden!");
                    return true;
                }
                team.upgradeNPC = player.getLocation();
                player.sendMessage(BedWars.PREFIX + "§aUpgrade-NPC für " + team.color + team.name + " §agesetzt!");
                break;

            case "addteamgen":
                if (args.length < 2) {
                    player.sendMessage(BedWars.PREFIX + "§cNutzung: /setup addteamgen <TeamName>");
                    return true;
                }
                if (!setups.containsKey(uuid)) {
                    player.sendMessage(BedWars.PREFIX + "§cErstelle zuerst eine Map!");
                    return true;
                }
                team = setups.get(uuid).getTeam(args[1]);
                if (team == null) {
                    player.sendMessage(BedWars.PREFIX + "§cTeam nicht gefunden!");
                    return true;
                }
                team.generators.add(player.getLocation());
                player.sendMessage(BedWars.PREFIX + "§aGenerator für " + team.color + team.name + " §ahinzugefügt!");
                break;

            case "addbronze":
                if (!setups.containsKey(uuid)) {
                    player.sendMessage(BedWars.PREFIX + "§cErstelle zuerst eine Map!");
                    return true;
                }
                setups.get(uuid).bronzeGens.add(player.getLocation());
                player.sendMessage(BedWars.PREFIX + "§aBronze-Generator hinzugefügt!");
                break;

            case "addgold":
                if (!setups.containsKey(uuid)) {
                    player.sendMessage(BedWars.PREFIX + "§cErstelle zuerst eine Map!");
                    return true;
                }
                setups.get(uuid).goldGens.add(player.getLocation());
                player.sendMessage(BedWars.PREFIX + "§aGold-Generator hinzugefügt!");
                break;

            case "save":
                if (!setups.containsKey(uuid)) {
                    player.sendMessage(BedWars.PREFIX + "§cErstelle zuerst eine Map!");
                    return true;
                }
                MapSetup setup = setups.get(uuid);
                if (!setup.isComplete()) {
                    player.sendMessage(BedWars.PREFIX + "§cMap ist nicht vollständig eingerichtet!");
                    player.sendMessage(BedWars.PREFIX + "§7Nutze §e/setup check §7um zu sehen, was fehlt");
                    return true;
                }

                MapData mapData = setup.build();
                plugin.getMapManager().saveMap(mapData);
                plugin.getMapManager().loadMaps();

                setups.remove(uuid);
                player.sendMessage(BedWars.PREFIX + "§aMap erfolgreich gespeichert!");
                break;

            case "check":
                if (!setups.containsKey(uuid)) {
                    player.sendMessage(BedWars.PREFIX + "§cErstelle zuerst eine Map!");
                    return true;
                }
                setup = setups.get(uuid);
                player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━");
                player.sendMessage("§c§lMap Setup Check");
                player.sendMessage("");
                player.sendMessage("§7Lobby: " + (setup.lobby != null ? "§a✓" : "§c✗"));
                player.sendMessage("§7Spectator: " + (setup.spectator != null ? "§a✓" : "§c✗"));
                player.sendMessage("§7Min Spieler: " + (setup.minPlayers > 0 ? "§a" + setup.minPlayers : "§c✗"));
                player.sendMessage("§7Max Spieler: " + (setup.maxPlayers > 0 ? "§a" + setup.maxPlayers : "§c✗"));
                player.sendMessage("§7Teams: " + (setup.teams.size() > 0 ? "§a" + setup.teams.size() : "§c✗"));
                player.sendMessage("");
                for (MapSetup.TeamSetup t : setup.teams) {
                    player.sendMessage(t.color + "§l" + t.name + ":");
                    player.sendMessage("  §7Spawn: " + (t.spawn != null ? "§a✓" : "§c✗"));
                    player.sendMessage("  §7Bett: " + (t.bed != null ? "§a✓" : "§c✗"));
                    player.sendMessage("  §7Shop: " + (t.shopNPC != null ? "§a✓" : "§c✗"));
                    player.sendMessage("  §7Upgrade: " + (t.upgradeNPC != null ? "§a✓" : "§c✗"));
                    player.sendMessage("  §7Generatoren: " + (t.generators.size() > 0 ? "§a" + t.generators.size() : "§c✗"));
                }
                player.sendMessage("");
                player.sendMessage("§7Bronze-Generatoren: §e" + setup.bronzeGens.size());
                player.sendMessage("§7Gold-Generatoren: §e" + setup.goldGens.size());
                player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━");
                break;

            case "cancel":
                if (setups.remove(uuid) != null) {
                    player.sendMessage(BedWars.PREFIX + "§cMap-Setup abgebrochen!");
                } else {
                    player.sendMessage(BedWars.PREFIX + "§cKein aktives Setup!");
                }
                break;

            case "help":
                sendHelp(player);
                break;

            default:
                player.sendMessage(BedWars.PREFIX + "§cUnbekannter Befehl! Nutze §e/setup help");
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§c§lBedWars Setup Commands");
        player.sendMessage("");
        player.sendMessage("§e/setup create <Name> §7- Map erstellen");
        player.sendMessage("§e/setup setlobby §7- Lobby setzen");
        player.sendMessage("§e/setup setspec §7- Spectator Spawn setzen");
        player.sendMessage("§e/setup setminplayers <Zahl> §7- Min. Spieler");
        player.sendMessage("§e/setup setmaxplayers <Zahl> §7- Max. Spieler");
        player.sendMessage("");
        player.sendMessage("§e/setup addteam <Name> <Short> <Farbe>");
        player.sendMessage("§e/setup setteamspawn <Team> §7- Team Spawn");
        player.sendMessage("§e/setup setteambed <Team> §7- Team Bett");
        player.sendMessage("§e/setup setteamshop <Team> §7- Shop NPC");
        player.sendMessage("§e/setup setteamupgrade <Team> §7- Upgrade NPC");
        player.sendMessage("§e/setup addteamgen <Team> §7- Team Generator");
        player.sendMessage("");
        player.sendMessage("§e/setup addbronze §7- Bronze Generator");
        player.sendMessage("§e/setup addgold §7- Gold Generator");
        player.sendMessage("");
        player.sendMessage("§e/setup check §7- Setup überprüfen");
        player.sendMessage("§e/setup save §7- Map speichern");
        player.sendMessage("§e/setup cancel §7- Setup abbrechen");
        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private static class MapSetup {
        String name;
        String worldName;
        int minPlayers;
        int maxPlayers;
        Location lobby;
        Location spectator;
        List<TeamSetup> teams;
        List<Location> bronzeGens;
        List<Location> goldGens;

        MapSetup(String name, String worldName) {
            this.name = name;
            this.worldName = worldName;
            this.teams = new ArrayList<>();
            this.bronzeGens = new ArrayList<>();
            this.goldGens = new ArrayList<>();
        }

        TeamSetup getTeam(String name) {
            for (TeamSetup team : teams) {
                if (team.name.equalsIgnoreCase(name)) {
                    return team;
                }
            }
            return null;
        }

        boolean isComplete() {
            if (lobby == null || spectator == null || minPlayers <= 0 || maxPlayers <= 0) {
                return false;
            }
            if (teams.isEmpty()) {
                return false;
            }
            for (TeamSetup team : teams) {
                if (!team.isComplete()) {
                    return false;
                }
            }
            return true;
        }

        MapData build() {
            List<TeamData> teamDataList = new ArrayList<>();
            for (TeamSetup team : teams) {
                teamDataList.add(new TeamData(
                        team.name,
                        team.shortName,
                        team.color,
                        team.spawn,
                        team.bed,
                        team.shopNPC,
                        team.upgradeNPC,
                        team.generators
                ));
            }

            return new MapData(
                    name,
                    worldName,
                    minPlayers,
                    maxPlayers,
                    lobby,
                    spectator,
                    teamDataList,
                    bronzeGens,
                    goldGens
            );
        }

        static class TeamSetup {
            String name;
            String shortName;
            ChatColor color;
            Location spawn;
            Location bed;
            Location shopNPC;
            Location upgradeNPC;
            List<Location> generators;

            TeamSetup(String name, String shortName, ChatColor color) {
                this.name = name;
                this.shortName = shortName;
                this.color = color;
                this.generators = new ArrayList<>();
            }

            boolean isComplete() {
                return spawn != null && bed != null && shopNPC != null &&
                        upgradeNPC != null && !generators.isEmpty();
            }
        }
    }
}