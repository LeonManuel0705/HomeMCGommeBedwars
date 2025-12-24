package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerQuitListener implements Listener {

    private final BedWars plugin;

    public PlayerQuitListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(null);

        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());
        UUID uuid = player.getUniqueId();
        if (arena != null) {
            arena.removePlayer(player, true);
        }

        if (plugin.getBuildManager().isInBuildMode(uuid)) {
            plugin.getBuildManager().removeBuildMode(uuid);
        }
        plugin.getStatsManager().unloadStats(player.getUniqueId());
    }
}