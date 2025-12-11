package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HologramManager {

    private final BedWars plugin;
    private final Map<String, Hologram> holograms;
    private BukkitRunnable updateTask;

    public HologramManager(BedWars plugin) {
        this.plugin = plugin;
        this.holograms = new ConcurrentHashMap<>();

        loadHolograms();
        startUpdateTask();
    }

    private void loadHolograms() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement ps = plugin.getMySQL().getConnection().prepareStatement(
                        "SELECT * FROM bw_holograms"
                );
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    String id = rs.getString("id");
                    String type = rs.getString("type");
                    String world = rs.getString("world");
                    double x = rs.getDouble("x");
                    double y = rs.getDouble("y");
                    double z = rs.getDouble("z");

                    Location location = new Location(Bukkit.getWorld(world), x, y, z);

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        HologramType hologramType = HologramType.valueOf(type);
                        Hologram hologram = new Hologram(id, location, hologramType);
                        holograms.put(id, hologram);
                        hologram.spawn();
                    });
                }

                rs.close();
                ps.close();

                plugin.getLogger().info(holograms.size() + " Hologramme geladen!");

            } catch (Exception e) {
                plugin.getLogger().severe("Fehler beim Laden der Hologramme!");
                e.printStackTrace();
            }
        });
    }

    public void createHologram(String id, Location location, HologramType type) {
        Hologram hologram = new Hologram(id, location, type);
        holograms.put(id, hologram);
        hologram.spawn();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement ps = plugin.getMySQL().getConnection().prepareStatement(
                        "INSERT INTO bw_holograms (id, type, world, x, y, z) VALUES (?, ?, ?, ?, ?, ?)"
                );
                ps.setString(1, id);
                ps.setString(2, type.name());
                ps.setString(3, location.getWorld().getName());
                ps.setDouble(4, location.getX());
                ps.setDouble(5, location.getY());
                ps.setDouble(6, location.getZ());
                ps.executeUpdate();
                ps.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void removeHologram(String id) {
        Hologram hologram = holograms.remove(id);
        if (hologram != null) {
            hologram.remove();
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement ps = plugin.getMySQL().getConnection().prepareStatement(
                        "DELETE FROM bw_holograms WHERE id = ?"
                );
                ps.setString(1, id);
                ps.executeUpdate();
                ps.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void startUpdateTask() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Hologram hologram : holograms.values()) {
                    hologram.update();
                }
            }
        };
        updateTask.runTaskTimer(plugin, 20L, 20L);
    }

    public void shutdown() {
        if (updateTask != null) {
            updateTask.cancel();
        }

        for (Hologram hologram : holograms.values()) {
            hologram.remove();
        }
    }

    public class Hologram {
        private final String id;
        private final Location location;
        private final HologramType type;
        private final List<ArmorStand> lines;

        public Hologram(String id, Location location, HologramType type) {
            this.id = id;
            this.location = location;
            this.type = type;
            this.lines = new ArrayList<>();
        }

        public void spawn() {
            List<String> content = getContent();

            double yOffset = 0;
            for (int i = content.size() - 1; i >= 0; i--) {
                Location lineLoc = location.clone().add(0, yOffset, 0);
                ArmorStand stand = location.getWorld().spawn(lineLoc, ArmorStand.class);

                stand.setVisible(false);
                stand.setGravity(false);
                stand.setCustomNameVisible(true);
                stand.setCustomName(content.get(i));
                stand.setMarker(true);
                stand.setSmall(true);

                lines.add(stand);
                yOffset += 0.25;
            }
        }

        public void update() {
            List<String> content = getContent();

            for (int i = 0; i < Math.min(lines.size(), content.size()); i++) {
                lines.get(i).setCustomName(content.get(content.size() - 1 - i));
            }
        }

        public void remove() {
            for (ArmorStand stand : lines) {
                stand.remove();
            }
            lines.clear();
        }

        private List<String> getContent() {
            List<String> content = new ArrayList<>();

            switch (type) {
                case TOP_KILLS:
                    content.add("§6§l⚔ TOP KILLS ⚔");
                    content.add("");
                    content.addAll(getTopKills());
                    break;

                case TOP_WINS:
                    content.add("§a§l★ TOP WINS ★");
                    content.add("");
                    content.addAll(getTopWins());
                    break;

                case TOP_BEDS:
                    content.add("§c§l☠ TOP BETTEN ☠");
                    content.add("");
                    content.addAll(getTopBeds());
                    break;

                case TOP_WINSTREAK:
                    content.add("§e§l⚡ TOP WINSTREAK ⚡");
                    content.add("");
                    content.addAll(getTopWinstreak());
                    break;

                case STATS:
                    content.add("§c§lBED§f§lWARS");
                    content.add("§7Statistiken");
                    content.add("");
                    content.addAll(getGlobalStats());
                    break;
            }

            return content;
        }

        private List<String> getTopKills() {
            List<String> lines = new ArrayList<>();

            try {
                PreparedStatement ps = plugin.getMySQL().getConnection().prepareStatement(
                        "SELECT name, kills FROM bw_stats ORDER BY kills DESC LIMIT 10"
                );
                ResultSet rs = ps.executeQuery();

                int rank = 1;
                while (rs.next() && rank <= 10) {
                    String name = rs.getString("name");
                    int kills = rs.getInt("kills");

                    String medal = getMedal(rank);
                    lines.add(medal + " §e" + rank + ". §7" + name + " §8- §c" + kills);
                    rank++;
                }

                rs.close();
                ps.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (lines.isEmpty()) {
                lines.add("§7Keine Daten");
            }

            return lines;
        }

        private List<String> getTopWins() {
            List<String> lines = new ArrayList<>();

            try {
                PreparedStatement ps = plugin.getMySQL().getConnection().prepareStatement(
                        "SELECT name, wins FROM bw_stats ORDER BY wins DESC LIMIT 10"
                );
                ResultSet rs = ps.executeQuery();

                int rank = 1;
                while (rs.next() && rank <= 10) {
                    String name = rs.getString("name");
                    int wins = rs.getInt("wins");

                    String medal = getMedal(rank);
                    lines.add(medal + " §e" + rank + ". §7" + name + " §8- §a" + wins);
                    rank++;
                }

                rs.close();
                ps.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (lines.isEmpty()) {
                lines.add("§7Keine Daten");
            }

            return lines;
        }

        private List<String> getTopBeds() {
            List<String> lines = new ArrayList<>();

            try {
                PreparedStatement ps = plugin.getMySQL().getConnection().prepareStatement(
                        "SELECT name, beds_destroyed FROM bw_stats ORDER BY beds_destroyed DESC LIMIT 10"
                );
                ResultSet rs = ps.executeQuery();

                int rank = 1;
                while (rs.next() && rank <= 10) {
                    String name = rs.getString("name");
                    int beds = rs.getInt("beds_destroyed");

                    String medal = getMedal(rank);
                    lines.add(medal + " §e" + rank + ". §7" + name + " §8- §c" + beds);
                    rank++;
                }

                rs.close();
                ps.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (lines.isEmpty()) {
                lines.add("§7Keine Daten");
            }

            return lines;
        }

        private List<String> getTopWinstreak() {
            List<String> lines = new ArrayList<>();

            try {
                PreparedStatement ps = plugin.getMySQL().getConnection().prepareStatement(
                        "SELECT name, highest_winstreak FROM bw_stats ORDER BY highest_winstreak DESC LIMIT 10"
                );
                ResultSet rs = ps.executeQuery();

                int rank = 1;
                while (rs.next() && rank <= 10) {
                    String name = rs.getString("name");
                    int streak = rs.getInt("highest_winstreak");

                    String medal = getMedal(rank);
                    lines.add(medal + " §e" + rank + ". §7" + name + " §8- §6" + streak);
                    rank++;
                }

                rs.close();
                ps.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (lines.isEmpty()) {
                lines.add("§7Keine Daten");
            }

            return lines;
        }

        private List<String> getGlobalStats() {
            List<String> lines = new ArrayList<>();

            try {
                PreparedStatement ps = plugin.getMySQL().getConnection().prepareStatement(
                        "SELECT SUM(kills) as total_kills, SUM(deaths) as total_deaths, " +
                                "SUM(wins) as total_wins, SUM(beds_destroyed) as total_beds, " +
                                "SUM(games_played) as total_games FROM bw_stats"
                );
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int totalKills = rs.getInt("total_kills");
                    int totalDeaths = rs.getInt("total_deaths");
                    int totalWins = rs.getInt("total_wins");
                    int totalBeds = rs.getInt("total_beds");
                    int totalGames = rs.getInt("total_games");

                    lines.add("§7Kills: §c" + formatNumber(totalKills));
                    lines.add("§7Deaths: §c" + formatNumber(totalDeaths));
                    lines.add("§7Wins: §a" + formatNumber(totalWins));
                    lines.add("§7Betten: §4" + formatNumber(totalBeds));
                    lines.add("§7Spiele: §e" + formatNumber(totalGames));
                }

                rs.close();
                ps.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (lines.isEmpty()) {
                lines.add("§7Keine Daten");
            }

            return lines;
        }

        private String getMedal(int rank) {
            switch (rank) {
                case 1: return "§6§l❶";
                case 2: return "§7§l❷";
                case 3: return "§c§l❸";
                default: return "§8";
            }
        }

        private String formatNumber(int number) {
            if (number >= 1000000) {
                return String.format("%.1fM", number / 1000000.0);
            } else if (number >= 1000) {
                return String.format("%.1fK", number / 1000.0);
            }
            return String.valueOf(number);
        }
    }

    public enum HologramType {
        TOP_KILLS,
        TOP_WINS,
        TOP_BEDS,
        TOP_WINSTREAK,
        STATS
    }
}