package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.game.GameState;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

public class ProjectileHitListener implements Listener {
    private final BedWars plugin;

    public ProjectileHitListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        Player shooter = (Player) event.getEntity().getShooter();
        Game game = plugin.getGameManager().getPlayerGame(shooter);

        if (game == null || game.getState() != GameState.INGAME) {
            return;
        }

        Location hitLoc = event.getEntity().getLocation();

        if (event.getEntity() instanceof Arrow) {
            hitLoc.getWorld().playEffect(hitLoc, Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
            hitLoc.getWorld().playSound(hitLoc, Sound.ARROW_HIT, 0.5f, 1.0f);
        } else if (event.getEntity() instanceof Snowball) {
            hitLoc.getWorld().playEffect(hitLoc, Effect.SNOW_SHOVEL, 0);
            hitLoc.getWorld().playSound(hitLoc, Sound.DIG_SNOW, 1.0f, 1.0f);

            for (Entity nearby : event.getEntity().getNearbyEntities(3, 3, 3)) {
                if (nearby instanceof Player) {
                    Player target = (Player) nearby;
                    if (game.isAlive(target.getUniqueId())) {
                        Vector direction = target.getLocation().toVector()
                                .subtract(hitLoc.toVector())
                                .normalize()
                                .multiply(0.5);
                        direction.setY(0.3);
                        target.setVelocity(direction);
                    }
                }
            }
        }

        for (int i = 0; i < 5; i++) {
            hitLoc.getWorld().playEffect(hitLoc, Effect.CRIT, 0);
        }
    }
}