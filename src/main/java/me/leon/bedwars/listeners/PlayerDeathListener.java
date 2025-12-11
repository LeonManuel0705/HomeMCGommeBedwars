package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {
    private final BedWars plugin;

    public PlayerDeathListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();

        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game == null) return;

        event.setDeathMessage(null);
        event.getDrops().clear();
        event.setDroppedExp(0);

        game.handlePlayerDeath(player, killer);
    }
}
