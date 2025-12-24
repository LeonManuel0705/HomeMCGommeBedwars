package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardManager {

    private final BedWars plugin;

    public ScoreboardManager(BedWars plugin) {
        this.plugin = plugin;
    }

    public void createGameScoreboard(Arena arena) {
        for (java.util.UUID uuid : arena.getPlayers()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                updatePlayerScoreboard(player, arena);
            }
        }
    }

    public void updateScoreboard(Arena arena) {
        for (java.util.UUID uuid : arena.getPlayers()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                updatePlayerScoreboard(player, arena);
            }
        }
    }

    private void updatePlayerScoreboard(Player player, Arena arena) {
        Scoreboard board = plugin.getServer().getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("bedwars", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName("§f§lHOMEMC");

        int line = 15;
        obj.getScore("§7" + getCurrentDate()).setScore(line--); // Datum oben
        obj.getScore("§f").setScore(line--);

        if (arena.isGoldEnabled()) {
            obj.getScore("§fGold: §aJa").setScore(line--);
        } else {
            obj.getScore("§fGold: §cNein").setScore(line--);
        }

        obj.getScore(" §f").setScore(line--);

        // Map-Name
        obj.getScore("§fMap: §e" + arena.getDisplayName()).setScore(line--);

        obj.getScore("  §f").setScore(line--);

        for (BedWarsTeam team : arena.getTeams().values()) {
            if (team.getPlayers().isEmpty()) continue;

            String status = team.isBedAlive() ? "§a✓" : "§c✗";
            String teamLine = status + " " + team.getColor().getColorCode() +
                    team.getColor().getDisplayName();
            obj.getScore(teamLine).setScore(line--);
        }

        player.setScoreboard(board);
    }

    private String getCurrentDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy");
        return sdf.format(new java.util.Date());
    }

    public void removeScoreboard(Player player) {
        player.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
    }
}