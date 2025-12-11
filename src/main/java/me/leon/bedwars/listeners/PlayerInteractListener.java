package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import org.bukkit.event.Listener;

public class PlayerInteractListener implements Listener {
    private final BedWars plugin;

    public PlayerInteractListener(BedWars plugin) {
        this.plugin = plugin;
    }
}