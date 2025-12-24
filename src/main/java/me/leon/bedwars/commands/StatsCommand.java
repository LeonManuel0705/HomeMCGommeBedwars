package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.managers.StatsManager.PlayerStats;
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
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl nutzen!");
            return true;
        }

        Player player = (Player) sender;
        PlayerStats stats = plugin.getStatsManager().getStats(player.getUniqueId());

        if (stats == null) {
            player.sendMessage("§cDeine Stats konnten nicht geladen werden!");
            return true;
        }

        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§6§lDeine BedWars Stats:");
        player.sendMessage("§f");
        player.sendMessage("§eKills: §7" + stats.kills);
        player.sendMessage("§eDeaths: §7" + stats.deaths);
        player.sendMessage("§eK/D: §7" + String.format("%.2f", stats.getKD()));
        player.sendMessage("§f");
        player.sendMessage("§aWins: §7" + stats.wins);
        player.sendMessage("§cLosses: §7" + stats.losses);
        player.sendMessage("§eWinrate: §7" + String.format("%.1f", stats.getWinRate()) + "%");
        player.sendMessage("§f");
        player.sendMessage("§dBetten zerstört: §7" + stats.bedsDestroyed);
        player.sendMessage("§eSpiele gespielt: §7" + stats.gamesPlayed);
        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━");

        return true;
    }
}