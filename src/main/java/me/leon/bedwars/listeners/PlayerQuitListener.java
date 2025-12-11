package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final BedWars plugin;

    public PlayerQuitListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        Player player = event.getPlayer();

        if (plugin.getGameManager().isInGame(player)) {
            plugin.getGameManager().leaveGame(player);
        }

        plugin.getStatsManager().unloadPlayer(player.getUniqueId());
        plugin.getAchievementManager().unloadPlayer(player.getUniqueId());
        plugin.getGadgetManager().unloadPlayer(player.getUniqueId());
    }
}