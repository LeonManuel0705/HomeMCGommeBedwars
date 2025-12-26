package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.cosmetics.CosmeticsManager;
import me.leon.bedwars.game.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectileListener implements Listener {

    private final BedWars plugin;
    private final Map<UUID, BukkitTask> arrowTasks = new ConcurrentHashMap<>();

    public ProjectileListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow)) return;

        Arrow arrow = (Arrow) event.getEntity();

        if (!(arrow.getShooter() instanceof Player)) return;

        Player shooter = (Player) arrow.getShooter();
        Arena arena = plugin.getGameManager().getArenaByPlayer(shooter.getUniqueId());

        if (arena == null) return;

        CosmeticsManager.PlayerCosmetics cosmetics = plugin.getCosmeticsManager().getCosmetics(shooter.getUniqueId());
        CosmeticsManager.ArrowTrail trail = cosmetics.getArrowTrail();

        if (trail == CosmeticsManager.ArrowTrail.DEFAULT || trail.getEffect() == null) return;

        Effect effect = trail.getEffect();
        UUID arrowId = arrow.getUniqueId();

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (arrow.isDead() || arrow.isOnGround() || !arrow.isValid()) {
                BukkitTask t = arrowTasks.remove(arrowId);
                if (t != null) t.cancel();
                return;
            }

            arrow.getWorld().playEffect(arrow.getLocation(), effect, 0);
        }, 1L, 1L);

        arrowTasks.put(arrowId, task);
    }
}
