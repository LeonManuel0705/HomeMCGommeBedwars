package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.lobby.BedWarsLobby;
import me.leon.core.Core;
import me.leon.core.utils.CoreAPI;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final BedWars plugin;

    public PlayerJoinListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();

        plugin.getStatsManager().loadPlayer(event.getPlayer());
        plugin.getAchievementManager().loadPlayer(event.getPlayer().getUniqueId());
        plugin.getGadgetManager().loadPlayer(event.getPlayer().getUniqueId());
        player.teleport(plugin.getBedWarsLobby().getLobbySpawn());
        plugin.getBedWarsLobby().giveItems(player);
        if (CoreAPI.isCoreAvailable()) {
            CoreAPI.getCore().getCoinManager().loadPlayer(event.getPlayer());
        } else {
            if (player.hasPermission("bedwars.admin")) {
                player.sendMessage(BedWars.PREFIX + "§c§lCoinsystem konnte nicht geladen werden, Core ist nicht verfügbar!");
            }
        }
        plugin.getBedWarsLobby().addPlayer(event.getPlayer());
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(player.hasPermission("homemc.fly"));
    }
}