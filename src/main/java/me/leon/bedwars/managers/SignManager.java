package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SignManager {

    private final BedWars plugin;
    private final Map<Location, String> arenaSigns = new HashMap<>();
    private final File signsFile;
    private BukkitTask updateTask;

    public SignManager(BedWars plugin) {
        this.plugin = plugin;
        this.signsFile = new File(plugin.getDataFolder(), "signs.yml");
        loadSigns();
        startUpdateTask();
    }

    public void createSign(Location location, String arenaName) {
        arenaSigns.put(location, arenaName);
        saveSigns();
        updateSign(location);
    }

    public void removeSign(Location location) {
        arenaSigns.remove(location);
        saveSigns();
    }

    public boolean isArenaSign(Location location) {
        return arenaSigns.containsKey(location);
    }

    public String getArenaName(Location location) {
        return arenaSigns.get(location);
    }

    public void handleSignClick(Player player, Location signLocation) {
        String arenaName = arenaSigns.get(signLocation);
        if (arenaName == null) return;

        Arena arena = plugin.getGameManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage(BedWars.PREFIX + "§cDiese Arena existiert nicht mehr!");
            return;
        }

        plugin.getGameManager().joinArena(player, arenaName);
    }

    private void updateSign(Location location) {
        Block block = location.getBlock();
        if (!(block.getState() instanceof Sign)) {
            arenaSigns.remove(location);
            return;
        }

        Sign sign = (Sign) block.getState();
        String arenaName = arenaSigns.get(location);

        Arena arena = plugin.getGameManager().getArena(arenaName);
        if (arena == null) {
            sign.setLine(0, "§4§l[FEHLER]");
            sign.setLine(1, "§cArena nicht");
            sign.setLine(2, "§cgefunden");
            sign.setLine(3, "");
            sign.update();
            return;
        }

        String statusColor;
        String statusText;
        switch (arena.getGameState()) {
            case WAITING:
                statusColor = "§a";
                statusText = "WARTEN";
                break;
            case STARTING:
                statusColor = "§e";
                statusText = "STARTET";
                break;
            case INGAME:
                statusColor = "§6";
                statusText = "LÄUFT";
                break;
            case ENDING:
                statusColor = "§c";
                statusText = "BEENDET";
                break;
            default:
                statusColor = "§7";
                statusText = "OFFLINE";
        }

        sign.setLine(0, "§1§l[BedWars]");
        sign.setLine(1, "§f" + arena.getDisplayName());
        sign.setLine(2, statusColor + statusText);
        sign.setLine(3, "§7" + arena.getPlayers().size() + "§8/§7" + arena.getMaxPlayers());
        sign.update();
    }

    public void updateAllSigns() {
        for (Location location : arenaSigns.keySet()) {
            updateSign(location);
        }
    }

    private void startUpdateTask() {
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAllSigns, 20L, 40L);
    }

    private void loadSigns() {
        if (!signsFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(signsFile);

        for (String key : config.getKeys(false)) {
            try {
                String world = config.getString(key + ".world");
                double x = config.getDouble(key + ".x");
                double y = config.getDouble(key + ".y");
                double z = config.getDouble(key + ".z");
                String arenaName = config.getString(key + ".arena");

                org.bukkit.World bukkitWorld = plugin.getServer().getWorld(world);
                if (bukkitWorld != null) {
                    Location loc = new Location(bukkitWorld, x, y, z);
                    arenaSigns.put(loc, arenaName);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load sign: " + key);
            }
        }

        plugin.getLogger().info("Loaded " + arenaSigns.size() + " arena signs");
    }

    private void saveSigns() {
        YamlConfiguration config = new YamlConfiguration();

        int index = 0;
        for (Map.Entry<Location, String> entry : arenaSigns.entrySet()) {
            Location loc = entry.getKey();
            String key = "sign" + index;

            config.set(key + ".world", loc.getWorld().getName());
            config.set(key + ".x", loc.getX());
            config.set(key + ".y", loc.getY());
            config.set(key + ".z", loc.getZ());
            config.set(key + ".arena", entry.getValue());

            index++;
        }

        try {
            config.save(signsFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save signs: " + e.getMessage());
        }
    }

    public Map<Location, String> getSigns() {
        return arenaSigns;
    }
}
