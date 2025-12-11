package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import org.bukkit.event.Listener;

public class PlayerPickupItemListener implements Listener {
    private final BedWars plugin;

    public PlayerPickupItemListener(BedWars plugin) {
        this.plugin = plugin;
    }
}