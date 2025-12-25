package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class PlayerChatListener implements Listener {

    private final BedWars plugin;

    public PlayerChatListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());

        if (arena == null) return;

        event.setCancelled(true);

        if (arena.getGameState() == GameState.INGAME) {
            BedWarsTeam team = getPlayerTeam(arena, player);

            if (team != null) {
                String message = event.getMessage();
                String teamColor = team.getColor().getColorCode();
                String displayName = getDisplayName(player);

                if (message.startsWith("@") || message.startsWith("!")) {
                    // All-chat: show with team color
                    message = message.substring(1);
                    String formatted = "§7[§6ALLE§7] " + teamColor + displayName + " §8» §f" + message;
                    arena.broadcastMessage(formatted);
                } else {
                    // Team chat: show with team color
                    String formatted = "§7[§aTEAM§7] " + teamColor + displayName + " §8» §f" + message;
                    for (UUID uuid : team.getPlayers()) {
                        Player p = plugin.getServer().getPlayer(uuid);
                        if (p != null) {
                            p.sendMessage(formatted);
                        }
                    }
                }
            }
        } else {
            // Waiting lobby: use rank color
            String playerName = getPlayerDisplayNameWithRank(player);
            String formatted = playerName + " §8» §7" + event.getMessage();
            arena.broadcastMessage(formatted);
        }
    }

    private String getDisplayName(Player player) {
        if (plugin.getCore().getNickManager().isNicked(player.getUniqueId())) {
            return plugin.getCore().getNickManager().getDisplayName(player.getUniqueId());
        }
        return player.getName();
    }

    private String getPlayerDisplayNameWithRank(Player player) {
        if (plugin.getCore().getNickManager().isNicked(player.getUniqueId())) {
            return "§7" + plugin.getCore().getNickManager().getDisplayName(player.getUniqueId());
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
