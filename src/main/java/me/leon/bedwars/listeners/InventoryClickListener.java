package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.lobby.LobbyManager;
import me.leon.bedwars.managers.TeamManager;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
import me.leon.bedwars.managers.TeamManager.TeamColor;
import me.leon.bedwars.shop.ShopManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {

    private final BedWars plugin;

    public InventoryClickListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getInventory().getTitle();

        // Handle game mode selector in lobby (not in arena)
        if (title.equals("§8» §6§lSpielmodus wählen")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.STAINED_GLASS_PANE) {
                return;
            }

            if (event.getCurrentItem().getType() == Material.BED) {
                int slot = event.getSlot();
                LobbyManager.GameMode mode = null;

                if (slot == 11) {
                    mode = LobbyManager.GameMode.SOLO;
                } else if (slot == 13) {
                    mode = LobbyManager.GameMode.DUO;
                } else if (slot == 15) {
                    mode = LobbyManager.GameMode.TEAM;
                }

                if (mode != null) {
                    player.closeInventory();
                    plugin.getNPCManager().joinBestArena(player, mode);
                }
            }
            return;
        }

        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());

        if (arena == null) return;

        if (title.equals("§8Team wählen")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;

            if (event.getCurrentItem().getType() == Material.GOLD_INGOT) {
                arena.voteGold(player, true);
                player.closeInventory();
                return;
            } else if (event.getCurrentItem().getType() == Material.BARRIER) {
                arena.voteGold(player, false);
                player.closeInventory();
                return;
            }

            if (event.getCurrentItem().getType() != Material.WOOL) {
                return;
            }

            short data = event.getCurrentItem().getDurability();
            TeamColor color = getTeamColorByData(data);

            if (color != null) {
                plugin.getTeamManager().joinTeam(player, arena, color);
                player.closeInventory();

                BedWarsTeam team = arena.getTeams().get(color.name());
                if (team != null) {
                    plugin.getNametagManager().setGameNametag(player, team);
                }
            }
        }

        else if (title.equals("§8Shop")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null ||
                    event.getCurrentItem().getType() == Material.STAINED_GLASS_PANE) {
                return;
            }

            Material clickedType = event.getCurrentItem().getType();

            for (ShopManager.ShopCategory cat : ShopManager.ShopCategory.values()) {
                if (clickedType == cat.getIcon()) {
                    plugin.getShopManager().openCategory(player, arena, cat);
                    return;
                }
            }
        }

        else if (title.startsWith("§8Shop §7» ")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;

            if (event.getCurrentItem().getType() == Material.ARROW) {
                if (event.getCurrentItem().hasItemMeta() &&
                        event.getCurrentItem().getItemMeta().hasDisplayName() &&
                        event.getCurrentItem().getItemMeta().getDisplayName().contains("Zurück")) {
                    plugin.getShopManager().openShop(player, arena);
                    return;
                }
            }

            if (event.getCurrentItem().getType() == Material.STAINED_GLASS_PANE) {
                return;
            }

            String categoryName = title.replace("§8Shop §7» ", "");
            ShopManager.ShopCategory category = null;

            for (ShopManager.ShopCategory cat : ShopManager.ShopCategory.values()) {
                if (cat.getDisplayName().equals(categoryName)) {
                    category = cat;
                    break;
                }
            }

            if (category != null) {
                boolean shiftClick = event.isShiftClick();
                plugin.getShopManager().handlePurchase(player, arena, category, event.getSlot(), shiftClick);
            }
        }
    }

    private TeamColor getTeamColorByData(short data) {
        for (TeamColor color : TeamColor.values()) {
            if (color.getData() == data) {
                return color;
            }
        }
        return null;
    }
}