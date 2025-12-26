package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final BedWars plugin;
    private final Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();

    public ScoreboardManager(BedWars plugin) {
        this.plugin = plugin;
    }

    public Scoreboard getOrCreateScoreboard(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerScoreboards.containsKey(uuid)) {
            Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
            playerScoreboards.put(uuid, board);
            player.setScoreboard(board);
        }
        return playerScoreboards.get(uuid);
    }

    public void createGameScoreboard(Arena arena) {
        for (UUID uuid : arena.getPlayers()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                updatePlayerScoreboard(player, arena);
            }
        }
    }

    public void updateScoreboard(Arena arena) {
        for (UUID uuid : arena.getPlayers()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                updatePlayerScoreboard(player, arena);
            }
        }
    }

    private void updatePlayerScoreboard(Player player, Arena arena) {
        Scoreboard board = getOrCreateScoreboard(player);

        Objective obj = board.getObjective("bedwars");
        if (obj != null) {
            obj.unregister();
        }

        obj = board.registerNewObjective("bedwars", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName("§f§lHOMEMC");

        int line = 15;

        if (arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING) {
            obj.getScore("§8" + getCurrentDate()).setScore(line--);
            obj.getScore("§f").setScore(line--);

            obj.getScore("§fMap: §a" + arena.getDisplayName()).setScore(line--);
            obj.getScore("§f ").setScore(line--);

            obj.getScore("§fSpieler: §a" + arena.getPlayers().size() + "§7/" + arena.getMaxPlayers()).setScore(line--);
            obj.getScore("§f  ").setScore(line--);

            if (arena.getGameState() == GameState.STARTING) {
                obj.getScore("§fStart in: §e" + arena.getCountdown() + "s").setScore(line--);
            } else {
                obj.getScore("§fStatus: §eWarten...").setScore(line--);
            }
            obj.getScore("§f   ").setScore(line--);

            int goldYes = arena.getGoldVotesYes();
            int goldNo = arena.getGoldVotesNo();
            if (arena.isGoldEnabled()) {
                obj.getScore("§fGold: §aJa §7(" + goldYes + "/" + goldNo + ")").setScore(line--);
            } else {
                obj.getScore("§fGold: §cNein §7(" + goldYes + "/" + goldNo + ")").setScore(line--);
            }
            obj.getScore("§f    ").setScore(line--);

            obj.getScore("§eplay.homemc.org").setScore(line--);
        } else {
            obj.getScore("§8" + getCurrentDate()).setScore(line--);
            obj.getScore("§f").setScore(line--);

            obj.getScore("§fMap: §a" + arena.getDisplayName()).setScore(line--);
            obj.getScore("§f ").setScore(line--);

            if (arena.isGoldEnabled()) {
                obj.getScore("§fGold: §aJa").setScore(line--);
            } else {
                obj.getScore("§fGold: §cNein").setScore(line--);
            }
            obj.getScore("§f  ").setScore(line--);

            for (BedWarsTeam team : arena.getTeams().values()) {
                if (!team.hasHadPlayers()) continue;

                String bedStatus;
                if (team.isBedAlive()) {
                    bedStatus = "§a✔";
                } else if (team.getPlayers().isEmpty()) {
                    bedStatus = "§c✘";
                } else {
                    bedStatus = "§e" + team.getPlayers().size();
                }

                String teamLine = team.getColor().getColorCode() + team.getColor().getDisplayName() + ": " + bedStatus;
                obj.getScore(teamLine).setScore(line--);
            }

            obj.getScore("§f   ").setScore(line--);
            obj.getScore("§eplay.homemc.org").setScore(line--);
        }

        player.setScoreboard(board);
    }

    private String getCurrentDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy");
        return sdf.format(new java.util.Date());
    }

    public void removeScoreboard(Player player) {
        playerScoreboards.remove(player.getUniqueId());
        player.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
    }
}