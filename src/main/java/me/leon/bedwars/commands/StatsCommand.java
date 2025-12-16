package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.managers.StatsManager;
import me.leon.core.Core;
import me.leon.core.utils.CoreAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    private final BedWars plugin;

    public StatsCommand(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl verwenden!");
            return true;
        }

        Player player = (Player) sender;
        StatsManager.PlayerStats stats = plugin.getStatsManager().getStats(player.getUniqueId());

        if (stats == null) {
            player.sendMessage(BedWars.PREFIX + "§cDeine Statistiken konnten nicht geladen werden!");
            return true;
        }

        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§c§lBedWars Statistiken");
        player.sendMessage("");
        player.sendMessage("§7Kills: §e" + stats.kills);
        player.sendMessage("§7Deaths: §e" + stats.deaths);
        player.sendMessage("§7K/D: §e" + String.format("%.2f", stats.getKD()));
        player.sendMessage("");
        player.sendMessage("§7Wins: §a" + stats.wins);
        player.sendMessage("§7Losses: §c" + stats.losses);
        player.sendMessage("§7Winrate: §e" + String.format("%.1f%%", stats.getWinrate()));
        player.sendMessage("");
        player.sendMessage("§7Betten zerstört: §e" + stats.bedsDestroyed);
        player.sendMessage("§7Spiele gespielt: §e" + stats.gamesPlayed);
        player.sendMessage("§7Current Winstreak: §e" + stats.currentWinstreak);
        player.sendMessage("§7Highest Winstreak: §e" + stats.highestWinstreak);

        if (CoreAPI.isCoreAvailable()) {
            try {
                int coins = CoreAPI.getCore().getCoinManager().getCoins(player.getUniqueId());
                player.sendMessage(Core.PREFIX + "§7Coins: §6§l" + coins);
            } catch (Exception e) {
            }
        }

        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        return true;
    }
}