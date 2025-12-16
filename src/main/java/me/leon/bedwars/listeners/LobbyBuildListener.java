package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;

public class LobbyBuildListener implements Listener {

    private final BedWars plugin;

    public LobbyBuildListener(BedWars plugin) {
        this.plugin = plugin;
    }

    private boolean isInLobby(Player player) {
        return plugin.getBedWarsLobby().isInLobby(player.getUniqueId());
    }

    private boolean isInBuildMode(Player player) {
        return plugin.getBedWarsLobby().isInBuildMode(player.getUniqueId());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isInLobby(player)) return;
        if (!isInBuildMode(player)) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!isInLobby(player)) return;
        if (!isInBuildMode(player)) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!isInLobby(player)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!isInLobby(player)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!isInLobby(player)) return;
        if (!isInBuildMode(player)) event.setCancelled(true);
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (!isInLobby(player)) return;
        if (!isInBuildMode(player)) event.setCancelled(true);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) event.setCancelled(true);
    }

    @EventHandler
    public void onDamageOther(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (isInLobby(player) && !isInBuildMode(player)) event.setCancelled(true);
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        Player player = event.getPlayer();
        if (isInLobby(player) && !isInBuildMode(player)) event.setCancelled(true);
    }

    @EventHandler
    public void onItemFrameDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ItemFrame)) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player damager = (Player) event.getDamager();
        if (isInLobby(damager) && !isInBuildMode(damager)) event.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player)) return;
        Player remover = (Player) event.getRemover();
        if (isInLobby(remover) && !isInBuildMode(remover)) event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isInLobby(player)) return;
        if (isInBuildMode(player)) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem() != null && event.getItem().getType() == Material.BOW) return;
        }

        if (event.getClickedBlock() != null) {
            Material type = event.getClickedBlock().getType();
            if (type == Material.STONE_BUTTON || type == Material.WOOD_BUTTON) return;

            if (type == Material.CHEST || type == Material.TRAPPED_CHEST ||
                    type == Material.FURNACE || type == Material.BEACON ||
                    type == Material.DROPPER || type == Material.DISPENSER) {
                event.setCancelled(true);
                return;
            }
        }

        event.setCancelled(true);
    }
}
