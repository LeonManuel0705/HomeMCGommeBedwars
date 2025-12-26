package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import me.leon.bedwars.lobby.LobbyManager;
import me.leon.core.managers.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NPCManager {

    private final BedWars plugin;
    private final Map<Location, NPCData> npcs = new ConcurrentHashMap<>();
    private final Map<UUID, Villager> spawnedNPCs = new ConcurrentHashMap<>();
    private final Map<UUID, Location> npcSpawnLocations = new ConcurrentHashMap<>();
    private final File npcFile;
    private BukkitTask freezeTask;

    public NPCManager(BedWars plugin) {
        this.plugin = plugin;
        this.npcFile = new File(plugin.getDataFolder(), "npcs.yml");
        loadNPCs();
        startFreezeTask();
    }

    private void startFreezeTask() {
        freezeTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Map.Entry<UUID, Villager> entry : spawnedNPCs.entrySet()) {
                Villager villager = entry.getValue();
                Location spawnLoc = npcSpawnLocations.get(entry.getKey());

                if (villager != null && !villager.isDead() && spawnLoc != null) {
                    Location current = villager.getLocation();
                    if (current.distanceSquared(spawnLoc) > 0.1) {
                        villager.teleport(spawnLoc);
                    }
                }
            }
        }, 5L, 5L);
    }

    private Villager createVillager(Location location, LobbyManager.GameMode gameMode, String displayName) {
        return createVillagerInternal(location, gameMode != null ? gameMode.name() : "TEAM", displayName);
    }

    private Villager createVillagerInternal(Location location, String npcType, String displayName) {
        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);

        villager.setCustomName(displayName);
        villager.setCustomNameVisible(true);
        villager.setCanPickupItems(false);
        villager.setProfession(Villager.Profession.FARMER);
        villager.setAdult();

        try {
            Object nmsEntity = villager.getClass().getMethod("getHandle").invoke(villager);

            java.lang.reflect.Field navigationField = nmsEntity.getClass().getDeclaredField("navigation");
            navigationField.setAccessible(true);
            Object navigation = navigationField.get(nmsEntity);
            if (navigation != null) {
                java.lang.reflect.Method clearMethod = navigation.getClass().getMethod("n");
                clearMethod.invoke(navigation);
            }

            Object goalSelector = nmsEntity.getClass().getDeclaredField("goalSelector").get(nmsEntity);
            Object targetSelector = nmsEntity.getClass().getDeclaredField("targetSelector").get(nmsEntity);

            java.lang.reflect.Field goalsField = goalSelector.getClass().getDeclaredField("b");
            goalsField.setAccessible(true);
            ((Set<?>) goalsField.get(goalSelector)).clear();

            java.lang.reflect.Field targetsField = targetSelector.getClass().getDeclaredField("b");
            targetsField.setAccessible(true);
            ((Set<?>) targetsField.get(targetSelector)).clear();

            java.lang.reflect.Field cField = goalSelector.getClass().getDeclaredField("c");
            cField.setAccessible(true);
            ((Set<?>) cField.get(goalSelector)).clear();

        } catch (Exception e) {
            plugin.getLogger().warning("Could not fully disable NPC AI: " + e.getMessage());
        }

        villager.setMetadata("bw_join_npc", new FixedMetadataValue(plugin, npcType));

        return villager;
    }

    public void spawnNPC(Location location, LobbyManager.GameMode gameMode, String displayName) {
        spawnNPCInternal(location, gameMode.name(), displayName);
    }

    public void spawnPrivateNPC(Location location, String displayName) {
        spawnNPCInternal(location, "PRIVATE", displayName);
    }

    private void spawnNPCInternal(Location location, String npcType, String displayName) {
        Villager villager = createVillagerInternal(location, npcType, displayName);

        UUID npcId = UUID.randomUUID();
        spawnedNPCs.put(npcId, villager);
        npcSpawnLocations.put(npcId, location.clone());

        NPCData data = new NPCData(npcId, location.clone(), npcType, displayName);
        npcs.put(location, data);

        saveNPCs();

        plugin.getLogger().info("Spawned BedWars NPC at " + location + " for type " + npcType);
    }

    public void removeNPC(Location location) {
        NPCData data = npcs.remove(location);
        if (data != null) {
            Villager villager = spawnedNPCs.remove(data.getId());
            npcSpawnLocations.remove(data.getId());
            if (villager != null && !villager.isDead()) {
                villager.remove();
            }
            saveNPCs();
        }
    }

    public void removeNearestNPC(Location location, double radius) {
        Location closest = null;
        double closestDistance = radius;

        for (Location npcLoc : npcs.keySet()) {
            if (npcLoc.getWorld().equals(location.getWorld())) {
                double distance = npcLoc.distance(location);
                if (distance < closestDistance) {
                    closest = npcLoc;
                    closestDistance = distance;
                }
            }
        }

        if (closest != null) {
            removeNPC(closest);
        }
    }

    public void handleNPCClick(Player player, Villager villager) {
        if (!villager.hasMetadata("bw_join_npc")) return;

        String gameModeStr = villager.getMetadata("bw_join_npc").get(0).asString();

        if (gameModeStr.equals("PRIVATE")) {
            plugin.getPrivateRoundGUI().openMainMenu(player);
            return;
        }

        LobbyManager.GameMode gameMode;
        try {
            gameMode = LobbyManager.GameMode.valueOf(gameModeStr);
        } catch (IllegalArgumentException e) {
            gameMode = LobbyManager.GameMode.TEAM;
        }

        joinBestArena(player, gameMode);
    }

    public void joinBestArena(Player player, LobbyManager.GameMode gameMode) {
        if (plugin.getGameManager().getArenaByPlayer(player.getUniqueId()) != null) {
            player.sendMessage(BedWars.PREFIX + "§cDu bist bereits in einer Runde!");
            return;
        }

        int partySize = getPartySize(player);

        Arena bestArena = findBestArena(gameMode, partySize);

        if (bestArena == null) {
            player.sendMessage(BedWars.PREFIX + "§cKeine passende Runde gefunden!");
            return;
        }

        if (partySize > 1) {
            joinWithParty(player, bestArena);
        } else {
            plugin.getGameManager().joinArena(player, bestArena.getName());
        }
    }

    private Arena findBestArena(LobbyManager.GameMode gameMode, int partySize) {
        List<Arena> candidates = new ArrayList<>();

        for (Arena arena : plugin.getArenaManager().getArenas()) {
            if (arena.getGameMode() != gameMode) continue;
            if (arena.getGameState() != GameState.WAITING && arena.getGameState() != GameState.STARTING) continue;

            int availableSlots = arena.getMaxPlayers() - arena.getPlayers().size();
            if (availableSlots < partySize) continue;

            candidates.add(arena);
        }

        if (candidates.isEmpty()) return null;

        candidates.sort((a, b) -> {
            if (a.getGameState() == GameState.STARTING && b.getGameState() != GameState.STARTING) return -1;
            if (b.getGameState() == GameState.STARTING && a.getGameState() != GameState.STARTING) return 1;

            return Integer.compare(b.getPlayers().size(), a.getPlayers().size());
        });

        return candidates.get(0);
    }

    private int getPartySize(Player player) {
        PartyManager partyManager = plugin.getCore().getPartyManager();
        if (partyManager.isInParty(player.getUniqueId())) {
            PartyManager.Party party = partyManager.getParty(player.getUniqueId());
            return party.getMembers().size();
        }
        return 1;
    }

    private void joinWithParty(Player leader, Arena arena) {
        PartyManager partyManager = plugin.getCore().getPartyManager();
        PartyManager.Party party = partyManager.getParty(leader.getUniqueId());

        if (party == null || !party.isLeader(leader.getUniqueId())) {
            leader.sendMessage(BedWars.PREFIX + "§cNur der Party-Leader kann die Party in eine Runde bringen!");
            return;
        }

        int availableSlots = arena.getMaxPlayers() - arena.getPlayers().size();
        if (availableSlots < party.getMembers().size()) {
            leader.sendMessage(BedWars.PREFIX + "§cNicht genug Platz für deine Party! §7(" +
                    party.getMembers().size() + " Spieler, " + availableSlots + " Plätze frei)");
            return;
        }

        for (UUID memberUUID : party.getMembers()) {
            Player member = plugin.getServer().getPlayer(memberUUID);
            if (member != null && member.isOnline()) {
                if (plugin.getGameManager().getArenaByPlayer(member.getUniqueId()) == null) {
                    plugin.getGameManager().joinArena(member, arena.getName());
                }
            }
        }
    }

    public void respawnAllNPCs() {
        for (Villager villager : spawnedNPCs.values()) {
            if (villager != null && !villager.isDead()) {
                villager.remove();
            }
        }
        spawnedNPCs.clear();
        npcSpawnLocations.clear();

        for (NPCData data : npcs.values()) {
            if (data.getLocation().getWorld() != null) {
                Villager villager = createVillagerInternal(data.getLocation(), data.getNpcType(), data.getDisplayName());
                spawnedNPCs.put(data.getId(), villager);
                npcSpawnLocations.put(data.getId(), data.getLocation().clone());
            }
        }

        plugin.getLogger().info("Respawned " + npcs.size() + " join NPCs");
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
                String npcType = config.getString(key + ".npcType", config.getString(key + ".gameMode", "TEAM"));
                String displayName = config.getString(key + ".displayName", "§6§lBedWars");

                org.bukkit.World bukkitWorld = plugin.getServer().getWorld(world);
                if (bukkitWorld == null) continue;

                Location loc = new Location(bukkitWorld, x, y, z, yaw, pitch);

                NPCData data = new NPCData(id, loc, npcType, displayName);
                npcs.put(loc, data);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load NPC: " + key + " - " + e.getMessage());
            }
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, this::respawnAllNPCs, 20L);

        plugin.getLogger().info("Loaded " + npcs.size() + " join NPCs from config");
    }

    private void saveNPCs() {
        YamlConfiguration config = new YamlConfiguration();

        int index = 0;
        for (NPCData data : npcs.values()) {
            String key = "npc" + index;
            config.set(key + ".id", data.getId().toString());
            config.set(key + ".world", data.getLocation().getWorld().getName());
            config.set(key + ".x", data.getLocation().getX());
            config.set(key + ".y", data.getLocation().getY());
            config.set(key + ".z", data.getLocation().getZ());
            config.set(key + ".yaw", data.getLocation().getYaw());
            config.set(key + ".pitch", data.getLocation().getPitch());
            config.set(key + ".npcType", data.getNpcType());
            config.set(key + ".displayName", data.getDisplayName());
            index++;
        }

        try {
            config.save(npcFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save NPCs: " + e.getMessage());
        }
    }

    public Map<Location, NPCData> getNPCs() {
        return npcs;
    }

    public static class NPCData {
        private final UUID id;
        private final Location location;
        private final String npcType;
        private final String displayName;

        public NPCData(UUID id, Location location, LobbyManager.GameMode gameMode, String displayName) {
            this(id, location, gameMode != null ? gameMode.name() : "TEAM", displayName);
        }

        public NPCData(UUID id, Location location, String npcType, String displayName) {
            this.id = id;
            this.location = location;
            this.npcType = npcType;
            this.displayName = displayName;
        }

        public UUID getId() { return id; }
        public Location getLocation() { return location; }
        public String getNpcType() { return npcType; }
        public LobbyManager.GameMode getGameMode() {
            try {
                return LobbyManager.GameMode.valueOf(npcType);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        public String getDisplayName() { return displayName; }
    }
}
