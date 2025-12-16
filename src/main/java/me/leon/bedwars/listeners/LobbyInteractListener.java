package me.leon.bedwars.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.leon.bedwars.BedWars;
import me.leon.bedwars.managers.MapData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LobbyInteractListener implements Listener {

    private final BedWars plugin;

    public LobbyInteractListener(BedWars plugin) {
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

        // Nur in der BedWars-Lobby
        if (!plugin.getBedWarsLobby().isInLobby(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);

        String displayName = item.getItemMeta().getDisplayName();

        if (displayName.contains("Navigator")) {
            openNavigator(player);
        } else if (displayName.contains("Statistiken")) {
            player.performCommand("stats");
        } else if (displayName.contains("Achievements")) {
            plugin.getAchievementManager().openAchievementsGUI(player);
        } else if (displayName.contains("Gadget Shop")) {
            plugin.getGadgetManager().openGadgetShop(player);
        } else if (displayName.contains("Zurück zur Lobby")) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF("lobby");
            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        }
    }

    private void openNavigator(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Spiel wählen");

        int slot = 10;
        for (MapData map : plugin.getMapManager().getAllMaps()) {
            int runningGames = 0;
            for (me.leon.bedwars.game.Game game : plugin.getGameManager().getAllGames()) {
                if (game.getMapData().equals(map)) {
                    runningGames++;
                }
            }

            ItemStack item = new me.leon.bedwars.utils.ItemBuilder(Material.MAP)
                    .setDisplayName("§e" + map.getName())
                    .setLore(
                            "§7Spieler: §e" + map.getMinPlayers() + "-" + map.getMaxPlayers(),
                            "§7Laufende Spiele: §e" + runningGames,
                            "",
                            "§eKlicke zum Beitreten!"
                    )
                    .build();

            inv.setItem(slot++, item);
        }

        ItemStack random = new me.leon.bedwars.utils.ItemBuilder(Material.ENDER_PEARL)
                .setDisplayName("§dZufällige Map")
                .setLore("§7Trete einem zufälligen Spiel bei!")
                .build();
        inv.setItem(16, random);

        player.openInventory(inv);
    }
}