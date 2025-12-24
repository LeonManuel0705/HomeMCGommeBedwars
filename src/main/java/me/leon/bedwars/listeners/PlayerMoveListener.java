package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final BedWars plugin;

    public PlayerMoveListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());

        if (arena == null) return;

        if (arena.getGameState() == GameState.INGAME) {
            if (player.getLocation().getY() >= 145) {
                Location newLoc = player.getLocation();
                newLoc.setY(144);
                event.setTo(newLoc);
                player.sendMessage("§cDie Luft ist hier oben zu dünn!");
            }

            if (player.getLocation().getY() < 0) {
                player.setHealth(0);
            }
        }
    }
}