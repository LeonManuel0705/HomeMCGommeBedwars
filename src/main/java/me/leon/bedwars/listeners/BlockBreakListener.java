package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.*;

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

                event.getBlock().setType(Material.AIR);
                if (event.getBlock().getRelative(0, 1, 0).getType() == Material.BED_BLOCK) {
                    event.getBlock().getRelative(0, 1, 0).setType(Material.AIR);
                }

                plugin.getStatsManager().addBedDestroy(player.getUniqueId());

                return;
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