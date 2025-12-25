package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class NPCClickListener implements Listener {

    private final BedWars plugin;

    public NPCClickListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();

        if (!(entity instanceof Villager)) return;

        Villager villager = (Villager) entity;

        if (villager.hasMetadata("bw_join_npc")) {
            event.setCancelled(true);
            plugin.getNPCManager().handleNPCClick(event.getPlayer(), villager);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Villager)) return;

        Villager villager = (Villager) event.getEntity();

        if (villager.hasMetadata("bw_join_npc")) {
            event.setCancelled(true);

            if (event.getDamager() instanceof Player) {
                plugin.getNPCManager().handleNPCClick((Player) event.getDamager(), villager);
            }
        }
    }
}
