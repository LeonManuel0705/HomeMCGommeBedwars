package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.game.GameState;
import me.leon.bedwars.managers.TeamData;
import me.leon.bedwars.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TeamSelectorListener implements Listener {

    private final BedWars plugin;

    public TeamSelectorListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.BED) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
        if (!item.getItemMeta().getDisplayName().contains("Team Auswahl")) return;

        event.setCancelled(true);

        Game game = plugin.getGameManager().getPlayerGame(player);
        if (game == null || game.getState() != GameState.LOBBY) return;

        openTeamSelector(player, game);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getInventory().getTitle();

        if (!title.equals("§8Team Auswahl")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        Game game = plugin.getGameManager().getPlayerGame(player);
        if (game == null || game.getState() != GameState.LOBBY) return;

        String displayName = clicked.getItemMeta().getDisplayName();

        for (TeamData team : game.getMapData().getTeams()) {
            if (displayName.contains(team.getName())) {
                game.setPlayerTeam(player, team);
                player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
                player.closeInventory();

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    openTeamSelector(player, game);
                }, 2L);
                break;
            }
        }

        if (displayName.contains("Zufälliges Team")) {
            List<TeamData> teams = game.getMapData().getTeams();
            TeamData randomTeam = teams.get((int) (Math.random() * teams.size()));
            game.setPlayerTeam(player, randomTeam);
            player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
            player.closeInventory();
        }
    }

    public void openTeamSelector(Player player, Game game) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Team Auswahl");

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (short) 7)
                    .setDisplayName(" ")
                    .build());
            inv.setItem(45 + i, new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (short) 7)
                    .setDisplayName(" ")
                    .build());
        }

        int slot = 19;
        for (TeamData team : game.getMapData().getTeams()) {
            int memberCount = 0;
            for (Player p : game.getOnlinePlayers()) {
                if (team.equals(game.getPlayerTeam(p.getUniqueId()))) {
                    memberCount++;
                }
            }

            boolean isPlayerInTeam = team.equals(game.getPlayerTeam(player.getUniqueId()));

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Spieler: §e" + memberCount + "§7/§e" + (game.getMapData().getMaxPlayers() / game.getMapData().getTeams().size()));
            lore.add("");

            if (memberCount > 0) {
                lore.add("§7Mitglieder:");
                for (Player p : game.getOnlinePlayers()) {
                    if (team.equals(game.getPlayerTeam(p.getUniqueId()))) {
                        lore.add("  §8• §7" + p.getName());
                    }
                }
                lore.add("");
            }

            if (isPlayerInTeam) {
                lore.add("§a§lDEIN TEAM");
            } else {
                lore.add("§eKlicke zum Beitreten!");
            }

            short woolColor = getWoolColor(team.getChatColor());

            ItemStack teamItem = new ItemBuilder(Material.WOOL, 1, woolColor)
                    .setDisplayName(team.getColor() + "§l" + team.getName())
                    .setLore(lore.toArray(new String[0]))
                    .build();

            inv.setItem(slot, teamItem);
            slot += 2;
        }

        ItemStack randomTeam = new ItemBuilder(Material.ENDER_PEARL)
                .setDisplayName("§dZufälliges Team")
                .setLore(
                        "",
                        "§7Wähle ein zufälliges Team!",
                        "",
                        "§eKlicke zum Auswählen!"
                )
                .build();
        inv.setItem(49, randomTeam);

        player.openInventory(inv);
    }

    private short getWoolColor(org.bukkit.ChatColor chatColor) {
        switch (chatColor) {
            case RED: return DyeColor.RED.getWoolData();
            case BLUE: return DyeColor.BLUE.getWoolData();
            case GREEN: return DyeColor.GREEN.getWoolData();
            case YELLOW: return DyeColor.YELLOW.getWoolData();
            case AQUA: return DyeColor.CYAN.getWoolData();
            case LIGHT_PURPLE: return DyeColor.MAGENTA.getWoolData();
            case GRAY: return DyeColor.GRAY.getWoolData();
            case WHITE: return DyeColor.WHITE.getWoolData();
            default: return DyeColor.WHITE.getWoolData();
        }
    }
}