package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerDeathListener implements Listener {

    private final BedWars plugin;
    private final Set<UUID> processingDeath = new HashSet<>();

    public PlayerDeathListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());

        if (arena == null || arena.getGameState() != GameState.INGAME) return;

        if (processingDeath.contains(player.getUniqueId())) {
            event.setDeathMessage(null);
            event.getDrops().clear();
            event.setDroppedExp(0);
            return;
        }

        processingDeath.add(player.getUniqueId());

        event.setDeathMessage(null);
        event.getDrops().clear();
        event.setDroppedExp(0);

        Player killer = player.getKiller();
        BedWarsTeam playerTeam = getPlayerTeam(arena, player);

        if (playerTeam == null) {
            processingDeath.remove(player.getUniqueId());
            return;
        }

        if (killer != null && killer != player) {
            plugin.getStatsManager().addKill(killer.getUniqueId());
        }
        plugin.getStatsManager().addDeath(player.getUniqueId());

        if (playerTeam.isBedAlive()) {
            String deathMsg = getDeathMessage(arena, player, killer, playerTeam);
            arena.broadcastMessage(deathMsg);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.spigot().respawn();
                    player.teleport(playerTeam.getSpawn());
                    player.setHealth(20.0);
                    player.setFoodLevel(20);

                    player.setNoDamageTicks(100);
                    player.sendMessage("§aDu wurdest respawnt! §7(5 Sekunden Schutz)");

                    plugin.getNametagManager().setGameNametag(player, playerTeam);
                }
                processingDeath.remove(player.getUniqueId());
            }, 1L);
        } else {
            arena.getDeadPlayers().add(player.getUniqueId());

            arena.broadcastMessage("§b§lFINAL KILL! " + playerTeam.getColor().getColorCode() +
                    getPlayerName(player) + " §7wurde " + (killer != null && killer != player ?
                    "von " + getPlayerTeam(arena, killer).getColor().getColorCode() + getPlayerName(killer) + " §7" : "") + "eliminiert!");

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.spigot().respawn();
                    player.setGameMode(org.bukkit.GameMode.SPECTATOR);

                    if (arena.getSpectator() != null) {
                        player.teleport(arena.getSpectator());
                    } else {
                        player.teleport(playerTeam.getSpawn());
                    }

                    player.sendMessage("§cDu wurdest eliminiert!");
                    arena.getSpectators().add(player.getUniqueId());
                    plugin.getNametagManager().setSpectatorNametag(player);
                }
                processingDeath.remove(player.getUniqueId());
            }, 1L);
        }
    }

    private String getDeathMessage(Arena arena, Player player, Player killer, BedWarsTeam playerTeam) {
        String playerColor = playerTeam.getColor().getColorCode();
        String playerName = getPlayerName(player);

        if (killer == null || killer == player) {
            return plugin.getCosmeticsManager().formatDeathMessage(playerName, playerColor, player.getUniqueId());
        }

        BedWarsTeam killerTeam = getPlayerTeam(arena, killer);
        String killerColor = killerTeam != null ? killerTeam.getColor().getColorCode() : "§f";
        String killerName = getPlayerName(killer);

        return plugin.getCosmeticsManager().formatKillMessage(killerName, killerColor, playerName, playerColor, killer.getUniqueId());
    }

    private String getPlayerName(Player player) {
        return plugin.getDisplayName(player);
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