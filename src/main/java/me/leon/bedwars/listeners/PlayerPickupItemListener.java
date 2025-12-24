package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class PlayerPickupItemListener implements Listener {

    private final BedWars plugin;

    public PlayerPickupItemListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());

        if (arena == null) {
            if (!plugin.getBuildManager().isInBuildMode(player.getUniqueId())) {
                event.setCancelled(true);
            }
            return;
        }

        if (arena.getGameState() != GameState.INGAME) {
            event.setCancelled(true);
            return;
        }

        Material type = event.getItem().getItemStack().getType();

        if (type == Material.CLAY_BRICK ||
                type == Material.IRON_INGOT ||
                type == Material.GOLD_INGOT) {
            return;
        }
    }
}
