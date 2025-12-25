package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import java.util.UUID;

public class EntityDamageListener implements Listener {

    private final BedWars plugin;

    public EntityDamageListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        UUID uuid = player.getUniqueId();

        if (plugin.getBuildManager().isInBuildMode(uuid)) {
            return;
        }

        // Check for god mode in private rounds
        if (plugin.getPrivateCommand() != null && plugin.getPrivateCommand().isInGodMode(uuid)) {
            event.setCancelled(true);
            return;
        }

        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());

        if (arena == null) {
            event.setCancelled(true);
            return;
        }

        if (arena.getGameState() != GameState.INGAME) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        Arena arena = plugin.getGameManager().getArenaByPlayer(victim.getUniqueId());

        if (arena == null || arena.getGameState() != GameState.INGAME) {
            event.setCancelled(true);
            return;
        }

        BedWarsTeam victimTeam = getPlayerTeam(arena, victim);
        BedWarsTeam damagerTeam = getPlayerTeam(arena, damager);

        if (victimTeam != null && victimTeam == damagerTeam) {
            event.setCancelled(true);
            damager.sendMessage("§cDu kannst deine Teammitglieder nicht angreifen!");
        }
    }

    private BedWarsTeam getPlayerTeam(Arena arena, Player player) {
        for (BedWarsTeam team : arena.getTeams().values()) {
            if (team.getPlayers().contains(player.getUniqueId())) {
                return team;
            }
        }
        return null;
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());

        if (arena == null) {
            if (!plugin.getBuildManager().isInBuildMode(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}
