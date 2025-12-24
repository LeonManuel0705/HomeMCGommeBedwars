package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class BlockPlaceListener implements Listener {

    private final BedWars plugin;

    public BlockPlaceListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Arena arena = plugin.getGameManager().getArenaByPlayer(uuid);

        if (plugin.getBuildManager().isInBuildMode(uuid)) return;

        if (arena == null) return;

        if (arena.getGameState() != GameState.INGAME) {
            event.setCancelled(true);
            return;
        }

        if (event.getBlock().getType() == Material.TNT) {
            event.setCancelled(true);
            org.bukkit.entity.TNTPrimed tnt = event.getBlock().getWorld().spawn(
                    event.getBlock().getLocation().add(0.5, 0, 0.5),
                    org.bukkit.entity.TNTPrimed.class
            );
            tnt.setFuseTicks(40);

            ItemStack item = event.getItemInHand();
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.setItemInHand(null);
            }
            return;
        }

        Location loc = event.getBlock().getLocation();
        loc.setX(loc.getBlockX());
        loc.setY(loc.getBlockY());
        loc.setZ(loc.getBlockZ());

        arena.getPlacedBlocks().add(loc);
    }
}