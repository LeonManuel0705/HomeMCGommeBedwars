package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements CommandExecutor {

    private final BedWars plugin;

    public LeaveCommand(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl verwenden!");
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.getGameManager().isInGame(player)) {
            player.sendMessage(BedWars.PREFIX + "§cDu bist in keinem Spiel!");
            return true;
        }

        plugin.getGameManager().leaveGame(player);
        plugin.getLobbyManager().sendToLobby(player);
        player.sendMessage(BedWars.PREFIX + "§aDu hast das Spiel verlassen!");

        return true;
    }
}