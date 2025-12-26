package me.leon.bedwars.npc;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.lobby.LobbyManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerNPCManager {

    private final BedWars plugin;
    private final Map<UUID, PlayerNPC> npcs = new ConcurrentHashMap<>();
    private final Map<Integer, UUID> entityIdToNpcId = new ConcurrentHashMap<>();
    private final File npcFile;
    private BukkitTask lookAtTask;

    public PlayerNPCManager(BedWars plugin) {
        this.plugin = plugin;
        this.npcFile = new File(plugin.getDataFolder(), "player_npcs.yml");
        loadNPCs();
        startLookAtTask();
    }

    public PlayerNPC createNPC(Location location, String displayName, String skinPlayerName, String npcType) {
        UUID npcId = UUID.randomUUID();

        SkinFetcher.fetchSkinAsync(skinPlayerName != null ? skinPlayerName : "Steve", skin -> {
            String skinValue = skin != null ? skin[0] : null;
            String skinSignature = skin != null ? skin[1] : null;

            PlayerNPC npc = new PlayerNPC(npcId, displayName, location, skinValue, skinSignature, npcType);
            npcs.put(npcId, npc);

            npc.spawn();

            if (npc.getEntityId() != -1) {
                entityIdToNpcId.put(npc.getEntityId(), npcId);
            }

            saveNPCs();
            plugin.getLogger().info("Created player NPC '" + displayName + "' with skin from " + skinPlayerName);
        });

        return null;
    }

    public PlayerNPC createNPCWithSkin(Location location, String displayName, String skinValue, String skinSignature, String npcType) {
        UUID npcId = UUID.randomUUID();

        PlayerNPC npc = new PlayerNPC(npcId, displayName, location, skinValue, skinSignature, npcType);
        npcs.put(npcId, npc);

        npc.spawn();

        if (npc.getEntityId() != -1) {
            entityIdToNpcId.put(npc.getEntityId(), npcId);
        }

        saveNPCs();
        return npc;
    }

    public void removeNPC(UUID npcId) {
        PlayerNPC npc = npcs.remove(npcId);
        if (npc != null) {
            entityIdToNpcId.remove(npc.getEntityId());
            npc.destroy();
            saveNPCs();
        }
    }

    public void removeNearestNPC(Location location, double radius) {
        UUID nearestId = null;
        double nearestDist = radius;

        for (PlayerNPC npc : npcs.values()) {
            if (npc.getLocation().getWorld().equals(location.getWorld())) {
                double dist = npc.getLocation().distance(location);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearestId = npc.getId();
                }
            }
        }

        if (nearestId != null) {
            removeNPC(nearestId);
        }
    }

    public PlayerNPC getNPCByEntityId(int entityId) {
        UUID npcId = entityIdToNpcId.get(entityId);
        return npcId != null ? npcs.get(npcId) : null;
    }

    public void handleNPCClick(Player player, int entityId) {
        PlayerNPC npc = getNPCByEntityId(entityId);
        if (npc == null) return;

        String npcType = npc.getNpcType();

        if (npcType.equals("PRIVATE")) {
            plugin.getPrivateRoundGUI().openMainMenu(player);
            return;
        }

        try {
            LobbyManager.GameMode gameMode = LobbyManager.GameMode.valueOf(npcType);
            plugin.getNPCManager().joinBestArena(player, gameMode);
        } catch (IllegalArgumentException e) {
            plugin.getNPCManager().joinBestArena(player, LobbyManager.GameMode.TEAM);
        }
    }

    public void showAllNPCs(Player player) {
        for (PlayerNPC npc : npcs.values()) {
            npc.showTo(player);
        }
    }

    public void hideAllNPCs(Player player) {
        for (PlayerNPC npc : npcs.values()) {
            npc.hideFrom(player);
        }
    }

    public void respawnAllNPCs() {
        for (PlayerNPC npc : npcs.values()) {
            npc.destroy();
            npc.spawn();
            if (npc.getEntityId() != -1) {
                entityIdToNpcId.put(npc.getEntityId(), npc.getId());
            }
        }
    }

    private void startLookAtTask() {
        lookAtTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (PlayerNPC npc : npcs.values()) {
                Player nearest = getNearestPlayer(npc.getLocation(), 10.0);
                if (nearest != null) {
                    npc.lookAt(nearest.getEyeLocation());
                }
            }
        }, 5L, 5L);
    }

    private Player getNearestPlayer(Location location, double radius) {
        Player nearest = null;
        double nearestDist = radius;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().equals(location.getWorld())) continue;

            double dist = player.getLocation().distance(location);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = player;
            }
        }

        return nearest;
    }

    private void loadNPCs() {
        if (!npcFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(npcFile);

        for (String key : config.getKeys(false)) {
            try {
                UUID id = UUID.fromString(config.getString(key + ".id"));
                String world = config.getString(key + ".world");
                double x = config.getDouble(key + ".x");
                double y = config.getDouble(key + ".y");
                double z = config.getDouble(key + ".z");
                float yaw = (float) config.getDouble(key + ".yaw");
                float pitch = (float) config.getDouble(key + ".pitch");
                String displayName = config.getString(key + ".displayName", "§6§lBedWars");
                String npcType = config.getString(key + ".npcType", "TEAM");
                String skinValue = config.getString(key + ".skinValue");
                String skinSignature = config.getString(key + ".skinSignature");

                org.bukkit.World bukkitWorld = plugin.getServer().getWorld(world);
                if (bukkitWorld == null) continue;

                Location loc = new Location(bukkitWorld, x, y, z, yaw, pitch);

                PlayerNPC npc = new PlayerNPC(id, displayName, loc, skinValue, skinSignature, npcType);
                npcs.put(id, npc);

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load player NPC: " + key + " - " + e.getMessage());
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (PlayerNPC npc : npcs.values()) {
                npc.spawn();
                if (npc.getEntityId() != -1) {
                    entityIdToNpcId.put(npc.getEntityId(), npc.getId());
                }
            }
            plugin.getLogger().info("Loaded " + npcs.size() + " player NPCs");
        }, 40L);
    }

    private void saveNPCs() {
        YamlConfiguration config = new YamlConfiguration();

        int index = 0;
        for (PlayerNPC npc : npcs.values()) {
            String key = "npc" + index;
            config.set(key + ".id", npc.getId().toString());
            config.set(key + ".world", npc.getLocation().getWorld().getName());
            config.set(key + ".x", npc.getLocation().getX());
            config.set(key + ".y", npc.getLocation().getY());
            config.set(key + ".z", npc.getLocation().getZ());
            config.set(key + ".yaw", npc.getLocation().getYaw());
            config.set(key + ".pitch", npc.getLocation().getPitch());
            config.set(key + ".displayName", npc.getName());
            config.set(key + ".npcType", npc.getNpcType());
            config.set(key + ".skinValue", npc.getSkinValue());
            config.set(key + ".skinSignature", npc.getSkinSignature());
            index++;
        }

        try {
            config.save(npcFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save player NPCs: " + e.getMessage());
        }
    }

    public void shutdown() {
        if (lookAtTask != null) {
            lookAtTask.cancel();
        }

        for (PlayerNPC npc : npcs.values()) {
            npc.destroy();
        }
        npcs.clear();
        entityIdToNpcId.clear();
    }

    public Map<UUID, PlayerNPC> getNPCs() {
        return npcs;
    }
}
