package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
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
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl nutzen!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bedwars.start")) {
            RankManager.RankData yt = plugin.getCore().getRankManager().getRankData("homemc.yt");
            String ytPrefix = yt.prefix;
            String ytColor = yt.color;
            String ytName = yt.displayName;
            String ytFormatted = ytColor + ytName;
            player.sendMessage("§8§m=======§r §c§lZugriff verweigert §8§m=======§r");
            player.sendMessage("§3Um das Spiel frühzeitig zu starten, benötigst du:");
            player.sendMessage(" §8• §2Rang: " + ytFormatted);
            player.sendMessage("§8§m====================================");
            return true;
        }

        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());

        if (arena == null) {
            player.sendMessage("§cDu bist in keiner Arena!");
            return true;
        }

        if (arena.getGameState() != GameState.STARTING && arena.getGameState() != GameState.WAITING) {
            player.sendMessage("§cDas Spiel läuft bereits oder ist beendet!");
            return true;
        }

        arena.broadcastMessage("§aDer Countdown wurde reduziert!");
        arena.forcestart();

        return true;
    }
}