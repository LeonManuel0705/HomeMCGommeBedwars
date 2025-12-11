package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.game.GameState;
import me.leon.bedwars.managers.TeamData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityListener implements Listener {
    private final BedWars plugin;

    public EntityDamageByEntityListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        Game game = plugin.getGameManager().getPlayerGame(victim);

        if (game == null || game.getState() != GameState.INGAME) {
            event.setCancelled(true);
            return;
        }

        TeamData victimTeam = game.getPlayerTeam(victim.getUniqueId());
        TeamData damagerTeam = game.getPlayerTeam(damager.getUniqueId());

        if (victimTeam != null && victimTeam.equals(damagerTeam)) {
            event.setCancelled(true);
        }
    }
}