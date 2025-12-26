package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.lobby.LobbyManager;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
import me.leon.bedwars.managers.TeamManager.TeamColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class ArenaManager {

    private final BedWars plugin;
    private final File arenasFolder;

    public ArenaManager(BedWars plugin) {
        this.plugin = plugin;
        this.arenasFolder = new File(plugin.getDataFolder(), "arenas");

        if (!arenasFolder.exists()) {
            arenasFolder.mkdirs();
        }
    }

    public void loadArenas() {
        File[] files = arenasFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files == null || files.length == 0) {
            plugin.getLogger().info("Keine Arenas gefunden!");
            return;
        }

        for (File file : files) {
            loadArena(file);
        }

        plugin.getLogger().info(files.length + " Arena(s) geladen!");

        // Clean up any leftover holograms from generators
        plugin.getServer().getScheduler().runTaskLater(plugin, this::cleanupGeneratorHolograms, 40L);
    }

    /**
     * Removes any leftover hologram ArmorStands near generator locations.
     */
    public void cleanupGeneratorHolograms() {
        int removed = 0;
        for (Arena arena : getArenas()) {
            for (Arena.GeneratorData gen : arena.getGenerators().values()) {
                if (gen.getLocation() != null && gen.getLocation().getWorld() != null) {
                    for (org.bukkit.entity.Entity entity : gen.getLocation().getWorld().getNearbyEntities(
                            gen.getLocation(), 5, 5, 5)) {
                        if (entity instanceof org.bukkit.entity.ArmorStand) {
                            org.bukkit.entity.ArmorStand armorStand = (org.bukkit.entity.ArmorStand) entity;
                            // Remove invisible/small/no-gravity armor stands (holograms)
                            if (!armorStand.isVisible() || armorStand.isSmall() || !armorStand.hasGravity()) {
                                entity.remove();
                                removed++;
                            }
                        }
                    }
                }
            }
        }
        if (removed > 0) {
            plugin.getLogger().info("Removed " + removed + " leftover generator holograms");
        }
    }

    private void loadArena(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        String name = config.getString("name");
        String displayName = config.getString("displayName");
        int minPlayers = config.getInt("minPlayers");
        int maxPlayers = config.getInt("maxPlayers");
        int teamsPerGame = config.getInt("teamsPerGame");
        String gameModeStr = config.getString("gameMode", "TEAM");

        LobbyManager.GameMode gameMode = LobbyManager.GameMode.TEAM;
        try {
            gameMode = LobbyManager.GameMode.valueOf(gameModeStr);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Ungültiger GameMode für Arena " + name + ", nutze TEAM");
        }

        Arena arena = new Arena(name, displayName, minPlayers, maxPlayers, teamsPerGame, gameMode);

        arena.setLobby(getLocation(config, "lobby"));
        arena.setSpectator(getLocation(config, "spectator"));

        ConfigurationSection teamsSection = config.getConfigurationSection("teams");
        if (teamsSection != null) {
            for (String teamName : teamsSection.getKeys(false)) {
                TeamColor color = TeamColor.valueOf(teamName.toUpperCase());
                int teamSize = config.getInt("teams." + teamName + ".maxPlayers");

                BedWarsTeam team = new BedWarsTeam(color, teamSize);
                team.setSpawn(getLocation(config, "teams." + teamName + ".spawn"));
                team.setBedLocation(getLocation(config, "teams." + teamName + ".bed"));
                team.setShopVillager(getLocation(config, "teams." + teamName + ".shop"));

                arena.getTeams().put(teamName.toUpperCase(), team);
            }
        }

        ConfigurationSection genSection = config.getConfigurationSection("generators");
        if (genSection != null) {
            for (String genKey : genSection.getKeys(false)) {
                String typeStr = config.getString("generators." + genKey + ".type");
                Location loc = getLocation(config, "generators." + genKey + ".location");

                if (typeStr != null && loc != null) {
                    try {
                        Arena.GeneratorType type = Arena.GeneratorType.valueOf(typeStr);
                        plugin.getGeneratorManager().createGenerator(arena, loc, type);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Ungültiger Generator-Typ: " + typeStr);
                    }
                }
            }
        }

        plugin.getGameManager().addArena(arena);
    }

    public void saveArena(Arena arena) {
        File file = new File(arenasFolder, arena.getName() + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        config.set("name", arena.getName());
        config.set("displayName", arena.getDisplayName());
        config.set("minPlayers", arena.getMinPlayers());
        config.set("maxPlayers", arena.getMaxPlayers());
        config.set("teamsPerGame", arena.getTeams().size());
        config.set("gameMode", arena.getGameMode().name());

        setLocation(config, "lobby", arena.getLobby());
        setLocation(config, "spectator", arena.getSpectator());

        for (Map.Entry<String, BedWarsTeam> entry : arena.getTeams().entrySet()) {
            String teamName = entry.getKey().toLowerCase();
            BedWarsTeam team = entry.getValue();

            config.set("teams." + teamName + ".maxPlayers", team.getMaxPlayers());
            setLocation(config, "teams." + teamName + ".spawn", team.getSpawn());
            setLocation(config, "teams." + teamName + ".bed", team.getBedLocation());
            setLocation(config, "teams." + teamName + ".shop", team.getShopVillager());
        }

        int genIndex = 0;
        for (Map.Entry<Location, Arena.GeneratorData> entry : arena.getGenerators().entrySet()) {
            Arena.GeneratorData gen = entry.getValue();
            String genKey = "gen" + genIndex;

            config.set("generators." + genKey + ".type", gen.getType().name());
            setLocation(config, "generators." + genKey + ".location", gen.getLocation());

            genIndex++;
        }

        try {
            config.save(file);
            plugin.getLogger().info("Arena " + arena.getName() + " gespeichert!");
        } catch (Exception e) {
            plugin.getLogger().severe("Fehler beim Speichern der Arena " + arena.getName());
            e.printStackTrace();
        }
    }

    private Location getLocation(YamlConfiguration config, String path) {
        if (!config.contains(path)) return null;

        return new Location(
                plugin.getServer().getWorld(config.getString(path + ".world")),
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw"),
                (float) config.getDouble(path + ".pitch")
        );
    }

    private void setLocation(YamlConfiguration config, String path, Location loc) {
        if (loc == null) return;

        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());
    }

    public Collection<Arena> getArenas() {
        return plugin.getGameManager().getArenas();
    }

    public Arena getArenaByWorld(org.bukkit.World world) {
        for (Arena arena : getArenas()) {
            if (arena.getLobby() != null && arena.getLobby().getWorld().equals(world)) {
                return arena;
            }
            if (arena.getSpectator() != null && arena.getSpectator().getWorld().equals(world)) {
                return arena;
            }
            for (me.leon.bedwars.managers.TeamManager.BedWarsTeam team : arena.getTeams().values()) {
                if (team.getSpawn() != null && team.getSpawn().getWorld().equals(world)) {
                    return arena;
                }
            }
        }
        return null;
    }
}