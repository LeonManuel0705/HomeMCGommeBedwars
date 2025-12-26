package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class PlayerUnnickListener implements Listener {

    private final BedWars plugin;

    public PlayerUnnickListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerUnnick(me.leon.core.events.PlayerUnnickEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Arena arena = plugin.getGameManager().getArenaByPlayer(uuid);
        if (arena == null) return;

        if (arena.getGameState() == GameState.INGAME) {
            BedWarsTeam team = getPlayerTeam(arena, player);

            if (team != null) {
                plugin.getNametagManager().setGameNametag(player, team);
            } else if (arena.getSpectators().contains(uuid)) {
                plugin.getNametagManager().setSpectatorNametag(player);
            }
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
}
