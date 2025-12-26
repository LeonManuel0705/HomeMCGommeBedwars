package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
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

        if (event.getItem() != null && event.getItem().getType() == Material.MAGMA_CREAM &&
                (arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING)) {
            event.setCancelled(true);
            plugin.getGameManager().leaveArena(player);
            return;
        }

        if (event.getItem() != null && event.getItem().getType() == Material.GOLD_INGOT &&
                (arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING)) {
            event.setCancelled(true);
            plugin.getTeamManager().openGoldVoteGUI(player, arena);
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

        if (event.getRightClicked() instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) event.getRightClicked();
            if (armorStand.hasMetadata("bw_shop_npc")) {
                event.setCancelled(true);
                plugin.getShopManager().openShop(player, arena);
            }
        }
    }

    @EventHandler
    public void onArmorStandInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());

        if (arena == null || arena.getGameState() != GameState.INGAME) return;

        if (event.getRightClicked() instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) event.getRightClicked();
            if (armorStand.hasMetadata("bw_shop_npc")) {
                event.setCancelled(true);
                plugin.getShopManager().openShop(player, arena);
            }
        }
    }
}