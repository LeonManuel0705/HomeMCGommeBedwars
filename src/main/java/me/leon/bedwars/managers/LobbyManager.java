package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;

public class LobbyManager {

    private final BedWars plugin;
    private Location lobbySpawn;

    public LobbyManager(BedWars plugin) {
        this.plugin = plugin;
        loadLobbyLocation();
    }

    private void loadLobbyLocation() {
        if (plugin.getConfig().contains("lobby.world")) {
            String world = plugin.getConfig().getString("lobby.world");
            double x = plugin.getConfig().getDouble("lobby.x");
            double y = plugin.getConfig().getDouble("lobby.y");
            double z = plugin.getConfig().getDouble("lobby.z");
            float yaw = (float) plugin.getConfig().getDouble("lobby.yaw");
            float pitch = (float) plugin.getConfig().getDouble("lobby.pitch");

            lobbySpawn = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
        }
    }

    public void sendToLobby(Player player) {
        if (lobbySpawn != null) {
            player.teleport(lobbySpawn);
        }

        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setFireTicks(0);
        player.setLevel(0);
        player.setExp(0);

        for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    public void setLobbySpawn(Location location) {
        this.lobbySpawn = location;

        plugin.getConfig().set("lobby.world", location.getWorld().getName());
        plugin.getConfig().set("lobby.x", location.getX());
        plugin.getConfig().set("lobby.y", location.getY());
        plugin.getConfig().set("lobby.z", location.getZ());
        plugin.getConfig().set("lobby.yaw", location.getYaw());
        plugin.getConfig().set("lobby.pitch", location.getPitch());
        plugin.saveConfig();
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }
}