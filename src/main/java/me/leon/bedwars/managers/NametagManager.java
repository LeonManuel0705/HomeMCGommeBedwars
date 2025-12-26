package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
import me.leon.bedwars.managers.TeamManager.TeamColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class NametagManager {

    private final BedWars plugin;

    public NametagManager(BedWars plugin) {
        this.plugin = plugin;
    }

    public void setGameNametag(Player player, BedWarsTeam bedwarsTeam) {
        String teamColor = bedwarsTeam.getColor().getColorCode();
        String teamName = getFullTeamName(bedwarsTeam.getColor());

        String displayName = getDisplayName(player);
        String prefix = teamColor + teamName + " §7| " + teamColor;

        String tabName = teamColor + displayName;
        player.setPlayerListName(tabName);

        for (Player online : Bukkit.getOnlinePlayers()) {
            updateScoreboardTeamFor(online, player, bedwarsTeam.getColor(), prefix, displayName);
        }

        plugin.getLogger().info("[Nametag] Set for " + displayName + " in team " + teamName);
    }

    private String getDisplayName(Player player) {
        return plugin.getDisplayName(player);
    }

    private void updateScoreboardTeamFor(Player viewer, Player target, TeamColor teamColor, String prefix, String displayName) {
        Scoreboard board = plugin.getScoreboardManager().getOrCreateScoreboard(viewer);

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
            case AQUA: return "0_cyan";
            case BLUE: return "1_blau";
            case GRAY: return "2_grau";
            case GREEN: return "3_gruen";
            case PINK: return "4_pink";
            case RED: return "5_rot";
            case WHITE: return "6_weiss";
            case YELLOW: return "7_gelb";
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
        String displayName = getDisplayName(player);

        for (Player online : Bukkit.getOnlinePlayers()) {
            Scoreboard board = plugin.getScoreboardManager().getOrCreateScoreboard(online);

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

        player.setPlayerListName("§7[SPEC] " + displayName);
    }

    public void hideNametag(Player player) {
        player.setPlayerListName("§7" + getDisplayName(player));

        for (Player online : Bukkit.getOnlinePlayers()) {
            Scoreboard board = plugin.getScoreboardManager().getOrCreateScoreboard(online);

            String teamName = "z_invis";
            Team sbTeam = board.getTeam(teamName);

            if (sbTeam == null) {
                sbTeam = board.registerNewTeam(teamName);
            }

            sbTeam.setPrefix("");
            sbTeam.setSuffix("");

            for (Team existingTeam : board.getTeams()) {
                if (existingTeam.hasEntry(player.getName()) && !existingTeam.getName().equals(teamName)) {
                    existingTeam.removeEntry(player.getName());
                }
            }

            if (!sbTeam.hasEntry(player.getName())) {
                sbTeam.addEntry(player.getName());
            }
        }
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