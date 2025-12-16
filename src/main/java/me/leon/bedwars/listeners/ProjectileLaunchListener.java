package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.game.GameState;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ProjectileLaunchListener implements Listener {
    private final BedWars plugin;

    public ProjectileLaunchListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();

        if (!(projectile.getShooter() instanceof org.bukkit.entity.Player)) {
            return;
        }

        org.bukkit.entity.Player shooter = (org.bukkit.entity.Player) projectile.getShooter();
        Game game = plugin.getGameManager().getPlayerGame(shooter);

        if (game == null || game.getState() != GameState.INGAME) {
            return;
        }

        if (projectile instanceof Arrow) {
            Location loc = projectile.getLocation();
            loc.getWorld().playEffect(loc, Effect.FLAME, 0);
        }
    }
}
