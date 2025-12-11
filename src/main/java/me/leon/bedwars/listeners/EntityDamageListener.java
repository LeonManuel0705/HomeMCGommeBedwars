package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.game.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {
    private final BedWars plugin;

    public EntityDamageListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game == null) {
            event.setCancelled(true);
            return;
        }

        if (game.getState() == GameState.LOBBY || game.getState() == GameState.PROTECTION) {
            event.setCancelled(true);
        }
    }
}