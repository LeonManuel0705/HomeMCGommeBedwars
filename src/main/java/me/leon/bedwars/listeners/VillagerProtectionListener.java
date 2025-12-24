package me.leon.bedwars.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.util.Vector;

public class VillagerProtectionListener implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (isProtectedVillager(e.getEntity())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent e) {
        if (isProtectedVillager(e.getEntity())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (isProtectedVillager(e.getEntity())) {
            e.getEntity().setVelocity(new Vector(0, 0, 0));
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if (isProtectedVillager(e.getRightClicked())) {
            e.setCancelled(true);
        }
    }

    private boolean isProtectedVillager(Entity entity) {
        return entity instanceof Villager &&
                (entity.hasMetadata("bw_shop") || entity.hasMetadata("bw_npc"));
    }
}