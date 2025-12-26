package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.managers.PrivateRoundManager;
import me.leon.bedwars.managers.PrivateRoundManager.PrivateRound;
import me.leon.bedwars.managers.PrivateRoundManager.PrivateSettings;
import me.leon.core.managers.RankManager;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PrivateCommand implements CommandExecutor {

    private final BedWars plugin;
    private final Set<UUID> godModePlayers = new HashSet<>();

    public PrivateCommand(BedWars plugin) {
        this.plugin = plugin;
    }

    public boolean isInGodMode(UUID playerId) {
        return godModePlayers.contains(playerId);
    }

    public void removeGodMode(UUID playerId) {
        godModePlayers.remove(playerId);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl nutzen!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bedwars.private")) {
            RankManager.RankData vip = plugin.getCore().getRankManager().getRankData("homemc.vip");
            String vipColor = vip != null ? vip.color : "§a";
            String vipName = vip != null ? vip.displayName : "VIP";
            String vipFormatted = vipColor + vipName;
            player.sendMessage("§8§m=======§r §c§lZugriff verweigert §8§m=======§r");
            player.sendMessage("§3Um diesen Befehl zu nutzen, benötigst du:");
            player.sendMessage(" §8• §2Rang: " + vipFormatted + " §7oder höher");
            player.sendMessage("§8§m====================================");
            return true;
        }

        if (args.length == 0) {
            plugin.getPrivateRoundGUI().openMainMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "gui":
            case "menu":
                plugin.getPrivateRoundGUI().openMainMenu(player);
                break;
            case "create":
                handleCreate(player, args);
                break;
            case "invite":
                handleInvite(player, args);
                break;
            case "join":
                handleJoin(player, args);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "start":
                handleStart(player);
                break;
            case "settings":
                handleSettings(player, args);
                break;
            case "delete":
            case "cancel":
                handleDelete(player);
                break;
            case "info":
                handleInfo(player);
                break;
            case "fly":
                handleFly(player);
                break;
            case "gm":
            case "gamemode":
                handleGamemode(player, args);
                break;
            case "god":
                handleGod(player);
                break;
            case "kick":
                handleKick(player, args);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(BedWars.PREFIX + "§cNutze: /private create <map>");
            player.sendMessage(BedWars.PREFIX + "§7Verfügbare Maps:");
            for (me.leon.bedwars.game.Arena a : plugin.getArenaManager().getArenas()) {
                player.sendMessage("  §8- §e" + a.getName() + " §7(" + a.getDisplayName() + ")");
            }
            return;
        }

        plugin.getPrivateRoundManager().createPrivateRound(player, args[1]);
    }

    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(BedWars.PREFIX + "§cNutze: /private invite <spieler>");
            return;
        }

        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(BedWars.PREFIX + "§cSpieler nicht gefunden!");
            return;
        }

        plugin.getPrivateRoundManager().invitePlayer(player, target);
    }

    private void handleJoin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(BedWars.PREFIX + "§cNutze: /private join <ersteller>");
            return;
        }

        Player creator = plugin.getServer().getPlayer(args[1]);
        if (creator == null) {
            player.sendMessage(BedWars.PREFIX + "§cSpieler nicht gefunden!");
            return;
        }

        plugin.getPrivateRoundManager().joinPrivateRound(player, creator);
    }

    private void handleLeave(Player player) {
        plugin.getPrivateRoundManager().leavePrivateRound(player);
    }

    private void handleStart(Player player) {
        plugin.getPrivateRoundManager().startPrivateRound(player);
    }

    private void handleSettings(Player player, String[] args) {
        PrivateRound round = plugin.getPrivateRoundManager().getPlayerRound(player.getUniqueId());
        if (round == null) {
            player.sendMessage(BedWars.PREFIX + "§cDu hast keine private Runde!");
            return;
        }

        if (!round.getCreator().equals(player.getUniqueId())) {
            player.sendMessage(BedWars.PREFIX + "§cNur der Ersteller kann Einstellungen ändern!");
            return;
        }

        if (args.length < 2) {
            showSettings(player, round.getSettings());
            return;
        }

        if (args.length < 3) {
            player.sendMessage(BedWars.PREFIX + "§cNutze: /private settings <einstellung> <wert>");
            return;
        }

        PrivateSettings settings = round.getSettings();
        String setting = args[1].toLowerCase();
        String value = args[2].toLowerCase();

        switch (setting) {
            case "gold":
                boolean goldEnabled = value.equals("true") || value.equals("on") || value.equals("ja");
                settings.setGoldEnabled(goldEnabled);
                player.sendMessage(BedWars.PREFIX + "§aGold wurde " + (goldEnabled ? "§aaktiviert" : "§cdeaktiviert"));
                break;

            case "iron":
            case "eisen":
                boolean ironEnabled = value.equals("true") || value.equals("on") || value.equals("ja");
                settings.setIronEnabled(ironEnabled);
                player.sendMessage(BedWars.PREFIX + "§aEisen wurde " + (ironEnabled ? "§aaktiviert" : "§cdeaktiviert"));
                break;

            case "health":
            case "leben":
                try {
                    double health = Double.parseDouble(value);
                    if (health < 1 || health > 100) {
                        player.sendMessage(BedWars.PREFIX + "§cLeben muss zwischen 1 und 100 sein!");
                        return;
                    }
                    settings.setMaxHealth(health);
                    player.sendMessage(BedWars.PREFIX + "§aMax. Leben auf §e" + health + " §agesetzt!");
                } catch (NumberFormatException e) {
                    player.sendMessage(BedWars.PREFIX + "§cUngültige Zahl!");
                }
                break;

            case "bronze":
            case "bronzerate":
                try {
                    int rate = Integer.parseInt(value);
                    if (rate < 1 || rate > 60) {
                        player.sendMessage(BedWars.PREFIX + "§cSpawnrate muss zwischen 1 und 60 Sekunden sein!");
                        return;
                    }
                    settings.setBronzeSpawnRate(rate);
                    player.sendMessage(BedWars.PREFIX + "§aBronze-Spawnrate auf §e" + rate + "s §agesetzt!");
                } catch (NumberFormatException e) {
                    player.sendMessage(BedWars.PREFIX + "§cUngültige Zahl!");
                }
                break;

            case "ironrate":
            case "eisenrate":
                try {
                    int rate = Integer.parseInt(value);
                    if (rate < 1 || rate > 120) {
                        player.sendMessage(BedWars.PREFIX + "§cSpawnrate muss zwischen 1 und 120 Sekunden sein!");
                        return;
                    }
                    settings.setIronSpawnRate(rate);
                    player.sendMessage(BedWars.PREFIX + "§aEisen-Spawnrate auf §e" + rate + "s §agesetzt!");
                } catch (NumberFormatException e) {
                    player.sendMessage(BedWars.PREFIX + "§cUngültige Zahl!");
                }
                break;

            case "goldrate":
                try {
                    int rate = Integer.parseInt(value);
                    if (rate < 1 || rate > 300) {
                        player.sendMessage(BedWars.PREFIX + "§cSpawnrate muss zwischen 1 und 300 Sekunden sein!");
                        return;
                    }
                    settings.setGoldSpawnRate(rate);
                    player.sendMessage(BedWars.PREFIX + "§aGold-Spawnrate auf §e" + rate + "s §agesetzt!");
                } catch (NumberFormatException e) {
                    player.sendMessage(BedWars.PREFIX + "§cUngültige Zahl!");
                }
                break;

            case "teamsize":
            case "team":
                try {
                    int size = Integer.parseInt(value);
                    if (size < 1 || size > 4) {
                        player.sendMessage(BedWars.PREFIX + "§cTeamgröße muss zwischen 1 und 4 sein!");
                        return;
                    }
                    settings.setPlayersPerTeam(size);
                    player.sendMessage(BedWars.PREFIX + "§aTeamgröße auf §e" + size + " §agesetzt!");
                } catch (NumberFormatException e) {
                    player.sendMessage(BedWars.PREFIX + "§cUngültige Zahl!");
                }
                break;

            default:
                player.sendMessage(BedWars.PREFIX + "§cUnbekannte Einstellung: " + setting);
                showSettings(player, settings);
                break;
        }
    }

    private void showSettings(Player player, PrivateSettings settings) {
        player.sendMessage("§8§m=======§r §6§lEinstellungen §8§m=======§r");
        player.sendMessage("§7Gold: " + (settings.isGoldEnabled() ? "§aAktiviert" : "§cDeaktiviert"));
        player.sendMessage("§7Eisen: " + (settings.isIronEnabled() ? "§aAktiviert" : "§cDeaktiviert"));
        player.sendMessage("§7Max. Leben: §e" + settings.getMaxHealth());
        player.sendMessage("§7Bronze-Spawnrate: §e" + settings.getBronzeSpawnRate() + "s");
        player.sendMessage("§7Eisen-Spawnrate: §e" + settings.getIronSpawnRate() + "s");
        player.sendMessage("§7Gold-Spawnrate: §e" + settings.getGoldSpawnRate() + "s");
        player.sendMessage("§7Teamgröße: §e" + settings.getPlayersPerTeam());
        player.sendMessage("§8§m================================");
        player.sendMessage("§7Ändere mit: §e/private settings <einstellung> <wert>");
        player.sendMessage("§7Einstellungen: §fgold, iron, health, bronze, ironrate, goldrate, teamsize");
    }

    private void handleDelete(Player player) {
        PrivateRound round = plugin.getPrivateRoundManager().getPlayerRound(player.getUniqueId());
        if (round == null) {
            player.sendMessage(BedWars.PREFIX + "§cDu hast keine private Runde!");
            return;
        }

        if (!round.getCreator().equals(player.getUniqueId())) {
            player.sendMessage(BedWars.PREFIX + "§cNur der Ersteller kann die Runde löschen!");
            return;
        }

        plugin.getPrivateRoundManager().deletePrivateRound(round);
        player.sendMessage(BedWars.PREFIX + "§aPrivate Runde gelöscht!");
    }

    private void handleInfo(Player player) {
        PrivateRound round = plugin.getPrivateRoundManager().getPlayerRound(player.getUniqueId());
        if (round == null) {
            player.sendMessage(BedWars.PREFIX + "§cDu bist in keiner privaten Runde!");
            return;
        }

        Player creator = plugin.getServer().getPlayer(round.getCreator());
        String creatorName = creator != null ? creator.getName() : "Unbekannt";

        player.sendMessage("§8§m=======§r §6§lPrivate Runde §8§m=======§r");
        player.sendMessage("§7Ersteller: §e" + creatorName);
        player.sendMessage("§7Map: §e" + round.getTemplateArena().getDisplayName());
        player.sendMessage("§7Spieler: §e" + round.getPlayers().size());
        player.sendMessage("§7Status: " + (round.isStarted() ? "§aGestartet" : "§eWarten"));
        player.sendMessage("§8§m==============================");

        if (!round.getPlayers().isEmpty()) {
            player.sendMessage("§7Spieler in der Runde:");
            for (java.util.UUID uuid : round.getPlayers()) {
                Player p = plugin.getServer().getPlayer(uuid);
                if (p != null) {
                    player.sendMessage("  §8- §f" + p.getName());
                }
            }
        }
    }

    private boolean isOwnerInPrivateRound(Player player) {
        PrivateRound round = plugin.getPrivateRoundManager().getPlayerRound(player.getUniqueId());
        if (round == null) {
            return false;
        }
        if (!round.getCreator().equals(player.getUniqueId())) {
            return false;
        }
        if (!player.getWorld().equals(round.getPrivateWorld())) {
            return false;
        }
        return true;
    }

    private void handleFly(Player player) {
        if (!isOwnerInPrivateRound(player)) {
            player.sendMessage(BedWars.PREFIX + "§cDieser Befehl ist nur in deiner privaten Runde verfügbar!");
            return;
        }

        boolean flying = !player.getAllowFlight();
        player.setAllowFlight(flying);
        player.setFlying(flying);

        if (flying) {
            player.sendMessage(BedWars.PREFIX + "§aFlugmodus §2aktiviert§a!");
        } else {
            player.sendMessage(BedWars.PREFIX + "§aFlugmodus §cdeaktiviert§a!");
        }
    }

    private void handleGamemode(Player player, String[] args) {
        if (!isOwnerInPrivateRound(player)) {
            player.sendMessage(BedWars.PREFIX + "§cDieser Befehl ist nur in deiner privaten Runde verfügbar!");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(BedWars.PREFIX + "§cNutze: /private gm <0|1|2|3>");
            player.sendMessage(BedWars.PREFIX + "§70 = Survival, 1 = Creative, 2 = Adventure, 3 = Spectator");
            return;
        }

        GameMode gameMode;
        switch (args[1].toLowerCase()) {
            case "0":
            case "s":
            case "survival":
                gameMode = GameMode.SURVIVAL;
                break;
            case "1":
            case "c":
            case "creative":
                gameMode = GameMode.CREATIVE;
                break;
            case "2":
            case "a":
            case "adventure":
                gameMode = GameMode.ADVENTURE;
                break;
            case "3":
            case "sp":
            case "spectator":
                gameMode = GameMode.SPECTATOR;
                break;
            default:
                player.sendMessage(BedWars.PREFIX + "§cUngültiger Spielmodus! Nutze 0, 1, 2 oder 3");
                return;
        }

        player.setGameMode(gameMode);
        player.sendMessage(BedWars.PREFIX + "§aSpielmodus auf §e" + gameMode.name() + " §agesetzt!");
    }

    private void handleGod(Player player) {
        if (!isOwnerInPrivateRound(player)) {
            player.sendMessage(BedWars.PREFIX + "§cDieser Befehl ist nur in deiner privaten Runde verfügbar!");
            return;
        }

        boolean godMode = !godModePlayers.contains(player.getUniqueId());

        if (godMode) {
            godModePlayers.add(player.getUniqueId());
            player.sendMessage(BedWars.PREFIX + "§aGod-Modus §2aktiviert§a!");
        } else {
            godModePlayers.remove(player.getUniqueId());
            player.sendMessage(BedWars.PREFIX + "§aGod-Modus §cdeaktiviert§a!");
        }
    }

    private void handleKick(Player player, String[] args) {
        if (!isOwnerInPrivateRound(player)) {
            player.sendMessage(BedWars.PREFIX + "§cDieser Befehl ist nur in deiner privaten Runde verfügbar!");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(BedWars.PREFIX + "§cNutze: /private kick <spieler>");
            return;
        }

        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(BedWars.PREFIX + "§cSpieler nicht gefunden!");
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(BedWars.PREFIX + "§cDu kannst dich nicht selbst kicken!");
            return;
        }

        PrivateRound round = plugin.getPrivateRoundManager().getPlayerRound(player.getUniqueId());
        if (!round.getPlayers().contains(target.getUniqueId())) {
            player.sendMessage(BedWars.PREFIX + "§cDieser Spieler ist nicht in deiner Runde!");
            return;
        }

        round.removePlayer(target.getUniqueId());
        plugin.getPrivateRoundManager().removePlayerFromRound(target.getUniqueId());
        plugin.getLobbyManager().sendToLobby(target);

        godModePlayers.remove(target.getUniqueId());

        target.sendMessage(BedWars.PREFIX + "§cDu wurdest aus der privaten Runde gekickt!");
        player.sendMessage(BedWars.PREFIX + "§a" + target.getName() + " §7wurde aus der Runde gekickt!");
        round.broadcastMessage("§c" + target.getName() + " §7wurde aus der Runde gekickt.");
    }

    private void sendHelp(Player player) {
        player.sendMessage("§8§m=======§r §6§lPrivate Runde §8§m=======§r");
        player.sendMessage("§e/private create <map> §8- §7Private Runde erstellen");
        player.sendMessage("§e/private invite <spieler> §8- §7Spieler einladen");
        player.sendMessage("§e/private join <ersteller> §8- §7Runde beitreten");
        player.sendMessage("§e/private leave §8- §7Runde verlassen");
        player.sendMessage("§e/private start §8- §7Runde starten");
        player.sendMessage("§e/private settings §8- §7Einstellungen anzeigen/ändern");
        player.sendMessage("§e/private info §8- §7Runden-Info anzeigen");
        player.sendMessage("§e/private delete §8- §7Runde löschen");
        player.sendMessage("§8§m================================");
        player.sendMessage("§d§lErsteller-Befehle §7(nur in eigener Runde):");
        player.sendMessage("§d/private fly §8- §7Flugmodus umschalten");
        player.sendMessage("§d/private gm <0-3> §8- §7Spielmodus ändern");
        player.sendMessage("§d/private god §8- §7Unverwundbarkeit umschalten");
        player.sendMessage("§d/private kick <spieler> §8- §7Spieler aus Runde kicken");
        player.sendMessage("§8§m================================");
    }
}
