package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PotionListener implements Listener {

    private final BedWars plugin;
    private final Map<UUID, BukkitTask> invisibilityTasks = new HashMap<>();

    public PotionListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPotionConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());

        if (arena == null || arena.getGameState() != GameState.INGAME) return;

        ItemStack item = event.getItem();
        if (item.getType() != Material.POTION) return;

        short data = item.getDurability();

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            applyPotionEffect(player, arena, data);
        }, 1L);
    }

    private void applyPotionEffect(Player player, Arena arena, short data) {
        switch (data) {
            case 8206:
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 30 * 20, 0));
                hideNametag(player, arena, 30 * 20);
                break;
            case 8194:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 90 * 20, 0));
                break;
            case 8203:
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 90 * 20, 0));
                break;
            case 8261:
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 8));
                break;
            case 8197:
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 4));
                break;
            case 8201:
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 60 * 20, 0));
                break;
        }
    }

    private void hideNametag(Player player, Arena arena, int durationTicks) {
        if (invisibilityTasks.containsKey(player.getUniqueId())) {
            invisibilityTasks.get(player.getUniqueId()).cancel();
        }

        plugin.getNametagManager().hideNametag(player);

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && arena.getPlayers().contains(player.getUniqueId())) {
                BedWarsTeam team = getPlayerTeam(arena, player);
                if (team != null && !arena.getDeadPlayers().contains(player.getUniqueId())) {
                    plugin.getNametagManager().setGameNametag(player, team);
                }
            }
            invisibilityTasks.remove(player.getUniqueId());
        }, durationTicks);

        invisibilityTasks.put(player.getUniqueId(), task);
    }

    private BedWarsTeam getPlayerTeam(Arena arena, Player player) {
        for (BedWarsTeam team : arena.getTeams().values()) {
            if (team.getPlayers().contains(player.getUniqueId())) {
                return team;
            }
        }
        return null;
    }
}
