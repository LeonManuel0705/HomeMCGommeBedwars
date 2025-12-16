package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShoutCommand implements CommandExecutor {

    private final BedWars plugin;

    public ShoutCommand(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl verwenden!");
            return true;
        }

        Player player = (Player) sender;

        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game == null) {
            player.sendMessage(BedWars.PREFIX + "§cDu bist in keinem Spiel!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(BedWars.PREFIX + "§cNutzung: /shout <Nachricht>");
            return true;
        }

        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(arg).append(" ");
        }

        game.broadcast("§7[SHOUT] §r" + plugin.getRankManager().getRankColor(player) + player.getName() + "§7: §f" + plugin.getRankManager().getRankColor(player) + message.toString().trim());

        return true;
    }
}