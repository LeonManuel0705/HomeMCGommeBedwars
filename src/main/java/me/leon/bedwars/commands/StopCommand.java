package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.core.managers.RankManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StopCommand implements CommandExecutor {

    private final BedWars plugin;

    public StopCommand(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl nutzen!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bedwars.stop")) {
            RankManager.RankData mod = plugin.getCore().getRankManager().getRankData("homemc.mod");
            String modPrefix = mod.prefix;
            String modColor = mod.color;
            String modName = mod.displayName;
            String modFormatted = modColor + modName;
            player.sendMessage("§8§m=======§r §c§lZugriff verweigert §8§m=======§r");
            player.sendMessage("§3Um diesen Befehl zu nutzen, benötigst du:");
            player.sendMessage(" §8• §2Rang: " + modFormatted);
            player.sendMessage("§8§m====================================");
            return true;
        }

        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());

        if (arena == null) {
            player.sendMessage("§cDu bist in keiner Arena!");
            return true;
        }

        arena.broadcastMessage("§c§lSPIEL ABGEBROCHEN!");
        arena.broadcastMessage("§7Das Spiel wurde von einem Teammitglied abgebrochen.");

        arena.forceEnd();

        return true;
    }
}