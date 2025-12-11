package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.core.utils.CoreAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {

    private final BedWars plugin;
    private final Map<UUID, PlayerStats> statsCache;

    public StatsManager(BedWars plugin) {
        this.plugin = plugin;
        this.statsCache = new HashMap<>();
    }

    public void loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement ps = plugin.getMySQL().getConnection().prepareStatement(
                        "SELECT * FROM bw_stats WHERE uuid = ?"
                );
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();

                PlayerStats stats;
                if (rs.next()) {
                    stats = new PlayerStats(
                            uuid,
                            rs.getString("name"),
                            rs.getInt("kills"),
                            rs.getInt("deaths"),
                            rs.getInt("wins"),
                            rs.getInt("losses"),
                            rs.getInt("beds_destroyed"),
                            rs.getInt("games_played"),
                            rs.getInt("coins"),
                            rs.getInt("current_winstreak"),
                            rs.getInt("highest_winstreak")
                    );
                } else {
                    stats = new PlayerStats(uuid, player.getName());
                    createPlayer(stats);
                }

                statsCache.put(uuid, stats);
                rs.close();
                ps.close();

            } catch (Exception e) {
                plugin.getLogger().severe("Fehler beim Laden der Stats von " + player.getName());
                e.printStackTrace();
            }
        });
    }

    private void createPlayer(PlayerStats stats) {
        try {
            PreparedStatement ps = plugin.getMySQL().getConnection().prepareStatement(
                    "INSERT INTO bw_stats (uuid, name) VALUES (?, ?)"
            );
            ps.setString(1, stats.uuid.toString());
            ps.setString(2, stats.name);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateStats(UUID uuid, String name, int kills, int deaths,
                            int wins, int losses, int bedsDestroyed) {

        PlayerStats stats = statsCache.get(uuid);
        if (stats == null) {
            stats = new PlayerStats(uuid, name);
            statsCache.put(uuid, stats);
        }

        stats.kills += kills;
        stats.deaths += deaths;
        stats.wins += wins;
        stats.losses += losses;
        stats.bedsDestroyed += bedsDestroyed;
        stats.gamesPlayed++;

        if (wins > 0) {
            stats.currentWinstreak++;
            if (stats.currentWinstreak > stats.highestWinstreak) {
                stats.highestWinstreak = stats.currentWinstreak;
            }
        } else if (losses > 0) {
            stats.currentWinstreak = 0;
        }

        saveStats(stats);
    }

    private void saveStats(PlayerStats stats) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement ps = plugin.getMySQL().getConnection().prepareStatement(
                        "UPDATE bw_stats SET name = ?, kills = ?, deaths = ?, wins = ?, " +
                                "losses = ?, beds_destroyed = ?, games_played = ?, coins = ?, " +
                                "current_winstreak = ?, highest_winstreak = ? WHERE uuid = ?"
                );
                ps.setString(1, stats.name);
                ps.setInt(2, stats.kills);
                ps.setInt(3, stats.deaths);
                ps.setInt(4, stats.wins);
                ps.setInt(5, stats.losses);
                ps.setInt(6, stats.bedsDestroyed);
                ps.setInt(7, stats.gamesPlayed);
                ps.setInt(8, stats.coins);
                ps.setInt(9, stats.currentWinstreak);
                ps.setInt(10, stats.highestWinstreak);
                ps.setString(11, stats.uuid.toString());
                ps.executeUpdate();
                ps.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void addCoins(UUID uuid, int amount) {
        if (CoreAPI.isCoreAvailable()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    me.leon.core.Core core = CoreAPI.getCore();
                    if (core != null && core.getCoinManager() != null) {
                        core.getCoinManager().addCoins(uuid, amount);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Coins konnten nicht hinzugefügt werden: " + e.getMessage());
                }
            });
        }

        PlayerStats stats = statsCache.get(uuid);
        if (stats != null) {
            stats.coins += amount;
        }
    }

    public PlayerStats getStats(UUID uuid) {
        return statsCache.get(uuid);
    }

    public void unloadPlayer(UUID uuid) {
        statsCache.remove(uuid);
    }

    public static class PlayerStats {
        public UUID uuid;
        public String name;
        public int kills;
        public int deaths;
        public int wins;
        public int losses;
        public int bedsDestroyed;
        public int gamesPlayed;
        public int coins;
        public int currentWinstreak;
        public int highestWinstreak;

        public PlayerStats(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
            this.kills = 0;
            this.deaths = 0;
            this.wins = 0;
            this.losses = 0;
            this.bedsDestroyed = 0;
            this.gamesPlayed = 0;
            this.coins = 0;
            this.currentWinstreak = 0;
            this.highestWinstreak = 0;
        }

        public PlayerStats(UUID uuid, String name, int kills, int deaths,
                           int wins, int losses, int bedsDestroyed, int gamesPlayed,
                           int coins, int currentWinstreak, int highestWinstreak) {
            this.uuid = uuid;
            this.name = name;
            this.kills = kills;
            this.deaths = deaths;
            this.wins = wins;
            this.losses = losses;
            this.bedsDestroyed = bedsDestroyed;
            this.gamesPlayed = gamesPlayed;
            this.coins = coins;
            this.currentWinstreak = currentWinstreak;
            this.highestWinstreak = highestWinstreak;
        }

        public double getKD() {
            if (deaths == 0) return kills;
            return (double) kills / deaths;
        }

        public double getWinrate() {
            if (gamesPlayed == 0) return 0;
            return ((double) wins / gamesPlayed) * 100;
        }
    }
}