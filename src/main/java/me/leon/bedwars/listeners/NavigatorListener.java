package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.managers.MapData;
import me.leon.core.utils.CoreAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class NavigatorListener implements Listener {

    private final BedWars plugin;

    public NavigatorListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getInventory().getTitle();

        if (!title.equals("§8Spiel wählen")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String displayName = clicked.getItemMeta().getDisplayName();

        player.closeInventory();

        if (displayName.contains("Zufällige Map")) {
            MapData randomMap = plugin.getMapManager().getRandomMap();
            if (randomMap == null) {
                player.sendMessage(BedWars.PREFIX + "§cKeine Maps verfügbar!");
                return;
            }

            joinMap(player, randomMap);
            return;
        }

        // Nach Map-Namen suchen
        for (MapData map : plugin.getMapManager().getAllMaps()) {
            if (displayName.contains(map.getName())) {
                joinMap(player, map);
                return;
            }
        }
    }

    private void joinMap(Player player, MapData mapData) {
        plugin.getBedWarsLobby().removePlayer(player);

        if (CoreAPI.isPartyManagerAvailable() && CoreAPI.isInParty(player.getUniqueId())) {
            plugin.getPartyIntegration().joinGameWithParty(player, mapData);
        } else {
            Game game = plugin.getGameManager().getAvailableGame(mapData);
            plugin.getGameManager().joinGame(player, game);
        }
    }
}