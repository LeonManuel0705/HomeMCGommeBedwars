package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.*;

import java.util.UUID;

public class EntitySpawnListener implements Listener {

    private final BedWars plugin;

    public EntitySpawnListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL ||
                event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DEFAULT ||
                event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {

            Arena arena = plugin.getArenaManager().getArenaByWorld(
                    event.getLocation().getWorld());

            if (arena != null) {
                if (event.getEntityType() == EntityType.VILLAGER) {
                    return;
                }
                event.setCancelled(true);
            }
        }
    }
}