package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
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

        plugin.getStatsManager().loadPlayer(event.getPlayer());
        plugin.getAchievementManager().loadPlayer(event.getPlayer().getUniqueId());
        plugin.getGadgetManager().loadPlayer(event.getPlayer().getUniqueId());

        plugin.getLobbyManager().sendToLobby(event.getPlayer());
    }
}