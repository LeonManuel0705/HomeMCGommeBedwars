package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
import me.leon.bedwars.managers.TeamManager.TeamColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

public class NametagManager {

    private final BedWars plugin;

    public NametagManager(BedWars plugin) {
        this.plugin = plugin;
    }

    public void setGameNametag(Player player, BedWarsTeam bedwarsTeam) {
        String teamColor = bedwarsTeam.getColor().getColorCode();
        String teamName = getFullTeamName(bedwarsTeam.getColor());

        String prefix = teamColor + teamName + " §7| " + teamColor;

        String tabName = teamColor + player.getName();
        player.setPlayerListName(tabName);

        for (Player online : Bukkit.getOnlinePlayers()) {
            updateScoreboardTeamFor(online, player, bedwarsTeam.getColor(), prefix);
        }

        plugin.getLogger().info("[Nametag] Set for " + player.getName() + " in team " + teamName);
    }

    private void updateScoreboardTeamFor(Player viewer, Player target, TeamColor teamColor, String prefix) {
        Scoreboard board = viewer.getScoreboard();

        if (board == null || board == Bukkit.getScoreboardManager().getMainScoreboard()) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            viewer.setScoreboard(board);
        }

        String scoreboardTeamName = getScoreboardTeamName(teamColor);

        Team sbTeam = board.getTeam(scoreboardTeamName);
        if (sbTeam == null) {
            sbTeam = board.registerNewTeam(scoreboardTeamName);
        }

        String finalPrefix = prefix;
        if (stripColors(finalPrefix).length() > 16) {
            finalPrefix = finalPrefix.substring(0, 16);
        }

        sbTeam.setPrefix(finalPrefix);
        sbTeam.setSuffix("");

        if (!sbTeam.hasEntry(target.getName())) {
            sbTeam.addEntry(target.getName());
        }
    }

    private String getScoreboardTeamName(TeamColor color) {
        switch (color) {
            case AQUA: return "0_cyan";      // A
            case BLUE: return "1_blau";      // B
            case GRAY: return "2_grau";      // G
            case GREEN: return "3_gruen";    // G
            case PINK: return "4_pink";      // P
            case RED: return "5_rot";        // R
            case WHITE: return "6_weiss";    // W
            case YELLOW: return "7_gelb";    // Y
            default: return "8_default";
        }
    }

    private String getFullTeamName(TeamColor color) {
        switch (color) {
            case RED: return "Rot";
            case BLUE: return "Blau";
            case GREEN: return "Grün";
            case YELLOW: return "Gelb";
            case AQUA: return "Cyan";
            case PINK: return "Pink";
            case GRAY: return "Grau";
            case WHITE: return "Weiß";
            default: return color.getDisplayName();
        }
    }

    public void setSpectatorNametag(Player player) {
        String prefix = "§7[SPEC] ";

        for (Player online : Bukkit.getOnlinePlayers()) {
            Scoreboard board = online.getScoreboard();

            if (board == null || board == Bukkit.getScoreboardManager().getMainScoreboard()) {
                board = Bukkit.getScoreboardManager().getNewScoreboard();
                online.setScoreboard(board);
            }

            String teamName = "9_spec";
            Team sbTeam = board.getTeam(teamName);

            if (sbTeam == null) {
                sbTeam = board.registerNewTeam(teamName);
            }

            sbTeam.setPrefix(prefix);
            sbTeam.setSuffix("");

            if (!sbTeam.hasEntry(player.getName())) {
                sbTeam.addEntry(player.getName());
            }
        }

        player.setPlayerListName("§7[SPEC] " + player.getName());
    }

    public void resetNametag(Player player) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            Scoreboard board = online.getScoreboard();
            if (board != null && board != Bukkit.getScoreboardManager().getMainScoreboard()) {
                for (Team team : board.getTeams()) {
                    if (team.hasEntry(player.getName())) {
                        team.removeEntry(player.getName());
                    }
                }
            }
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.getCore().getNickManager().updatePlayerDisplay(player);
        });
    }

    private String stripColors(String text) {
        if (text == null) return "";
        return text.replaceAll("§.", "");
    }
}