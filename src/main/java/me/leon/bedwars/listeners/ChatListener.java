package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.managers.TeamData;
import me.leon.core.utils.CoreAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final BedWars plugin;

    public ChatListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game == null) return;

        event.setCancelled(true);

        TeamData team = game.getPlayerTeam(player.getUniqueId());
        String teamColor = team != null ? team.getColor() : "§7";

        String prefix = "";
        if (CoreAPI.isCoreAvailable()) {
            prefix = CoreAPI.getRankPrefix(player) + " ";
        }

        String format = prefix + teamColor + player.getName() + " §8» §7" + event.getMessage();

        game.broadcast(format);
    }
}