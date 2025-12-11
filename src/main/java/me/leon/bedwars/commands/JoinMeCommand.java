package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.managers.JoinMeIntegration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinMeCommand implements CommandExecutor {

    private final BedWars plugin;
    private final JoinMeIntegration joinMeIntegration;

    public JoinMeCommand(BedWars plugin) {
        this.plugin = plugin;
        this.joinMeIntegration = new JoinMeIntegration(plugin);
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

        joinMeIntegration.sendJoinMe(player, game);

        return true;
    }
}