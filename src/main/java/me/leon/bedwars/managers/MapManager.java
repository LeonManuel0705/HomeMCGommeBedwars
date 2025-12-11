package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MapManager {

    private final BedWars plugin;
    private final Map<String, MapData> maps;
    private final File mapsFolder;

    public MapManager(BedWars plugin) {
        this.plugin = plugin;
        this.maps = new HashMap<>();
        this.mapsFolder = new File(plugin.getDataFolder(), "maps");

        if (!mapsFolder.exists()) {
            mapsFolder.mkdirs();
        }
    }

    public void loadMaps() {
        maps.clear();

        File[] files = mapsFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files == null || files.length == 0) {
            plugin.getLogger().warning("Keine Maps gefunden!");
            return;
        }

        for (File file : files) {
            try {
                MapData mapData = loadMapFromFile(file);
                maps.put(mapData.getName().toLowerCase(), mapData);
                plugin.getLogger().info("Map geladen: " + mapData.getName());
            } catch (Exception e) {
                plugin.getLogger().severe("Fehler beim Laden der Map: " + file.getName());
                e.printStackTrace();
            }
        }

        plugin.getLogger().info(maps.size() + " Map(s) geladen!");
    }

    private MapData loadMapFromFile(File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        String name = config.getString("name");
        String worldName = config.getString("world");
        int minPlayers = config.getInt("min-players", 2);
        int maxPlayers = config.getInt("max-players", 8);

        Location lobby = deserializeLocation(config.getString("lobby"));
        Location spec = deserializeLocation(config.getString("spectator"));

        List<TeamData> teams = new ArrayList<>();
        for (String teamKey : config.getConfigurationSection("teams").getKeys(false)) {
            String teamPath = "teams." + teamKey;

            String teamName = config.getString(teamPath + ".name");
            String teamShortName = config.getString(teamPath + ".short-name");
            ChatColor color = ChatColor.valueOf(config.getString(teamPath + ".color"));

            Location spawn = deserializeLocation(config.getString(teamPath + ".spawn"));
            Location bed = deserializeLocation(config.getString(teamPath + ".bed"));
            Location shopNPC = deserializeLocation(config.getString(teamPath + ".shop-npc"));
            Location upgradeNPC = deserializeLocation(config.getString(teamPath + ".upgrade-npc"));

            List<Location> generators = new ArrayList<>();
            for (String genLoc : config.getStringList(teamPath + ".generators")) {
                generators.add(deserializeLocation(genLoc));
            }

            TeamData team = new TeamData(teamName, teamShortName, color, spawn, bed,
                    shopNPC, upgradeNPC, generators);
            teams.add(team);
        }

        List<Location> bronzeGens = new ArrayList<>();
        if (config.contains("bronze-generators")) {
            for (String loc : config.getStringList("bronze-generators")) {
                bronzeGens.add(deserializeLocation(loc));
            }
        }

        List<Location> goldGens = new ArrayList<>();
        if (config.contains("gold-generators")) {
            for (String loc : config.getStringList("gold-generators")) {
                goldGens.add(deserializeLocation(loc));
            }
        }

        return new MapData(name, worldName, minPlayers, maxPlayers, lobby, spec,
                teams, bronzeGens, goldGens);
    }

    public void saveMap(MapData mapData) {
        File file = new File(mapsFolder, mapData.getName() + ".yml");
        FileConfiguration config = new YamlConfiguration();

        config.set("name", mapData.getName());
        config.set("world", mapData.getWorldName());
        config.set("min-players", mapData.getMinPlayers());
        config.set("max-players", mapData.getMaxPlayers());
        config.set("lobby", serializeLocation(mapData.getLobby()));
        config.set("spectator", serializeLocation(mapData.getSpectator()));

        int teamIndex = 1;
        for (TeamData team : mapData.getTeams()) {
            String teamPath = "teams.team" + teamIndex;

            config.set(teamPath + ".name", team.getName());
            config.set(teamPath + ".short-name", team.getShortName());
            config.set(teamPath + ".color", team.getChatColor().name());
            config.set(teamPath + ".spawn", serializeLocation(team.getSpawn()));
            config.set(teamPath + ".bed", serializeLocation(team.getBed()));
            config.set(teamPath + ".shop-npc", serializeLocation(team.getShopNPC()));
            config.set(teamPath + ".upgrade-npc", serializeLocation(team.getUpgradeNPC()));

            List<String> gens = new ArrayList<>();
            for (Location loc : team.getGenerators()) {
                gens.add(serializeLocation(loc));
            }
            config.set(teamPath + ".generators", gens);

            teamIndex++;
        }

        List<String> bronzeGens = new ArrayList<>();
        for (Location loc : mapData.getBronzeGenerators()) {
            bronzeGens.add(serializeLocation(loc));
        }
        config.set("bronze-generators", bronzeGens);

        List<String> goldGens = new ArrayList<>();
        for (Location loc : mapData.getGoldGenerators()) {
            goldGens.add(serializeLocation(loc));
        }
        config.set("gold-generators", goldGens);

        try {
            config.save(file);
            plugin.getLogger().info("Map gespeichert: " + mapData.getName());
        } catch (IOException e) {
            plugin.getLogger().severe("Fehler beim Speichern der Map: " + mapData.getName());
            e.printStackTrace();
        }
    }

    private String serializeLocation(Location loc) {
        if (loc == null) return null;
        return loc.getWorld().getName() + ";" +
                loc.getX() + ";" +
                loc.getY() + ";" +
                loc.getZ() + ";" +
                loc.getYaw() + ";" +
                loc.getPitch();
    }

    private Location deserializeLocation(String str) {
        if (str == null) return null;
        String[] parts = str.split(";");

        World world = Bukkit.getWorld(parts[0]);
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = Float.parseFloat(parts[4]);
        float pitch = Float.parseFloat(parts[5]);

        return new Location(world, x, y, z, yaw, pitch);
    }

    public MapData getMap(String name) {
        return maps.get(name.toLowerCase());
    }

    public Collection<MapData> getAllMaps() {
        return maps.values();
    }

    public MapData getRandomMap() {
        if (maps.isEmpty()) return null;
        List<MapData> list = new ArrayList<>(maps.values());
        return list.get(new Random().nextInt(list.size()));
    }
}