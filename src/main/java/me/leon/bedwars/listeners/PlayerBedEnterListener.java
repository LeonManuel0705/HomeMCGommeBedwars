package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class PlayerBedEnterListener implements Listener {
    private final BedWars plugin;

    public PlayerBedEnterListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        event.setCancelled(true);
    }
}