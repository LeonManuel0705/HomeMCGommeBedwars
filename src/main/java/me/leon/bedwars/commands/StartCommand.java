package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.game.GameState;
import me.leon.core.managers.RankManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommand implements CommandExecutor {

    private final BedWars plugin;

    public StartCommand(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl verwenden!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bedwars.forcestart")) {
            RankManager.RankData yt = plugin.getRankManager().getRankData("homemc.yt");
            String ytPrefix = yt.prefix;
            String ytColor = yt.color;
            String ytName = yt.displayName;
            String ytFormatted = ytColor + ytName;
            player.sendMessage("§8§m=======§r §c§lZugriff verweigert §8§m=======§r");
            player.sendMessage("§3Um die Geschwindigkeit anderer zu ändern, benötigst du:");
            player.sendMessage(" §8• §2Rang: " + ytFormatted);
            player.sendMessage("§8§m====================================");
            return true;
        }

        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game == null) {
            player.sendMessage(BedWars.PREFIX + "§cDu bist in keinem Spiel!");
            return true;
        }

        if (game.getState() != GameState.LOBBY) {
            player.sendMessage(BedWars.PREFIX + "§cDas Spiel läuft bereits!");
            return true;
        }

        player.sendMessage(BedWars.PREFIX + "§aForcestarte das Spiel...");
        game.broadcast("§cDas Spiel wurde gestartet");

        return true;
    }
}