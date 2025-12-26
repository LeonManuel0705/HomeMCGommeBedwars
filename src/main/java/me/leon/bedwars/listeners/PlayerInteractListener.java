package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {

    private final BedWars plugin;

    public PlayerInteractListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Handle arena sign clicks
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block block = event.getClickedBlock();
            if (block.getState() instanceof Sign) {
                if (plugin.getSignManager().isArenaSign(block.getLocation())) {
                    event.setCancelled(true);
                    plugin.getSignManager().handleSignClick(player, block.getLocation());
                    return;
                }
            }
        }

        // Skip if in build mode
        if (plugin.getBuildManager().isInBuildMode(player.getUniqueId())) {
            return;
        }

        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());

        if (arena == null) return;

        if (event.getItem() != null && event.getItem().getType() == Material.BED &&
                (arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING)) {

            event.setCancelled(true);
            plugin.getTeamManager().openTeamSelector(player, arena);
            return;
        }

        // Handle leave item (Magma Cream)
        if (event.getItem() != null && event.getItem().getType() == Material.MAGMA_CREAM &&
                (arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING)) {
            event.setCancelled(true);
            plugin.getGameManager().leaveArena(player);
            return;
        }

        if (arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING) {
            if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.BED_BLOCK) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());

        if (arena == null || arena.getGameState() != GameState.INGAME) return;

        if (event.getRightClicked().getType() == EntityType.VILLAGER) {
            org.bukkit.entity.Villager villager = (org.bukkit.entity.Villager) event.getRightClicked();

            if (villager.hasMetadata("bw_shop")) {
                event.setCancelled(true);
                plugin.getShopManager().openShop(player, arena);
            }
        }
    }
}