package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StatsManager {

    private final BedWars plugin;
    private final ConcurrentHashMap<UUID, PlayerStats> statsCache;

    public StatsManager(BedWars plugin) {
        this.plugin = plugin;
        this.statsCache = new ConcurrentHashMap<>();
    }

    public void loadStats(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement ps = plugin.getCore().getMySQL().getConnection().prepareStatement(
                        "SELECT * FROM bedwars_stats WHERE uuid = ?"
                );
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    PlayerStats stats = new PlayerStats(
                            rs.getInt("kills"),
                            rs.getInt("deaths"),
                            rs.getInt("wins"),
                            rs.getInt("losses"),
                            rs.getInt("beds_destroyed"),
                            rs.getInt("games_played")
                    );
                    statsCache.put(uuid, stats);
                } else {
                    statsCache.put(uuid, new PlayerStats(0, 0, 0, 0, 0, 0));
                    createStats(uuid);
                }

                rs.close();
                ps.close();

            } catch (Exception e) {
                plugin.getLogger().severe("Fehler beim Laden der Stats für " + uuid);
                e.printStackTrace();
            }
        });
    }

    private void createStats(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement ps = plugin.getCore().getMySQL().getConnection().prepareStatement(
                        "INSERT INTO bedwars_stats (uuid, kills, deaths, wins, losses, beds_destroyed, games_played) " +
                                "VALUES (?, 0, 0, 0, 0, 0, 0)"
                );
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
                ps.close();

            } catch (Exception e) {
                plugin.getLogger().severe("Fehler beim Erstellen der Stats für " + uuid);
                e.printStackTrace();
            }
        });
    }

    public void updateStats(UUID uuid, boolean won) {
        PlayerStats stats = statsCache.get(uuid);
        if (stats == null) return;

        if (won) {
            stats.wins++;
        } else {
            stats.losses++;
        }
        stats.gamesPlayed++;

        saveStats(uuid);
    }

    public void addKill(UUID uuid) {
        PlayerStats stats = statsCache.get(uuid);
        if (stats != null) {
            stats.kills++;
            saveStats(uuid);
        }
    }

    public void addDeath(UUID uuid) {
        PlayerStats stats = statsCache.get(uuid);
        if (stats != null) {
            stats.deaths++;
            saveStats(uuid);
        }
    }

    public void addBedDestroy(UUID uuid) {
        PlayerStats stats = statsCache.get(uuid);
        if (stats != null) {
            stats.bedsDestroyed++;
            saveStats(uuid);
        }
    }

    private void saveStats(UUID uuid) {
        PlayerStats stats = statsCache.get(uuid);
        if (stats == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement ps = plugin.getCore().getMySQL().getConnection().prepareStatement(
                        "UPDATE bedwars_stats SET kills = ?, deaths = ?, wins = ?, losses = ?, " +
                                "beds_destroyed = ?, games_played = ? WHERE uuid = ?"
                );
                ps.setInt(1, stats.kills);
                ps.setInt(2, stats.deaths);
                ps.setInt(3, stats.wins);
                ps.setInt(4, stats.losses);
                ps.setInt(5, stats.bedsDestroyed);
                ps.setInt(6, stats.gamesPlayed);
                ps.setString(7, uuid.toString());
                ps.executeUpdate();
                ps.close();

            } catch (Exception e) {
                plugin.getLogger().severe("Fehler beim Speichern der Stats für " + uuid);
                e.printStackTrace();
            }
        });
    }

    public PlayerStats getStats(UUID uuid) {
        return statsCache.get(uuid);
    }

    public void unloadStats(UUID uuid) {
        statsCache.remove(uuid);
    }

    public static class PlayerStats {
        public int kills;
        public int deaths;
        public int wins;
        public int losses;
        public int bedsDestroyed;
        public int gamesPlayed;

        public PlayerStats(int kills, int deaths, int wins, int losses, int bedsDestroyed, int gamesPlayed) {
            this.kills = kills;
            this.deaths = deaths;
            this.wins = wins;
            this.losses = losses;
            this.bedsDestroyed = bedsDestroyed;
            this.gamesPlayed = gamesPlayed;
        }

        public double getKD() {
            return deaths == 0 ? kills : (double) kills / deaths;
        }

        public double getWinRate() {
            return gamesPlayed == 0 ? 0 : ((double) wins / gamesPlayed) * 100;
        }
    }
}