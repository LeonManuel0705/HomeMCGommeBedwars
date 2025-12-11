package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import org.bukkit.event.Listener;

public class ProjectileHitListener implements Listener {
    private final BedWars plugin;

    public ProjectileHitListener(BedWars plugin) {
        this.plugin = plugin;
    }
}