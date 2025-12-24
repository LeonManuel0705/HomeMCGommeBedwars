package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class RescuePlatformListener implements Listener {

    private final BedWars plugin;

    public RescuePlatformListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());

        if (arena == null || arena.getGameState() != GameState.INGAME) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.BLAZE_ROD) return;

        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        String displayName = item.getItemMeta().getDisplayName();
        if (!displayName.contains("Rettungsplattform")) return;

        event.setCancelled(true);

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.setItemInHand(null);
        }

        Location playerLoc = player.getLocation();
        Location platformLoc = playerLoc.clone().subtract(0, 1, 0);

        while (platformLoc.getBlock().getType() == Material.AIR && platformLoc.getY() > 0) {
            platformLoc.subtract(0, 1, 0);
        }

        platformLoc.add(0, 1, 0);

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location blockLoc = platformLoc.clone().add(x, 0, z);
                blockLoc.getBlock().setType(Material.GLASS);

                arena.getPlacedBlocks().add(blockLoc);
            }
        }

        player.sendMessage("§aRettungsplattform aktiviert!");
        player.playSound(player.getLocation(), org.bukkit.Sound.GLASS, 1.0f, 1.0f);
    }
}