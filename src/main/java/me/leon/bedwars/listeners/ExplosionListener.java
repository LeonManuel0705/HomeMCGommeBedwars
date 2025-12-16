package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;

public class ExplosionListener implements Listener {
    private final BedWars plugin;

    public ExplosionListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        Location loc = event.getLocation();

        Game game = null;
        for (Game g : plugin.getGameManager().getAllGames()) {
            if (g.getMapData().getLobby().getWorld().equals(loc.getWorld())) {
                game = g;
                break;
            }
        }

        if (game == null) {
            event.setCancelled(true);
            return;
        }

        Iterator<Block> iterator = event.blockList().iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (!game.isPlacedBlock(block)) {
                iterator.remove();
            } else {
                game.removeTrackedBlock(block);
            }
        }
    }
}