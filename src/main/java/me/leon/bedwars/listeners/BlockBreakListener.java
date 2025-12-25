package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.UUID;

public class BlockBreakListener implements Listener {

    private final BedWars plugin;

    public BlockBreakListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());
        UUID UUID = player.getUniqueId();

        if (plugin.getBuildManager().isInBuildMode(UUID)) {
            return;
        }

        if (arena == null) return;

        if (arena.getGameState() != GameState.INGAME) {
            event.setCancelled(true);
            return;
        }

        if (!arena.getPlacedBlocks().contains(event.getBlock().getLocation())) {
            if (event.getBlock().getType() == Material.BED_BLOCK) {
                handleBedBreak(event, arena, player);
                return;
            }

            event.setCancelled(true);
            player.sendMessage("§cDu kannst nur von Spielern platzierte Blöcke und gegnerische Betten abbauen!");
            return;
        }

        arena.getPlacedBlocks().remove(event.getBlock().getLocation());
    }

    private void handleBedBreak(BlockBreakEvent event, Arena arena, Player player) {
        event.setCancelled(true);

        BedWarsTeam playerTeam = getPlayerTeam(arena, player);
        if (playerTeam == null) return;

        for (BedWarsTeam team : arena.getTeams().values()) {
            if (team.getBedLocation() != null &&
                    isNearBed(event.getBlock().getLocation(), team.getBedLocation())) {

                if (team == playerTeam) {
                    player.sendMessage("§cDu kannst dein eigenes Bett nicht zerstören!");
                    return;
                }

                team.destroyBed(player);

                // Remove both parts of the bed without dropping items
                Block bedBlock = event.getBlock();
                removeBedBlocks(bedBlock);

                // Play custom bed destroy sound from cosmetics
                plugin.getCosmeticsManager().playBedDestroySound(player, bedBlock.getLocation());

                plugin.getStatsManager().addBedDestroy(player.getUniqueId());

                return;
            }
        }
    }

    private void removeBedBlocks(Block block) {
        // Set the block to air first
        block.setType(Material.AIR, false);

        // Check all adjacent blocks for the other half of the bed
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
        for (BlockFace face : faces) {
            Block relative = block.getRelative(face);
            if (relative.getType() == Material.BED_BLOCK) {
                relative.setType(Material.AIR, false);
            }
        }
    }

    private boolean isNearBed(org.bukkit.Location block, org.bukkit.Location bed) {
        return block.distance(bed) <= 2;
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