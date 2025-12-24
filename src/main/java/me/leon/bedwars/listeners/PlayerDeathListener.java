package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
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
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());

        if (arena == null || arena.getGameState() != GameState.INGAME) return;

        event.setDeathMessage(null);
        event.getDrops().clear();
        event.setDroppedExp(0);

        Player killer = player.getKiller();
        BedWarsTeam playerTeam = getPlayerTeam(arena, player);

        if (playerTeam == null) return;

        String deathMsg = getDeathMessage(arena, player, killer, playerTeam);
        arena.broadcastMessage(deathMsg);

        if (killer != null && killer != player) {
            plugin.getStatsManager().addKill(killer.getUniqueId());
        }
        plugin.getStatsManager().addDeath(player.getUniqueId());

        if (playerTeam.isBedAlive()) {
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
            }, 1L);
        } else {
            arena.getDeadPlayers().add(player.getUniqueId());
            player.sendMessage("§cDu wurdest eliminiert!");

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.spigot().respawn();
                    player.setGameMode(org.bukkit.GameMode.SPECTATOR);

                    if (arena.getSpectator() != null) {
                        player.teleport(arena.getSpectator());
                    } else {
                        player.teleport(playerTeam.getSpawn());
                    }

                    arena.getSpectators().add(player.getUniqueId());
                    plugin.getNametagManager().setSpectatorNametag(player);
                }
            }, 1L);

            arena.broadcastMessage("§b§lFINAL KILL! " + playerTeam.getColor().getColorCode() +
                    getPlayerName(player) + " §7(" + playerTeam.getColoredName() + "§7) wurde eliminiert!");
        }
    }

    private String getDeathMessage(Arena arena, Player player, Player killer, BedWarsTeam playerTeam) {
        String color = playerTeam.getColor().getColorCode();
        String playerName = color + getPlayerName(player);

        if (killer == null) {
            return playerName + " §7ist gestorben";
        }

        BedWarsTeam killerTeam = getPlayerTeam(arena, killer);
        String killerColor = killerTeam != null ? killerTeam.getColor().getColorCode() : "§f";
        String killerName = killerColor + getPlayerName(killer);

        return playerName + " §7wurde von " + killerName + " §7getötet";
    }

    private String getPlayerName(Player player) {
        if (plugin.getCore().getNickManager().isNicked(player.getUniqueId())) {
            return plugin.getCore().getNickManager().getDisplayName(player.getUniqueId());
        }
        return plugin.getCore().getRankManager().getRankColor(player) + player.getName();
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