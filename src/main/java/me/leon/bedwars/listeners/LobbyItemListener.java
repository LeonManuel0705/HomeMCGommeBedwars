package me.leon.bedwars.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.game.GameState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class LobbyItemListener implements Listener {

    private final BedWars plugin;

    public LobbyItemListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game == null || game.getState() != GameState.LOBBY) {
            return;
        }

        String displayName = item.getItemMeta().getDisplayName();

        if (displayName.contains("Statistiken")) {
            event.setCancelled(true);
            player.performCommand("stats");
        } else if (displayName.contains("Zurück zur Lobby")) {
            event.setCancelled(true);

            plugin.getGameManager().leaveGame(player);

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF("lobby");

            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        }
    }
}