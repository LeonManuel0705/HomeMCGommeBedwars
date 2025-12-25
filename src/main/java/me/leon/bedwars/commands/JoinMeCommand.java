package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import me.leon.core.managers.RankManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinMeCommand implements CommandExecutor {

    private final BedWars plugin;

    public JoinMeCommand(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl nutzen!");
            return true;
        }

        Player player = (Player) sender;

        // Check permission
        if (!player.hasPermission("bedwars.joinme")) {
            RankManager.RankData vip = plugin.getCore().getRankManager().getRankData("homemc.vip");
            String vipColor = vip != null ? vip.color : "§a";
            String vipName = vip != null ? vip.displayName : "VIP";
            String vipFormatted = vipColor + vipName;
            player.sendMessage("§8§m=======§r §c§lZugriff verweigert §8§m=======§r");
            player.sendMessage("§3Um diesen Befehl zu nutzen, benötigst du:");
            player.sendMessage(" §8• §2Rang: " + vipFormatted + " §7oder höher");
            player.sendMessage("§8§m====================================");
            return true;
        }

        // Check if player is in an arena
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null) {
            player.sendMessage(BedWars.PREFIX + "§cDu musst in einer Runde sein!");
            return true;
        }

        // Only allow in waiting or starting state
        if (arena.getGameState() != GameState.WAITING && arena.getGameState() != GameState.STARTING) {
            player.sendMessage(BedWars.PREFIX + "§cJoinMe kann nur in der Wartelobby gesendet werden!");
            return true;
        }

        // Get server name from config
        String serverName = plugin.getConfig().getString("server-name", "bedwars1");
        String gameMode = "BedWars " + arena.getGameMode().getDisplayName();
        String arenaName = arena.getDisplayName();
        int currentPlayers = arena.getPlayers().size();
        int maxPlayers = arena.getMaxPlayers();

        // Send JoinMe through Core plugin
        plugin.getCore().getJoinMeManager().sendJoinMe(
                player,
                serverName,
                gameMode,
                arenaName,
                currentPlayers,
                maxPlayers
        );

        return true;
    }
}
