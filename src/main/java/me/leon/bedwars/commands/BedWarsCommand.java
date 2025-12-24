package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
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
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl nutzen!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "join":
                if (args.length < 2) {
                    player.sendMessage("§cBitte gib eine Arena an! /join <arena>");
                    return true;
                }
                plugin.getGameManager().joinArena(player, args[1]);
                break;

            case "list":
                player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━");
                player.sendMessage("§6§lVerfügbare Arenas:");
                for (Arena arena : plugin.getArenaManager().getArenas()) {
                    player.sendMessage("§e" + arena.getName() + " §8- §e" +
                            arena.getPlayers().size() + "§8/§c" + arena.getMaxPlayers() +
                            " §8- §7" + arena.getGameState());
                }
                player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━");
                break;

            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§6§lBedWars Befehle:");
        player.sendMessage("§e/join <arena> §8- §7Arena beitreten");
        player.sendMessage("§e/bw list §8- §7Arenas anzeigen");
        player.sendMessage("§e/leave §8- §7Arena verlassen");
        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}