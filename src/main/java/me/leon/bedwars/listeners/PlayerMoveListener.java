package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import org.bukkit.event.Listener;

public class PlayerMoveListener implements Listener {
    private final BedWars plugin;

    public PlayerMoveListener(BedWars plugin) {
        this.plugin = plugin;
    }
}