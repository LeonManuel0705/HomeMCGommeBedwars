package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.lobby.LobbyManager;
import me.leon.bedwars.managers.NPCManager;
import me.leon.bedwars.npc.PlayerNPC;
import me.leon.core.managers.RankManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

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
            case "player":
                handlePlayerNPC(player, args);
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
                    displayName = "§5§lP§d§lr§5§li§d§lv§5§la§d§lt§5§le §d§lR§5§lu§d§ln§5§ld§d§le";
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

    private void handlePlayerNPC(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§c/bwnpc player <Typ> <Skin> [Name]");
            player.sendMessage("§7Typen: SOLO, DUO, TEAM, PRIVATE");
            player.sendMessage("§7Skin: Minecraft-Spielername für die Skin");
            return;
        }

        String npcType = args[1].toUpperCase();
        String skinName = args[2];
        String displayName;

        if (args.length >= 4) {
            StringBuilder sb = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
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
                    displayName = "§5§lP§d§lr§5§li§d§lv§5§la§d§lt§5§le §d§lR§5§lu§d§ln§5§ld§d§le";
                    break;
                default:
                    player.sendMessage(BedWars.PREFIX + "§cUngültiger Typ! Verfügbar: SOLO, DUO, TEAM, PRIVATE");
                    return;
            }
        }

        if (!npcType.equals("PRIVATE")) {
            try {
                LobbyManager.GameMode.valueOf(npcType);
            } catch (IllegalArgumentException e) {
                player.sendMessage(BedWars.PREFIX + "§cUngültiger Typ! Verfügbar: SOLO, DUO, TEAM, PRIVATE");
                return;
            }
        }

        player.sendMessage(BedWars.PREFIX + "§eSpieler-NPC wird erstellt, Skin wird geladen...");

        plugin.getPlayerNPCManager().createNPC(player.getLocation(), displayName, skinName, npcType);
        player.sendMessage(BedWars.PREFIX + "§aSpieler-NPC für §e" + npcType + " §amit Skin von §e" + skinName + " §aerstellt!");
    }

    private void handleRemove(Player player) {
        plugin.getNPCManager().removeNearestNPC(player.getLocation(), 5.0);
        plugin.getPlayerNPCManager().removeNearestNPC(player.getLocation(), 5.0);
        player.sendMessage(BedWars.PREFIX + "§aNächster NPC entfernt!");
    }

    private void handleList(Player player) {
        player.sendMessage("§8§m=======§r §6§lBedWars NPCs §8§m=======§r");

        Map<Location, NPCManager.NPCData> villagerNpcs = plugin.getNPCManager().getNPCs();
        if (!villagerNpcs.isEmpty()) {
            player.sendMessage("§e§lVillager NPCs:");
            for (Map.Entry<Location, NPCManager.NPCData> entry : villagerNpcs.entrySet()) {
                NPCManager.NPCData data = entry.getValue();
                player.sendMessage("§8- §e" + data.getDisplayName() + " §7(" + data.getNpcType() + ")");
                player.sendMessage("  §7Welt: §f" + data.getLocation().getWorld().getName());
                player.sendMessage("  §7Position: §f" + (int) data.getLocation().getX() + ", " +
                        (int) data.getLocation().getY() + ", " + (int) data.getLocation().getZ());
            }
        }

        Map<UUID, PlayerNPC> playerNpcs = plugin.getPlayerNPCManager().getNPCs();
        if (!playerNpcs.isEmpty()) {
            player.sendMessage("§b§lSpieler NPCs:");
            for (PlayerNPC npc : playerNpcs.values()) {
                player.sendMessage("§8- §b" + npc.getName() + " §7(" + npc.getNpcType() + ")");
                player.sendMessage("  §7Welt: §f" + npc.getLocation().getWorld().getName());
                player.sendMessage("  §7Position: §f" + (int) npc.getLocation().getX() + ", " +
                        (int) npc.getLocation().getY() + ", " + (int) npc.getLocation().getZ());
            }
        }

        if (villagerNpcs.isEmpty() && playerNpcs.isEmpty()) {
            player.sendMessage("§7Keine NPCs vorhanden.");
        }

        player.sendMessage("§8§m================================");
    }

    private void handleRespawn(Player player) {
        plugin.getNPCManager().respawnAllNPCs();
        plugin.getPlayerNPCManager().respawnAllNPCs();
        player.sendMessage(BedWars.PREFIX + "§aAlle NPCs wurden neu gespawnt!");
    }

    private void sendHelp(Player player) {
        player.sendMessage("§8§m=======§r §6§lNPC Befehle §8§m=======§r");
        player.sendMessage("§e/bwnpc spawn <Typ> [Name] §8- §7Villager NPC spawnen");
        player.sendMessage("§b/bwnpc player <Typ> <Skin> [Name] §8- §7Spieler NPC mit Skin");
        player.sendMessage("§e/bwnpc remove §8- §7Nächsten NPC entfernen");
        player.sendMessage("§e/bwnpc list §8- §7Alle NPCs anzeigen");
        player.sendMessage("§e/bwnpc respawn §8- §7Alle NPCs neu spawnen");
        player.sendMessage("§8§m==============================");
        player.sendMessage("§7Typen: §eSOLO§7, §eDUO§7, §eTEAM§7, §dPRIVATE");
    }
}
