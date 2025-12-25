package me.leon.bedwars.gui;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.managers.PrivateRoundManager;
import me.leon.bedwars.managers.PrivateRoundManager.PrivateRound;
import me.leon.bedwars.managers.PrivateRoundManager.PrivateSettings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrivateRoundGUI implements Listener {

    private final BedWars plugin;

    public PrivateRoundGUI(BedWars plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, "§8Private Runde");

        PrivateRound round = plugin.getPrivateRoundManager().getPlayerRound(player.getUniqueId());

        if (round == null) {
            inv.setItem(13, createItem(Material.EMERALD_BLOCK, "§a§lRunde erstellen",
                    "§7Klicke um eine Map auszuwählen",
                    "§7und eine private Runde zu erstellen"));

            inv.setItem(31, createItem(Material.COMPASS, "§e§lRunde beitreten",
                    "§7Klicke um einer Einladung",
                    "§7zu folgen"));
        } else {
            if (round.getCreator().equals(player.getUniqueId())) {
                inv.setItem(11, createItem(Material.REDSTONE_COMPARATOR, "§6§lEinstellungen",
                        "§7Passe die Runden-Einstellungen an"));

                inv.setItem(13, createItem(Material.SKULL_ITEM, "§e§lSpieler einladen",
                        "§7Lade Spieler zu deiner Runde ein"));

                inv.setItem(15, createItem(Material.DIAMOND_SWORD, "§a§lRunde starten",
                        "§7Spieler: §e" + round.getPlayers().size(),
                        "§7Klicke zum Starten"));

                inv.setItem(31, createItem(Material.BARRIER, "§c§lRunde löschen",
                        "§7Lösche deine private Runde"));
            } else {
                inv.setItem(13, createItem(Material.BOOK, "§e§lRunden-Info",
                        "§7Zeige Informationen zur Runde"));

                inv.setItem(31, createItem(Material.IRON_DOOR, "§c§lRunde verlassen",
                        "§7Verlasse die private Runde"));
            }
        }

        player.openInventory(inv);
    }

    public void openMapSelection(Player player) {
        List<Arena> arenas = new ArrayList<>(plugin.getArenaManager().getArenas());
        int size = Math.min(54, ((arenas.size() / 9) + 1) * 9);
        if (size < 9) size = 9;

        Inventory inv = Bukkit.createInventory(null, size, "§8Map auswählen");

        int slot = 0;
        for (Arena arena : arenas) {
            if (slot >= size - 9) break;

            ItemStack item = createItem(Material.PAPER, "§e" + arena.getDisplayName(),
                    "§7Interner Name: §f" + arena.getName(),
                    "§7Teams: §f" + arena.getTeams().size(),
                    "§7Max. Spieler: §f" + arena.getMaxPlayers(),
                    "",
                    "§aKlicke zum Auswählen");
            inv.setItem(slot++, item);
        }

        inv.setItem(size - 5, createItem(Material.ARROW, "§c§lZurück", "§7Zurück zum Hauptmenü"));

        player.openInventory(inv);
    }

    public void openSettingsMenu(Player player) {
        PrivateRound round = plugin.getPrivateRoundManager().getPlayerRound(player.getUniqueId());
        if (round == null) return;

        PrivateSettings settings = round.getSettings();

        Inventory inv = Bukkit.createInventory(null, 45, "§8Einstellungen");

        inv.setItem(10, createItem(settings.isGoldEnabled() ? Material.GOLD_INGOT : Material.BARRIER,
                "§e§lGold", settings.isGoldEnabled() ? "§aAktiviert" : "§cDeaktiviert",
                "", "§7Klicke zum Umschalten"));

        inv.setItem(12, createItem(settings.isIronEnabled() ? Material.IRON_INGOT : Material.BARRIER,
                "§7§lEisen", settings.isIronEnabled() ? "§aAktiviert" : "§cDeaktiviert",
                "", "§7Klicke zum Umschalten"));

        inv.setItem(14, createItem(Material.GOLDEN_APPLE, "§c§lMax. Leben",
                "§7Aktuell: §e" + (int) settings.getMaxHealth() + " HP",
                "", "§aLinksklick: §7+2 HP", "§cRechtsklick: §7-2 HP"));

        inv.setItem(16, createItem(Material.LEATHER_CHESTPLATE, "§b§lTeamgröße",
                "§7Aktuell: §e" + settings.getPlayersPerTeam() + " Spieler",
                "", "§aLinksklick: §7+1", "§cRechtsklick: §7-1"));

        inv.setItem(28, createItem(Material.CLAY_BRICK, "§6§lBronze-Rate",
                "§7Aktuell: §e" + settings.getBronzeSpawnRate() + "s",
                "", "§aLinksklick: §7+1s", "§cRechtsklick: §7-1s"));

        inv.setItem(30, createItem(Material.IRON_INGOT, "§7§lEisen-Rate",
                "§7Aktuell: §e" + settings.getIronSpawnRate() + "s",
                "", "§aLinksklick: §7+1s", "§cRechtsklick: §7-1s"));

        inv.setItem(32, createItem(Material.GOLD_INGOT, "§e§lGold-Rate",
                "§7Aktuell: §e" + settings.getGoldSpawnRate() + "s",
                "", "§aLinksklick: §7+1s", "§cRechtsklick: §7-1s"));

        inv.setItem(40, createItem(Material.ARROW, "§c§lZurück", "§7Zurück zum Hauptmenü"));

        player.openInventory(inv);
    }

    public void openInviteMenu(Player player) {
        List<Player> onlinePlayers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player) && plugin.getPrivateRoundManager().getPlayerRound(p.getUniqueId()) == null) {
                onlinePlayers.add(p);
            }
        }

        int size = Math.min(54, ((onlinePlayers.size() / 9) + 2) * 9);
        if (size < 18) size = 18;

        Inventory inv = Bukkit.createInventory(null, size, "§8Spieler einladen");

        int slot = 0;
        for (Player target : onlinePlayers) {
            if (slot >= size - 9) break;

            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            ItemMeta meta = skull.getItemMeta();
            meta.setDisplayName("§e" + target.getName());
            meta.setLore(Arrays.asList("", "§aKlicke zum Einladen"));
            skull.setItemMeta(meta);

            inv.setItem(slot++, skull);
        }

        inv.setItem(size - 5, createItem(Material.ARROW, "§c§lZurück", "§7Zurück zum Hauptmenü"));

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getInventory().getTitle();

        if (!title.startsWith("§8")) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        event.setCancelled(true);

        switch (title) {
            case "§8Private Runde":
                handleMainMenuClick(player, item);
                break;
            case "§8Map auswählen":
                handleMapSelectionClick(player, item);
                break;
            case "§8Einstellungen":
                handleSettingsClick(player, item, event.isLeftClick());
                break;
            case "§8Spieler einladen":
                handleInviteClick(player, item);
                break;
        }
    }

    private void handleMainMenuClick(Player player, ItemStack item) {
        PrivateRound round = plugin.getPrivateRoundManager().getPlayerRound(player.getUniqueId());

        if (item.getType() == Material.EMERALD_BLOCK) {
            player.closeInventory();
            openMapSelection(player);
        } else if (item.getType() == Material.REDSTONE_COMPARATOR) {
            player.closeInventory();
            openSettingsMenu(player);
        } else if (item.getType() == Material.SKULL_ITEM) {
            player.closeInventory();
            openInviteMenu(player);
        } else if (item.getType() == Material.DIAMOND_SWORD) {
            player.closeInventory();
            plugin.getPrivateRoundManager().startPrivateRound(player);
        } else if (item.getType() == Material.BARRIER) {
            player.closeInventory();
            if (round != null) {
                plugin.getPrivateRoundManager().deletePrivateRound(round);
                player.sendMessage(BedWars.PREFIX + "§aPrivate Runde gelöscht!");
            }
        } else if (item.getType() == Material.IRON_DOOR) {
            player.closeInventory();
            plugin.getPrivateRoundManager().leavePrivateRound(player);
        }
    }

    private void handleMapSelectionClick(Player player, ItemStack item) {
        if (item.getType() == Material.ARROW) {
            player.closeInventory();
            openMainMenu(player);
            return;
        }

        if (item.getType() == Material.PAPER && item.hasItemMeta()) {
            String displayName = item.getItemMeta().getDisplayName();
            String mapName = displayName.substring(2);

            for (Arena arena : plugin.getArenaManager().getArenas()) {
                if (arena.getDisplayName().equals(mapName)) {
                    player.closeInventory();
                    plugin.getPrivateRoundManager().createPrivateRound(player, arena.getName());
                    return;
                }
            }
        }
    }

    private void handleSettingsClick(Player player, ItemStack item, boolean leftClick) {
        PrivateRound round = plugin.getPrivateRoundManager().getPlayerRound(player.getUniqueId());
        if (round == null) return;

        PrivateSettings settings = round.getSettings();

        if (item.getType() == Material.ARROW) {
            player.closeInventory();
            openMainMenu(player);
            return;
        }

        boolean changed = false;

        if (item.getType() == Material.GOLD_INGOT && item.getItemMeta().getDisplayName().contains("Gold")) {
            settings.setGoldEnabled(!settings.isGoldEnabled());
            changed = true;
        } else if (item.getType() == Material.BARRIER && item.getItemMeta().getDisplayName().contains("Gold")) {
            settings.setGoldEnabled(true);
            changed = true;
        } else if (item.getType() == Material.IRON_INGOT && item.getItemMeta().getDisplayName().contains("Eisen")) {
            settings.setIronEnabled(!settings.isIronEnabled());
            changed = true;
        } else if (item.getType() == Material.BARRIER && item.getItemMeta().getDisplayName().contains("Eisen")) {
            settings.setIronEnabled(true);
            changed = true;
        } else if (item.getType() == Material.GOLDEN_APPLE) {
            double newHealth = settings.getMaxHealth() + (leftClick ? 2 : -2);
            if (newHealth >= 2 && newHealth <= 100) {
                settings.setMaxHealth(newHealth);
                changed = true;
            }
        } else if (item.getType() == Material.LEATHER_CHESTPLATE) {
            int newSize = settings.getPlayersPerTeam() + (leftClick ? 1 : -1);
            if (newSize >= 1 && newSize <= 4) {
                settings.setPlayersPerTeam(newSize);
                changed = true;
            }
        } else if (item.getType() == Material.CLAY_BRICK) {
            int newRate = settings.getBronzeSpawnRate() + (leftClick ? 1 : -1);
            if (newRate >= 1 && newRate <= 60) {
                settings.setBronzeSpawnRate(newRate);
                changed = true;
            }
        } else if (item.getItemMeta().getDisplayName().contains("Eisen-Rate")) {
            int newRate = settings.getIronSpawnRate() + (leftClick ? 1 : -1);
            if (newRate >= 1 && newRate <= 120) {
                settings.setIronSpawnRate(newRate);
                changed = true;
            }
        } else if (item.getItemMeta().getDisplayName().contains("Gold-Rate")) {
            int newRate = settings.getGoldSpawnRate() + (leftClick ? 1 : -1);
            if (newRate >= 1 && newRate <= 300) {
                settings.setGoldSpawnRate(newRate);
                changed = true;
            }
        }

        if (changed) {
            player.closeInventory();
            openSettingsMenu(player);
        }
    }

    private void handleInviteClick(Player player, ItemStack item) {
        if (item.getType() == Material.ARROW) {
            player.closeInventory();
            openMainMenu(player);
            return;
        }

        if (item.getType() == Material.SKULL_ITEM && item.hasItemMeta()) {
            String targetName = item.getItemMeta().getDisplayName().substring(2);
            Player target = Bukkit.getPlayer(targetName);

            if (target != null && target.isOnline()) {
                player.closeInventory();
                plugin.getPrivateRoundManager().invitePlayer(player, target);
            }
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }
}
