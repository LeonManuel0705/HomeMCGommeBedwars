package me.leon.bedwars.cosmetics;

import me.leon.bedwars.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CosmeticsManager {

    private final BedWars plugin;
    private final Map<UUID, PlayerCosmetics> playerCosmetics = new ConcurrentHashMap<>();

    public CosmeticsManager(BedWars plugin) {
        this.plugin = plugin;
        createTable();
    }

    private void createTable() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement ps = plugin.getCore().getMySQL().getConnection().prepareStatement(
                        "CREATE TABLE IF NOT EXISTS bedwars_cosmetics (" +
                                "uuid VARCHAR(36) PRIMARY KEY, " +
                                "kill_message VARCHAR(50) DEFAULT 'DEFAULT', " +
                                "death_message VARCHAR(50) DEFAULT 'DEFAULT', " +
                                "bed_destroy_sound VARCHAR(50) DEFAULT 'DEFAULT', " +
                                "victory_dance VARCHAR(50) DEFAULT 'DEFAULT', " +
                                "owned_cosmetics TEXT DEFAULT ''" +
                                ")"
                );
                ps.executeUpdate();
                ps.close();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to create cosmetics table: " + e.getMessage());
            }
        });
    }

    public void loadPlayerCosmetics(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement ps = plugin.getCore().getMySQL().getConnection().prepareStatement(
                        "SELECT * FROM bedwars_cosmetics WHERE uuid = ?"
                );
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();

                PlayerCosmetics cosmetics;
                if (rs.next()) {
                    cosmetics = new PlayerCosmetics(
                            KillMessage.valueOf(rs.getString("kill_message")),
                            DeathMessage.valueOf(rs.getString("death_message")),
                            BedDestroySound.valueOf(rs.getString("bed_destroy_sound")),
                            VictoryDance.valueOf(rs.getString("victory_dance")),
                            rs.getString("owned_cosmetics")
                    );
                } else {
                    cosmetics = new PlayerCosmetics();
                    createPlayerCosmetics(uuid);
                }

                playerCosmetics.put(uuid, cosmetics);
                rs.close();
                ps.close();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load cosmetics for " + uuid + ": " + e.getMessage());
                playerCosmetics.put(uuid, new PlayerCosmetics());
            }
        });
    }

    private void createPlayerCosmetics(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement ps = plugin.getCore().getMySQL().getConnection().prepareStatement(
                        "INSERT INTO bedwars_cosmetics (uuid) VALUES (?)"
                );
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
                ps.close();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to create cosmetics for " + uuid);
            }
        });
    }

    public void savePlayerCosmetics(UUID uuid) {
        PlayerCosmetics cosmetics = playerCosmetics.get(uuid);
        if (cosmetics == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement ps = plugin.getCore().getMySQL().getConnection().prepareStatement(
                        "UPDATE bedwars_cosmetics SET kill_message = ?, death_message = ?, " +
                                "bed_destroy_sound = ?, victory_dance = ?, owned_cosmetics = ? WHERE uuid = ?"
                );
                ps.setString(1, cosmetics.getKillMessage().name());
                ps.setString(2, cosmetics.getDeathMessage().name());
                ps.setString(3, cosmetics.getBedDestroySound().name());
                ps.setString(4, cosmetics.getVictoryDance().name());
                ps.setString(5, cosmetics.getOwnedCosmeticsString());
                ps.setString(6, uuid.toString());
                ps.executeUpdate();
                ps.close();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save cosmetics for " + uuid);
            }
        });
    }

    public void unloadPlayerCosmetics(UUID uuid) {
        savePlayerCosmetics(uuid);
        playerCosmetics.remove(uuid);
    }

    public PlayerCosmetics getCosmetics(UUID uuid) {
        return playerCosmetics.getOrDefault(uuid, new PlayerCosmetics());
    }

    public String formatKillMessage(String killerName, String killerColor, String victimName, String victimColor, UUID killerUUID) {
        PlayerCosmetics cosmetics = getCosmetics(killerUUID);
        KillMessage killMsg = cosmetics.getKillMessage();
        return killMsg.format(killerName, killerColor, victimName, victimColor);
    }

    public String formatDeathMessage(String playerName, String playerColor, UUID playerUUID) {
        PlayerCosmetics cosmetics = getCosmetics(playerUUID);
        DeathMessage deathMsg = cosmetics.getDeathMessage();
        return deathMsg.format(playerName, playerColor);
    }

    public void playBedDestroySound(Player destroyer, org.bukkit.Location bedLocation) {
        PlayerCosmetics cosmetics = getCosmetics(destroyer.getUniqueId());
        BedDestroySound sound = cosmetics.getBedDestroySound();
        sound.play(bedLocation);
    }

    // Enums for cosmetics
    public enum KillMessage {
        DEFAULT("wurde von", "getötet"),
        DESTROYED("wurde von", "zerstört"),
        ELIMINATED("wurde von", "eliminiert"),
        CRUSHED("wurde von", "zerquetscht"),
        OBLITERATED("wurde von", "vernichtet"),
        TERMINATED("wurde von", "terminiert"),
        ANNIHILATED("wurde von", "ausgelöscht"),
        SLAYED("wurde von", "erschlagen");

        private final String prefix;
        private final String suffix;

        KillMessage(String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public String format(String killerName, String killerColor, String victimName, String victimColor) {
            return victimColor + victimName + " §7" + prefix + " " + killerColor + killerName + " §7" + suffix;
        }

        public String getDisplayName() {
            switch (this) {
                case DEFAULT: return "§fStandard";
                case DESTROYED: return "§cZerstört";
                case ELIMINATED: return "§6Eliminiert";
                case CRUSHED: return "§eZerquetscht";
                case OBLITERATED: return "§4Vernichtet";
                case TERMINATED: return "§bTerminiert";
                case ANNIHILATED: return "§5Ausgelöscht";
                case SLAYED: return "§aErschlagen";
                default: return "§7Unbekannt";
            }
        }
    }

    public enum DeathMessage {
        DEFAULT("ist gestorben"),
        PERISHED("ist umgekommen"),
        FELL("ist gefallen"),
        DIED("hat den Geist aufgegeben"),
        VANISHED("ist verschwunden"),
        EXPIRED("ist abgelaufen");

        private final String message;

        DeathMessage(String message) {
            this.message = message;
        }

        public String format(String playerName, String playerColor) {
            return playerColor + playerName + " §7" + message;
        }

        public String getDisplayName() {
            switch (this) {
                case DEFAULT: return "§fStandard";
                case PERISHED: return "§cUmgekommen";
                case FELL: return "§6Gefallen";
                case DIED: return "§eGeist aufgegeben";
                case VANISHED: return "§bVerschwunden";
                case EXPIRED: return "§5Abgelaufen";
                default: return "§7Unbekannt";
            }
        }
    }

    public enum BedDestroySound {
        DEFAULT(Sound.ENDERDRAGON_GROWL, 1.0f, 1.0f),
        EXPLOSION(Sound.EXPLODE, 1.0f, 1.0f),
        THUNDER(Sound.AMBIENCE_THUNDER, 1.0f, 1.0f),
        WITHER(Sound.WITHER_SPAWN, 1.0f, 1.0f),
        ANVIL(Sound.ANVIL_LAND, 1.0f, 0.5f),
        GLASS(Sound.GLASS, 1.0f, 1.0f),
        ZOMBIE(Sound.ZOMBIE_DEATH, 1.0f, 0.5f),
        FIREWORK(Sound.FIREWORK_BLAST, 1.0f, 1.0f);

        private final Sound sound;
        private final float volume;
        private final float pitch;

        BedDestroySound(Sound sound, float volume, float pitch) {
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
        }

        public void play(org.bukkit.Location location) {
            location.getWorld().playSound(location, sound, volume, pitch);
        }

        public String getDisplayName() {
            switch (this) {
                case DEFAULT: return "§fDrachen-Grollen";
                case EXPLOSION: return "§cExplosion";
                case THUNDER: return "§eDonner";
                case WITHER: return "§8Wither";
                case ANVIL: return "§7Amboss";
                case GLASS: return "§bGlas";
                case ZOMBIE: return "§2Zombie";
                case FIREWORK: return "§dFeuerwerk";
                default: return "§7Unbekannt";
            }
        }
    }

    public enum VictoryDance {
        DEFAULT,
        FIREWORKS,
        LIGHTNING,
        PARTICLES;

        public String getDisplayName() {
            switch (this) {
                case DEFAULT: return "§fKein Tanz";
                case FIREWORKS: return "§dFeuerwerk";
                case LIGHTNING: return "§eBlitze";
                case PARTICLES: return "§bPartikel";
                default: return "§7Unbekannt";
            }
        }
    }

    // Player cosmetics data class
    public static class PlayerCosmetics {
        private KillMessage killMessage;
        private DeathMessage deathMessage;
        private BedDestroySound bedDestroySound;
        private VictoryDance victoryDance;
        private final java.util.Set<String> ownedCosmetics;

        public PlayerCosmetics() {
            this.killMessage = KillMessage.DEFAULT;
            this.deathMessage = DeathMessage.DEFAULT;
            this.bedDestroySound = BedDestroySound.DEFAULT;
            this.victoryDance = VictoryDance.DEFAULT;
            this.ownedCosmetics = ConcurrentHashMap.newKeySet();
            // Everyone owns the default cosmetics
            ownedCosmetics.add("KILL_DEFAULT");
            ownedCosmetics.add("DEATH_DEFAULT");
            ownedCosmetics.add("SOUND_DEFAULT");
            ownedCosmetics.add("DANCE_DEFAULT");
        }

        public PlayerCosmetics(KillMessage killMessage, DeathMessage deathMessage,
                               BedDestroySound bedDestroySound, VictoryDance victoryDance, String ownedString) {
            this.killMessage = killMessage;
            this.deathMessage = deathMessage;
            this.bedDestroySound = bedDestroySound;
            this.victoryDance = victoryDance;
            this.ownedCosmetics = ConcurrentHashMap.newKeySet();
            if (ownedString != null && !ownedString.isEmpty()) {
                for (String s : ownedString.split(",")) {
                    ownedCosmetics.add(s.trim());
                }
            }
            // Ensure defaults are owned
            ownedCosmetics.add("KILL_DEFAULT");
            ownedCosmetics.add("DEATH_DEFAULT");
            ownedCosmetics.add("SOUND_DEFAULT");
            ownedCosmetics.add("DANCE_DEFAULT");
        }

        public KillMessage getKillMessage() { return killMessage; }
        public void setKillMessage(KillMessage killMessage) { this.killMessage = killMessage; }

        public DeathMessage getDeathMessage() { return deathMessage; }
        public void setDeathMessage(DeathMessage deathMessage) { this.deathMessage = deathMessage; }

        public BedDestroySound getBedDestroySound() { return bedDestroySound; }
        public void setBedDestroySound(BedDestroySound bedDestroySound) { this.bedDestroySound = bedDestroySound; }

        public VictoryDance getVictoryDance() { return victoryDance; }
        public void setVictoryDance(VictoryDance victoryDance) { this.victoryDance = victoryDance; }

        public boolean ownsCosmetic(String cosmeticId) {
            return ownedCosmetics.contains(cosmeticId);
        }

        public void addOwnedCosmetic(String cosmeticId) {
            ownedCosmetics.add(cosmeticId);
        }

        public String getOwnedCosmeticsString() {
            return String.join(",", ownedCosmetics);
        }
    }
}
