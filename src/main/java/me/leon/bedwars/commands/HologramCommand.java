package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.managers.HologramManager;
import me.leon.core.managers.RankManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HologramCommand implements CommandExecutor {

    private final BedWars plugin;

    public HologramCommand(BedWars plugin) {
        this.plugin = plugin;
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

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                return handleCreate(player, args);

            case "remove":
                return handleRemove(player, args);

            case "list":
                return handleList(player);

            case "help":
                sendHelp(player);
                return true;

            default:
                player.sendMessage(BedWars.PREFIX + "§cUnbekannter Unterbefehel! Nutze §6/hologram help");
                return true;
        }
    }

    private boolean handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(BedWars.PREFIX + "§cNutzung: §6/hologram create <id> <typ>");
            player.sendMessage("§7Typen: TOP_KILLS, TOP_WINS, TOP_BEDS, TOP_WINSTREAK, STATS");
            return true;
        }

        String id = args[1];
        String typeStr = args[2].toUpperCase();

        HologramManager.HologramType type;
        try {
            type = HologramManager.HologramType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage(BedWars.PREFIX + "§cUngültiger Typ! §7Verfügbare Typen:");
            player.sendMessage("§7  • TOP_KILLS");
            player.sendMessage("§7  • TOP_WINS");
            player.sendMessage("§7  • TOP_BEDS");
            player.sendMessage("§7  • TOP_WINSTREAK");
            player.sendMessage("§7  • STATS");
            return true;
        }

        plugin.getHologramManager().createHologram(id, player.getLocation(), type);
        player.sendMessage(BedWars.PREFIX + "§aHologram §6" + id + " §avom Typ §6" + typeStr + " §aerstellt!");
        player.sendMessage("§7Position: §e" + player.getLocation().getBlockX() + " " +
                player.getLocation().getBlockY() + " " + player.getLocation().getBlockZ());

        return true;
    }

    private boolean handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(BedWars.PREFIX + "§cNutzung: §6/hologram remove <id>");
            return true;
        }

        String id = args[1];
        plugin.getHologramManager().removeHologram(id);
        player.sendMessage(BedWars.PREFIX + "§aHologram §6" + id + " §awurde gelöscht!");

        return true;
    }

    private boolean handleList(Player player) {
        player.sendMessage(BedWars.PREFIX + "§7Verfügbare Hologramm-Typen:");
        player.sendMessage("§7  • §cTOP_KILLS §8- §7Zeige die Top 10 nach Kills");
        player.sendMessage("§7  • §aTOP_WINS §8- §7Zeige die Top 10 nach Siegen");
        player.sendMessage("§7  • §4TOP_BEDS §8- §7Zeige die Top 10 nach zerstörten Betten");
        player.sendMessage("§7  • §6TOP_WINSTREAK §8- §7Zeige die Top 10 Winstreaks");
        player.sendMessage("§7  • §5STATS §8- §7Zeige globale Statistiken");
        player.sendMessage("");
        player.sendMessage("§7Beispiel: §6/hologram create mein_holo TOP_KILLS");

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§8§m════════════════════════════════════════");
        player.sendMessage("§c§lHologram Befehle");
        player.sendMessage("§8§m════════════════════════════════════════");
        player.sendMessage("");
        player.sendMessage("§6/hologram create <id> <typ>");
        player.sendMessage("§7  Erstellt ein neues Hologram an deiner Position");
        player.sendMessage("");
        player.sendMessage("§6/hologram remove <id>");
        player.sendMessage("§7  Entfernt ein Hologram");
        player.sendMessage("");
        player.sendMessage("§6/hologram list");
        player.sendMessage("§7  Zeigt alle verfügbaren Hologram-Typen");
        player.sendMessage("");
        player.sendMessage("§8§m════════════════════════════════════════");
    }
}

