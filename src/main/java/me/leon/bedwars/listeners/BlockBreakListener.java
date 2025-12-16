package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.game.GameState;
import me.leon.bedwars.managers.TeamData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockBreakListener implements Listener {

    private final BedWars plugin;

    public BlockBreakListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game == null) {
            if (!player.hasPermission("bedwars.admin")) {
                event.setCancelled(true);
            }
            return;
        }

        if (game.getState() != GameState.INGAME) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlock();

        if (block.getType() == Material.BED_BLOCK) {
            TeamData breakerTeam = game.getPlayerTeam(player.getUniqueId());

            for (TeamData team : game.getMapData().getTeams()) {
                if (block.getLocation().distance(team.getBed()) < 2) {
                    if (team.equals(breakerTeam)) {
                        player.sendMessage(BedWars.PREFIX + "§cDu kannst dein eigenes Bett nicht zerstören!");
                        event.setCancelled(true);
                        return;
                    }

                    game.destroyBed(team, player);
                    event.setExpToDrop(0);
                    return;
                }
            }
        }

        if (!game.isPlacedBlock(block)) {
            event.setCancelled(true);
            player.sendMessage(BedWars.PREFIX + "§cDu kannst nur von Spielern platzierte Blöcke abbauen!");
            return;
        }

        game.removeTrackedBlock(block);
        event.setExpToDrop(0);
    }
}