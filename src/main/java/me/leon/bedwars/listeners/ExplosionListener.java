package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExplosionListener implements Listener {

    private final BedWars plugin;

    public ExplosionListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        Arena arena = plugin.getArenaManager().getArenaByWorld(event.getLocation().getWorld());
        if (arena == null) return;

        if (arena.getGameState() != GameState.INGAME) {
            event.setCancelled(true);
            return;
        }

        Set<String> placedBlockKeys = arena.getPlacedBlocks().stream()
                .map(this::locKey)
                .collect(Collectors.toSet());

        List<Block> toProtect = new ArrayList<>();

        for (Block block : event.blockList()) {
            String key = locKey(block.getLocation());
            if (!placedBlockKeys.contains(key)) {
                toProtect.add(block);
            } else {
                arena.getPlacedBlocks().removeIf(l -> locKey(l).equals(key));
            }
        }

        event.blockList().removeAll(toProtect);

        plugin.getLogger().info("[TNT] Explosion in " + arena.getName() + ": " +
                (event.blockList().size()) + " zerstört, " + toProtect.size() + " geschützt");
    }

    private String locKey(Location loc) {
        return loc.getWorld().getName() + ":" +
                loc.getBlockX() + ":" +
                loc.getBlockY() + ":" +
                loc.getBlockZ();
    }
}
