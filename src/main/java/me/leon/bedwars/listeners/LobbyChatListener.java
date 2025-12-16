package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.core.managers.NickManager;
import me.leon.core.utils.CoreAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class LobbyChatListener implements Listener {
    private final BedWars plugin;

    public LobbyChatListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLobbyChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        String prefix;
        String color;
        String name;

        if (player.hasPermission("homemc.chatcolor")) {
            event.setMessage(event.getMessage().replace("&", "§"));
        }

        if (plugin.getNickManager().isNicked(player.getUniqueId())) {
            NickManager.NickData data = plugin.getNickManager().getNickData(player.getUniqueId());
            prefix = "§7";
            color = "§7";
            name = data.nickName;
        } else {
            prefix = plugin.getRankManager().getPrefix(player);
            color = plugin.getRankManager().getRankColor(player);
            name = player.getName();
        }

        String format = prefix + name + plugin.getClanManager().getClanSuffix(player) + " §8» " + color + event.getMessage();
        event.setFormat(format);
    }
}

