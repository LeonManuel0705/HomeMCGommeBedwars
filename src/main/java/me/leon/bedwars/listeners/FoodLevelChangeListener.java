package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodLevelChangeListener implements Listener {
    private final BedWars plugin;

    public FoodLevelChangeListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
}