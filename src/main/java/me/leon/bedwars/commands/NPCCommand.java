package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.lobby.LobbyManager;
import me.leon.bedwars.managers.NPCManager;
import me.leon.core.managers.RankManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class NPCCommand implements CommandExecutor {

    private final BedWars plugin;

    public NPCCommand(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl nutzen!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bedwars.admin")) {
            RankManager.RankData admin = plugin.getCore().getRankManager().getRankData("homemc.admin");
            String adminColor = admin.color;
            String adminName = admin.displayName;
            String adminFormatted = adminColor + adminName;
            player.sendMessage("§8§m=======§r §c§lZugriff verweigert §8§m=======§r");
            player.sendMessage("§3Um diesen Befehl zu nutzen, benötigst du:");
            player.sendMessage(" §8• §2Rang: " + adminFormatted);
            player.sendMessage("§8§m====================================");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "spawn":
            case "create":
                handleSpawn(player, args);
                break;
            case "remove":
            case "delete":
                handleRemove(player);
                break;
            case "list":
                handleList(player);
                break;
            case "respawn":
                handleRespawn(player);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void handleSpawn(Player player, String[] args) {
        String displayName = "§6§lBedWars";
        String npcType = "TEAM";

        if (args.length >= 2) {
            npcType = args[1].toUpperCase();
        }

        if (args.length >= 3) {
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }
            displayName = sb.toString().trim().replace("&", "§");
        } else {
            switch (npcType) {
                case "SOLO":
                    displayName = "§6§lBedWars §e(1x8)";
                    break;
                case "DUO":
                    displayName = "§6§lBedWars §e(2x4)";
                    break;
                case "TEAM":
                    displayName = "§6§lBedWars §e(4x2)";
                    break;
                case "PRIVATE":
                    displayName = "§d§lPrivate Runde";
                    break;
                default:
                    player.sendMessage(BedWars.PREFIX + "§cUngültiger Typ! Verfügbar: SOLO, DUO, TEAM, PRIVATE");
                    return;
            }
        }

        if (npcType.equals("PRIVATE")) {
            plugin.getNPCManager().spawnPrivateNPC(player.getLocation(), displayName);
        } else {
            try {
                LobbyManager.GameMode gameMode = LobbyManager.GameMode.valueOf(npcType);
                plugin.getNPCManager().spawnNPC(player.getLocation(), gameMode, displayName);
            } catch (IllegalArgumentException e) {
                player.sendMessage(BedWars.PREFIX + "§cUngültiger Typ! Verfügbar: SOLO, DUO, TEAM, PRIVATE");
                return;
            }
        }

        player.sendMessage(BedWars.PREFIX + "§aNPC für §e" + npcType + " §agespawnt!");
    }

    private void handleRemove(Player player) {
        plugin.getNPCManager().removeNearestNPC(player.getLocation(), 5.0);
        player.sendMessage(BedWars.PREFIX + "§aNächster NPC entfernt!");
    }

    private void handleList(Player player) {
        player.sendMessage("§8§m=======§r §6§lBedWars NPCs §8§m=======§r");

        Map<Location, NPCManager.NPCData> npcs = plugin.getNPCManager().getNPCs();
        if (npcs.isEmpty()) {
            player.sendMessage("§7Keine NPCs vorhanden.");
        } else {
            for (Map.Entry<Location, NPCManager.NPCData> entry : npcs.entrySet()) {
                NPCManager.NPCData data = entry.getValue();
                player.sendMessage("§8- §e" + data.getDisplayName() + " §7(" + data.getNpcType() + ")");
                player.sendMessage("  §7Welt: §f" + data.getLocation().getWorld().getName());
                player.sendMessage("  §7Position: §f" + (int) data.getLocation().getX() + ", " +
                        (int) data.getLocation().getY() + ", " + (int) data.getLocation().getZ());
            }
        }

        player.sendMessage("§8§m================================");
    }

    private void handleRespawn(Player player) {
        plugin.getNPCManager().respawnAllNPCs();
        player.sendMessage(BedWars.PREFIX + "§aAlle NPCs wurden neu gespawnt!");
    }

    private void sendHelp(Player player) {
        player.sendMessage("§8§m=======§r §6§lNPC Befehle §8§m=======§r");
        player.sendMessage("§e/bwnpc spawn <Typ> [Name] §8- §7NPC spawnen");
        player.sendMessage("§e/bwnpc remove §8- §7Nächsten NPC entfernen");
        player.sendMessage("§e/bwnpc list §8- §7Alle NPCs anzeigen");
        player.sendMessage("§e/bwnpc respawn §8- §7Alle NPCs neu spawnen");
        player.sendMessage("§8§m==============================");
        player.sendMessage("§7Typen: §eSOLO§7, §eDUO§7, §eTEAM§7, §dPRIVATE");
    }
}
