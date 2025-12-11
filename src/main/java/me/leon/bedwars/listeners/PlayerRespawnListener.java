package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.managers.TeamData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {
    private final BedWars plugin;

    public PlayerRespawnListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game == null) return;

        TeamData team = game.getPlayerTeam(player.getUniqueId());
        if (team != null) {
            event.setRespawnLocation(team.getSpawn());
        }
    }
}