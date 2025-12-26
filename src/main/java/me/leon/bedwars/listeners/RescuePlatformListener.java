package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class RescuePlatformListener implements Listener {

    private final BedWars plugin;

    public RescuePlatformListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());

        if (arena == null || arena.getGameState() != GameState.INGAME) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.BLAZE_ROD) return;

        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        String displayName = item.getItemMeta().getDisplayName();
        if (!displayName.contains("Rettungsplattform")) return;

        event.setCancelled(true);

        Location playerLoc = player.getLocation();
        Location platformCenter = playerLoc.clone().subtract(0, 1, 0);

        if (platformCenter.getY() < 1) {
            player.sendMessage("§cDu bist zu tief für eine Rettungsplattform!");
            return;
        }

        BedWarsTeam team = getPlayerTeam(arena, player);
        Material glassMaterial = Material.STAINED_GLASS;
        byte glassData = team != null ? (byte) team.getColor().getData() : 0;

        int blocksPlaced = 0;
        java.util.List<Location> placedLocations = new java.util.ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block block = platformCenter.getWorld().getBlockAt(
                        platformCenter.getBlockX() + x,
                        platformCenter.getBlockY(),
                        platformCenter.getBlockZ() + z
                );

                if (block.getType() == Material.AIR) {
                    block.setType(glassMaterial);
                    block.setData(glassData);
                    Location blockLocation = block.getLocation();
                    arena.getPlacedBlocks().add(blockLocation);
                    placedLocations.add(blockLocation);
                    blocksPlaced++;
                }
            }
        }

        if (blocksPlaced > 0) {
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.setItemInHand(null);
            }

            player.sendMessage("§aRettungsplattform aktiviert!");
            player.playSound(player.getLocation(), org.bukkit.Sound.GLASS, 1.0f, 1.0f);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                for (Location loc : placedLocations) {
                    Block block = loc.getBlock();
                    if (block.getType() == glassMaterial) {
                        block.setType(Material.AIR);
                        arena.getPlacedBlocks().remove(loc);
                    }
                }
            }, 100L);
        } else {
            player.sendMessage("§cKein Platz für die Plattform!");
        }
    }

    private BedWarsTeam getPlayerTeam(Arena arena, Player player) {
        for (BedWarsTeam team : arena.getTeams().values()) {
            if (team.getPlayers().contains(player.getUniqueId())) {
                return team;
            }
        }
        return null;
    }
}