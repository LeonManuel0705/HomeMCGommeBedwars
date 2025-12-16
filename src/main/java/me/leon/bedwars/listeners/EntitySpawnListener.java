package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

public class EntitySpawnListener implements Listener {
    private final BedWars plugin;

    public EntitySpawnListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL ||
                event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CHUNK_GEN ||
                event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DEFAULT) {

            if (event.getEntityType() != EntityType.VILLAGER) {
                event.setCancelled(true);
            }
        }
    }
}