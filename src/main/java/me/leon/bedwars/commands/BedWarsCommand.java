package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.managers.MapData;
import me.leon.core.utils.CoreAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BedWarsCommand implements CommandExecutor {

    private final BedWars plugin;

    public BedWarsCommand(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(BedWars.PREFIX + "§cNur Spieler können diesen Befehl verwenden!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(BedWars.PREFIX + "§7Verfügbare Befehle:");
            player.sendMessage("§e/bw join [map] §7- Spiel beitreten");
            player.sendMessage("§e/bw leave §7- Spiel verlassen");
            player.sendMessage("§e/bw maps §7- Zeige alle Maps");
            player.sendMessage("§e/bw achievements §7- Zeige Achievements");
            player.sendMessage("§e/bw gadgets §7- Öffne Gadget Shop");
            player.sendMessage("§e/stats §7- Zeige Statistiken");
            return true;
        }

        if (args[0].equalsIgnoreCase("join")) {
            if (args.length < 2) {
                MapData randomMap = plugin.getMapManager().getRandomMap();
                if (randomMap == null) {
                    player.sendMessage(BedWars.PREFIX + "§cKeine Maps verfügbar!");
                    return true;
                }

                if (CoreAPI.isPartyManagerAvailable() && CoreAPI.isInParty(player.getUniqueId())) {
                    plugin.getPartyIntegration().joinGameWithParty(player, randomMap);
                } else {
                    Game game = plugin.getGameManager().getAvailableGame(randomMap);
                    plugin.getGameManager().joinGame(player, game);
                }
            } else {
                String mapName = args[1];
                MapData mapData = plugin.getMapManager().getMap(mapName);

                if (mapData == null) {
                    player.sendMessage(BedWars.PREFIX + "§cDiese Map existiert nicht!");
                    return true;
                }

                if (CoreAPI.isPartyManagerAvailable() && CoreAPI.isInParty(player.getUniqueId())) {
                    plugin.getPartyIntegration().joinGameWithParty(player, mapData);
                } else {
                    Game game = plugin.getGameManager().getAvailableGame(mapData);
                    plugin.getGameManager().joinGame(player, game);
                }
            }
        } else if (args[0].equalsIgnoreCase("leave")) {
            if (!plugin.getGameManager().isInGame(player)) {
                player.sendMessage(BedWars.PREFIX + "§cDu bist in keinem Spiel!");
                return true;
            }

            plugin.getGameManager().leaveGame(player);
            plugin.getLobbyManager().sendToLobby(player);
            player.sendMessage(BedWars.PREFIX + "§aDu hast das Spiel verlassen!");

        } else if (args[0].equalsIgnoreCase("maps")) {
            player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§c§lVerfügbare Maps");
            player.sendMessage("");

            for (MapData map : plugin.getMapManager().getAllMaps()) {
                player.sendMessage("  §8• §e" + map.getName() + " §7(" +
                        map.getMinPlayers() + "-" + map.getMaxPlayers() + " Spieler)");
            }

            player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━");

        } else if (args[0].equalsIgnoreCase("achievements")) {
            plugin.getAchievementManager().openAchievementsGUI(player);

        } else if (args[0].equalsIgnoreCase("gadgets")) {
            plugin.getGadgetManager().openGadgetShop(player);

        } else {
            player.sendMessage(BedWars.PREFIX + "§cUnbekannter Befehl! Verwende §e/bw §coder §e/bw help");
        }

        return true;
    }
}