package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.core.managers.RankManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildCommand implements CommandExecutor {

    private final BedWars plugin;

    public BuildCommand(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl nutzen!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bedwars.build")) {
            RankManager.RankData builder = plugin.getCore().getRankManager().getRankData("homemc.builder");
            String builderPrefix = builder.prefix;
            String builderColor = builder.color;
            String builderName = builder.displayName;
            String builderFormatted = builderColor + builderName;
            player.sendMessage("§8§m=======§r §c§lZugriff verweigert §8§m=======§r");
            player.sendMessage("§3Um diesen Befehl auszuführen, benötigst du:");
            player.sendMessage(" §8• §2Rang: " + builderFormatted);
            player.sendMessage("§8§m====================================");
            return true;
        }

        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());
        if (arena != null && arena.getGameState() == me.leon.bedwars.game.GameState.INGAME) {
            player.sendMessage("§cDu kannst den Build-Modus nicht während einer laufenden Runde nutzen!");
            return true;
        }

        plugin.getBuildManager().toggleBuildMode(player);

        return true;
    }
}