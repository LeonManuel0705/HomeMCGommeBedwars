package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.game.GameState;
import me.leon.bedwars.managers.TeamData;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {
    private final BedWars plugin;

    public PlayerInteractListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) {
            return;
        }

        Game game = plugin.getGameManager().getPlayerGame(player);

        // Lobby-Items
        if (game != null && game.getState() == GameState.LOBBY) {
            String displayName = item.getItemMeta().getDisplayName();

            if (displayName.contains("Statistiken")) {
                event.setCancelled(true);
                player.performCommand("stats");
            } else if (displayName.contains("Zurück zur Lobby")) {
                event.setCancelled(true);
                player.performCommand("lobby");
            }
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (!(entity instanceof Villager)) {
            return;
        }

        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game == null || game.getState() != GameState.INGAME) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        TeamData playerTeam = game.getPlayerTeam(player.getUniqueId());
        if (playerTeam == null) {
            return;
        }

        Villager villager = (Villager) entity;

        if (isNearLocation(villager.getLocation(), playerTeam.getShopNPC(), 2.0)) {
            plugin.getShopManager().openShop(player, game);
            return;
        }

        if (isNearLocation(villager.getLocation(), playerTeam.getUpgradeNPC(), 2.0)) {
            player.sendMessage(BedWars.PREFIX + "§cUpgrade-System coming soon!");
            // TODO: Upgrade-System implementieren
            return;
        }
    }

    private boolean isNearLocation(org.bukkit.Location loc1, org.bukkit.Location loc2, double distance) {
        if (loc1 == null || loc2 == null) {
            return false;
        }
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return false;
        }
        return loc1.distance(loc2) <= distance;
    }
}