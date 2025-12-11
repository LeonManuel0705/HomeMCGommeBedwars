package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.game.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItemListener implements Listener {
    private final BedWars plugin;

    public PlayerDropItemListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game == null || game.getState() != GameState.INGAME) {
            event.setCancelled(true);
        }
    }
}