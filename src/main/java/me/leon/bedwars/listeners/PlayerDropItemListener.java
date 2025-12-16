package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.game.GameState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItemListener implements Listener {
    private final BedWars plugin;

    public PlayerDropItemListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player);

        // In Lobby: Kein Item Drop
        if (game == null || game.getState() == GameState.LOBBY) {
            event.setCancelled(true);
            return;
        }

        // Im Spiel: Nur bestimmte Items dürfen gedroppt werden
        if (game.getState() == GameState.INGAME) {
            Material type = event.getItemDrop().getItemStack().getType();

            // Rüstung und Schwerter können nicht gedroppt werden
            if (isArmor(type) || isSword(type)) {
                event.setCancelled(true);
                return;
            }
        } else {
            event.setCancelled(true);
        }
    }

    private boolean isArmor(Material type) {
        return type == Material.LEATHER_HELMET ||
                type == Material.LEATHER_CHESTPLATE ||
                type == Material.LEATHER_LEGGINGS ||
                type == Material.LEATHER_BOOTS ||
                type == Material.CHAINMAIL_HELMET ||
                type == Material.CHAINMAIL_CHESTPLATE ||
                type == Material.CHAINMAIL_LEGGINGS ||
                type == Material.CHAINMAIL_BOOTS ||
                type == Material.IRON_HELMET ||
                type == Material.IRON_CHESTPLATE ||
                type == Material.IRON_LEGGINGS ||
                type == Material.IRON_BOOTS ||
                type == Material.DIAMOND_HELMET ||
                type == Material.DIAMOND_CHESTPLATE ||
                type == Material.DIAMOND_LEGGINGS ||
                type == Material.DIAMOND_BOOTS;
    }

    private boolean isSword(Material type) {
        return type == Material.WOOD_SWORD ||
                type == Material.STONE_SWORD ||
                type == Material.IRON_SWORD ||
                type == Material.DIAMOND_SWORD ||
                type == Material.GOLD_SWORD;
    }
}