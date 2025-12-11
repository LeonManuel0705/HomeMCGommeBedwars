package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.managers.ShopManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {

    private final BedWars plugin;

    public InventoryClickListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getInventory().getTitle();

        if (title.equals("§8Shop")) {
            event.setCancelled(true);
            handleShopClick(player, event);
        } else if (title.startsWith("§8Shop - ")) {
            event.setCancelled(true);
            handleCategoryClick(player, event);
        } else if (title.equals("§8Achievements")) {
            event.setCancelled(true);
        } else if (title.equals("§8Gadget Shop")) {
            event.setCancelled(true);
            handleGadgetShopClick(player, event);
        }
    }

    private void handleShopClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        String displayName = clicked.getItemMeta().getDisplayName();

        ShopManager.ShopCategory category = null;
        if (displayName.contains("Blöcke")) category = plugin.getShopManager().getCategory("blocks");
        else if (displayName.contains("Waffen")) category = plugin.getShopManager().getCategory("weapons");
        else if (displayName.contains("Rüstung")) category = plugin.getShopManager().getCategory("armor");
        else if (displayName.contains("Werkzeuge")) category = plugin.getShopManager().getCategory("tools");
        else if (displayName.contains("Essen")) category = plugin.getShopManager().getCategory("food");
        else if (displayName.contains("Spezial")) category = plugin.getShopManager().getCategory("special");

        if (category != null) {
            String categoryKey = "";
            if (displayName.contains("Blöcke")) categoryKey = "blocks";
            else if (displayName.contains("Waffen")) categoryKey = "weapons";
            else if (displayName.contains("Rüstung")) categoryKey = "armor";
            else if (displayName.contains("Werkzeuge")) categoryKey = "tools";
            else if (displayName.contains("Essen")) categoryKey = "food";
            else if (displayName.contains("Spezial")) categoryKey = "special";

            plugin.getShopManager().openCategory(player, categoryKey);
        }
    }

    private void handleCategoryClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        if (clicked.getType() == Material.ARROW) {
            Game game = plugin.getGameManager().getPlayerGame(player);
            if (game != null) {
                plugin.getShopManager().openShop(player, game);
            }
            return;
        }

        String categoryName = event.getInventory().getTitle().replace("§8Shop - ", "");
        ShopManager.ShopCategory category = null;

        for (String key : new String[]{"blocks", "weapons", "armor", "tools", "food", "special"}) {
            ShopManager.ShopCategory cat = plugin.getShopManager().getCategory(key);
            if (cat != null && cat.getName().equals(categoryName)) {
                category = cat;
                break;
            }
        }

        if (category != null) {
            for (ShopManager.ShopItem item : category.getItems()) {
                if (item.getDisplay().isSimilar(clicked)) {
                    plugin.getShopManager().purchase(player, item);
                    return;
                }
            }
        }
    }

    private void handleGadgetShopClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String displayName = clicked.getItemMeta().getDisplayName();

        for (String gadgetId : new String[]{
                "FIREWORK_PARTY", "LIGHTNING_STRIKE", "ENDER_DRAGON",
                "PARTICLE_HEARTS", "PARTICLE_FLAME", "PARTICLE_MAGIC",
                "KILL_MSG_BOOM", "KILL_MSG_RIP", "KILL_MSG_OWNED",
                "BED_DESTROY_EXPLOSION", "BED_DESTROY_WITHER", "DANCE_MOVES"
        }) {
            me.leon.bedwars.managers.GadgetManager.Gadget gadget = plugin.getGadgetManager().getGadget(gadgetId);
            if (gadget != null && displayName.contains(gadget.getName().replace("§", "").substring(1))) {

                if (plugin.getGadgetManager().ownsGadget(player.getUniqueId(), gadgetId)) {
                    plugin.getGadgetManager().selectGadget(player, gadgetId);
                } else {
                    plugin.getGadgetManager().purchaseGadget(player, gadgetId);
                }

                player.closeInventory();
                org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    plugin.getGadgetManager().openGadgetShop(player);
                }, 2L);
                break;
            }
        }
    }
}