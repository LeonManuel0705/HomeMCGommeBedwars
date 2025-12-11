package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.core.utils.CoreAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AchievementManager {

    private final BedWars plugin;
    private final Map<UUID, Set<String>> unlockedAchievements;
    private final Map<String, Achievement> achievements;

    public AchievementManager(BedWars plugin) {
        this.plugin = plugin;
        this.unlockedAchievements = new ConcurrentHashMap<>();
        this.achievements = new LinkedHashMap<>();

        registerAchievements();
    }

    private void registerAchievements() {
        achievements.put("FIRST_BLOOD", new Achievement(
                "FIRST_BLOOD", "§cErster Kill",
                "Töte einen Gegner", Material.DIAMOND_SWORD, 50,
                AchievementCategory.COMBAT
        ));

        achievements.put("BED_DESTROYER", new Achievement(
                "BED_DESTROYER", "§4Bett-Zerstörer",
                "Zerstöre dein erstes Bett", Material.BED, 100,
                AchievementCategory.OBJECTIVES
        ));

        achievements.put("FIRST_WIN", new Achievement(
                "FIRST_WIN", "§aErster Sieg",
                "Gewinne dein erstes Spiel", Material.GOLD_BLOCK, 100,
                AchievementCategory.WINS
        ));

        achievements.put("KILLER", new Achievement(
                "KILLER", "§cKiller",
                "Erreiche 100 Kills", Material.IRON_SWORD, 200,
                AchievementCategory.COMBAT
        ));

        achievements.put("MASTER_KILLER", new Achievement(
                "MASTER_KILLER", "§4Master Killer",
                "Erreiche 1000 Kills", Material.DIAMOND_SWORD, 500,
                AchievementCategory.COMBAT
        ));

        achievements.put("BED_MASTER", new Achievement(
                "BED_MASTER", "§4Bett-Meister",
                "Zerstöre 50 Betten", Material.BED, 300,
                AchievementCategory.OBJECTIVES
        ));

        achievements.put("WINNER", new Achievement(
                "WINNER", "§eGewinner",
                "Gewinne 10 Spiele", Material.GOLD_BLOCK, 200,
                AchievementCategory.WINS
        ));

        achievements.put("CHAMPION", new Achievement(
                "CHAMPION", "§6Champion",
                "Gewinne 100 Spiele", Material.DIAMOND_BLOCK, 1000,
                AchievementCategory.WINS
        ));

        achievements.put("STREAK_5", new Achievement(
                "STREAK_5", "§aWinstreak 5",
                "Erreiche eine Winstreak von 5", Material.EMERALD, 300,
                AchievementCategory.STREAKS
        ));

        achievements.put("STREAK_10", new Achievement(
                "STREAK_10", "§2Winstreak 10",
                "Erreiche eine Winstreak von 10", Material.EMERALD_BLOCK, 500,
                AchievementCategory.STREAKS
        ));

        achievements.put("UNSTOPPABLE", new Achievement(
                "UNSTOPPABLE", "§4Unaufhaltsam",
                "Erreiche eine Winstreak von 25", Material.DIAMOND, 1500,
                AchievementCategory.STREAKS
        ));

        achievements.put("FAST_WIN", new Achievement(
                "FAST_WIN", "§eSchneller Sieg",
                "Gewinne in unter 5 Minuten", Material.WATCH, 250,
                AchievementCategory.SPECIAL
        ));

        achievements.put("SOLO_DESTROYER", new Achievement(
                "SOLO_DESTROYER", "§cSolo Zerstörer",
                "Zerstöre alle Betten alleine", Material.TNT, 400,
                AchievementCategory.SPECIAL
        ));

        achievements.put("COMEBACK", new Achievement(
                "COMEBACK", "§aComeback King",
                "Gewinne ohne Bett", Material.BEACON, 350,
                AchievementCategory.SPECIAL
        ));

        achievements.put("RICH", new Achievement(
                "RICH", "§6Reichtum",
                "Sammle 10000 Coins", Material.GOLD_INGOT, 500,
                AchievementCategory.COLLECTION
        ));

        achievements.put("SHOPAHOLIC", new Achievement(
                "SHOPAHOLIC", "§eShopping-König",
                "Kaufe 1000 Items", Material.CHEST, 300,
                AchievementCategory.COLLECTION
        ));
    }

    public void loadPlayer(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement ps = plugin.getMySQL().getConnection().prepareStatement(
                        "SELECT achievement_id FROM bw_achievements WHERE uuid = ?"
                );
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();

                Set<String> unlocked = ConcurrentHashMap.newKeySet();
                while (rs.next()) {
                    unlocked.add(rs.getString("achievement_id"));
                }

                unlockedAchievements.put(uuid, unlocked);

                rs.close();
                ps.close();
            } catch (Exception e) {
                plugin.getLogger().severe("Fehler beim Laden der Achievements für " + uuid);
                e.printStackTrace();
            }
        });
    }

    public void unlockAchievement(Player player, String achievementId) {
        UUID uuid = player.getUniqueId();

        if (hasAchievement(uuid, achievementId)) {
            return;
        }

        Achievement achievement = achievements.get(achievementId);
        if (achievement == null) {
            return;
        }

        unlockedAchievements.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet()).add(achievementId);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement ps = plugin.getMySQL().getConnection().prepareStatement(
                        "INSERT INTO bw_achievements (uuid, achievement_id, unlocked_at) VALUES (?, ?, ?)"
                );
                ps.setString(1, uuid.toString());
                ps.setString(2, achievementId);
                ps.setLong(3, System.currentTimeMillis());
                ps.executeUpdate();
                ps.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        if (CoreAPI.isCoreAvailable()) {
            try {
                CoreAPI.getCore().getCoinManager().addCoins(uuid, achievement.getReward());
            } catch (Exception e) {
                plugin.getLogger().warning("Coins konnten nicht hinzugefügt werden");
            }
        }

        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§6§lACHIEVEMENT FREIGESCHALTET!");
        player.sendMessage("");
        player.sendMessage("  " + achievement.getName());
        player.sendMessage("  §7" + achievement.getDescription());
        player.sendMessage("");
        player.sendMessage("  §6Belohnung: §e" + achievement.getReward() + " Coins");
        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
    }

    public void checkAchievements(Player player, StatsManager.PlayerStats stats) {
        UUID uuid = player.getUniqueId();

        if (stats.kills >= 1 && !hasAchievement(uuid, "FIRST_BLOOD")) {
            unlockAchievement(player, "FIRST_BLOOD");
        }

        if (stats.kills >= 100 && !hasAchievement(uuid, "KILLER")) {
            unlockAchievement(player, "KILLER");
        }

        if (stats.kills >= 1000 && !hasAchievement(uuid, "MASTER_KILLER")) {
            unlockAchievement(player, "MASTER_KILLER");
        }

        if (stats.bedsDestroyed >= 1 && !hasAchievement(uuid, "BED_DESTROYER")) {
            unlockAchievement(player, "BED_DESTROYER");
        }

        if (stats.bedsDestroyed >= 50 && !hasAchievement(uuid, "BED_MASTER")) {
            unlockAchievement(player, "BED_MASTER");
        }

        if (stats.wins >= 1 && !hasAchievement(uuid, "FIRST_WIN")) {
            unlockAchievement(player, "FIRST_WIN");
        }

        if (stats.wins >= 10 && !hasAchievement(uuid, "WINNER")) {
            unlockAchievement(player, "WINNER");
        }

        if (stats.wins >= 100 && !hasAchievement(uuid, "CHAMPION")) {
            unlockAchievement(player, "CHAMPION");
        }

        if (stats.currentWinstreak >= 5 && !hasAchievement(uuid, "STREAK_5")) {
            unlockAchievement(player, "STREAK_5");
        }

        if (stats.currentWinstreak >= 10 && !hasAchievement(uuid, "STREAK_10")) {
            unlockAchievement(player, "STREAK_10");
        }

        if (stats.currentWinstreak >= 25 && !hasAchievement(uuid, "UNSTOPPABLE")) {
            unlockAchievement(player, "UNSTOPPABLE");
        }
    }

    public void openAchievementsGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Achievements");

        int slot = 10;
        for (Achievement achievement : achievements.values()) {
            boolean unlocked = hasAchievement(player.getUniqueId(), achievement.getId());

            ItemStack item = new me.leon.bedwars.utils.ItemBuilder(
                    unlocked ? achievement.getIcon() : Material.STAINED_GLASS_PANE, 1, (short) (unlocked ? 0 : 14)
            )
                    .setDisplayName((unlocked ? "§a" : "§7") + achievement.getName())
                    .setLore(
                            "§7" + achievement.getDescription(),
                            "",
                            unlocked ? "§a§lFREIGESCHALTET" : "§c§lGESPERRT",
                            "",
                            "§7Belohnung: §6" + achievement.getReward() + " Coins",
                            "§7Kategorie: §e" + achievement.getCategory().getDisplayName()
                    )
                    .build();

            inv.setItem(slot, item);

            slot++;
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
            if (slot == 35) slot = 37;
            if (slot == 44) slot = 46;
        }

        int unlocked = getUnlockedCount(player.getUniqueId());
        int total = achievements.size();
        double percentage = (double) unlocked / total * 100;

        ItemStack info = new me.leon.bedwars.utils.ItemBuilder(Material.BOOK)
                .setDisplayName("§6Dein Fortschritt")
                .setLore(
                        "§7Freigeschaltet: §e" + unlocked + "§7/§e" + total,
                        "§7Fortschritt: §e" + String.format("%.1f%%", percentage)
                )
                .build();
        inv.setItem(49, info);

        player.openInventory(inv);
    }

    public boolean hasAchievement(UUID uuid, String achievementId) {
        Set<String> unlocked = unlockedAchievements.get(uuid);
        return unlocked != null && unlocked.contains(achievementId);
    }

    public int getUnlockedCount(UUID uuid) {
        Set<String> unlocked = unlockedAchievements.get(uuid);
        return unlocked != null ? unlocked.size() : 0;
    }

    public void unloadPlayer(UUID uuid) {
        unlockedAchievements.remove(uuid);
    }

    public static class Achievement {
        private final String id;
        private final String name;
        private final String description;
        private final Material icon;
        private final int reward;
        private final AchievementCategory category;

        public Achievement(String id, String name, String description, Material icon, int reward, AchievementCategory category) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.icon = icon;
            this.reward = reward;
            this.category = category;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Material getIcon() { return icon; }
        public int getReward() { return reward; }
        public AchievementCategory getCategory() { return category; }
    }

    public enum AchievementCategory {
        COMBAT("Kampf"),
        OBJECTIVES("Ziele"),
        WINS("Siege"),
        STREAKS("Winstreaks"),
        SPECIAL("Spezial"),
        COLLECTION("Sammlung");

        private final String displayName;

        AchievementCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}