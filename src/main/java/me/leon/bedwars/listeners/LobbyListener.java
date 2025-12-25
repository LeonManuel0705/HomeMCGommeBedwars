package me.leon.bedwars.listeners;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import me.leon.bedwars.lobby.LobbyManager;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LobbyListener implements Listener {

    private final BedWars plugin;
    private final Map<UUID, Boolean> hiddenPlayers;

    public LobbyListener(BedWars plugin) {
        this.plugin = plugin;
        this.hiddenPlayers = new HashMap<>();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getLobbyManager().sendToLobby(player);

        plugin.getStatsManager().loadStats(player.getUniqueId());
        plugin.getCosmeticsManager().loadPlayerCosmetics(player.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getGameManager().getArenaByPlayer(player.getUniqueId()) != null) {
            plugin.getGameManager().leaveArena(player);
        }

        plugin.getCosmeticsManager().unloadPlayerCosmetics(player.getUniqueId());
        hiddenPlayers.remove(player.getUniqueId());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        UUID uuid = player.getUniqueId();

        if (plugin.getBuildManager().isInBuildMode(uuid)) {
            return;
        }

        if (item == null) return;

        if (plugin.getGameManager().getArenaByPlayer(player.getUniqueId()) != null) {
            return;
        }

        // Compass - Game mode selector GUI
        if (item.getType() == Material.COMPASS) {
            event.setCancelled(true);
            openGameModeSelector(player);
        }
        // Paper - Stats
        else if (item.getType() == Material.PAPER) {
            event.setCancelled(true);
            player.performCommand("stats");
        }
        // Blaze Rod - Player visibility toggle
        else if (item.getType() == Material.BLAZE_ROD || item.getType() == Material.BLAZE_POWDER) {
            event.setCancelled(true);
            togglePlayerVisibility(player);
        }
        // Chest - Cosmetics GUI
        else if (item.getType() == Material.CHEST) {
            event.setCancelled(true);
            plugin.getCosmeticsGUI().openMainMenu(player);
        }
        // Nether Star - Private round GUI
        else if (item.getType() == Material.NETHER_STAR) {
            event.setCancelled(true);
            if (player.hasPermission("bedwars.private")) {
                plugin.getPrivateRoundGUI().openMainMenu(player);
            }
        }
        // Slime Ball - Return to lobby (BungeeCord)
        else if (item.getType() == Material.SLIME_BALL) {
            event.setCancelled(true);
            try {
                com.google.common.io.ByteArrayDataOutput out = com.google.common.io.ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF("lobby");
                player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
            } catch (Exception e) {
                player.sendMessage("§cFehler beim Verbinden zur Lobby!");
                e.printStackTrace();
            }
        }
    }

    private void openGameModeSelector(Player player) {
        org.bukkit.inventory.Inventory inv = org.bukkit.Bukkit.createInventory(null, 27, "§8» §6§lSpielmodus wählen");

        // Solo - Yellow bed
        ItemStack solo = new ItemStack(Material.BED, 1, (short) 4); // Yellow bed
        org.bukkit.inventory.meta.ItemMeta soloMeta = solo.getItemMeta();
        soloMeta.setDisplayName("§e§lSolo §7(1x8)");
        int soloPlayers = getPlayingCount(LobbyManager.GameMode.SOLO);
        soloMeta.setLore(java.util.Arrays.asList(
                "§7Spiele alleine gegen 7 andere!",
                "§f",
                "§8▸ §7Spieler: §e" + soloPlayers,
                "§f",
                "§aKlicke zum Spielen!"
        ));
        solo.setItemMeta(soloMeta);
        inv.setItem(11, solo);

        // Duo - Lime bed
        ItemStack duo = new ItemStack(Material.BED, 1, (short) 5); // Lime bed
        org.bukkit.inventory.meta.ItemMeta duoMeta = duo.getItemMeta();
        duoMeta.setDisplayName("§a§lDuo §7(2x4)");
        int duoPlayers = getPlayingCount(LobbyManager.GameMode.DUO);
        duoMeta.setLore(java.util.Arrays.asList(
                "§7Spiele mit einem Freund!",
                "§f",
                "§8▸ §7Spieler: §a" + duoPlayers,
                "§f",
                "§aKlicke zum Spielen!"
        ));
        duo.setItemMeta(duoMeta);
        inv.setItem(13, duo);

        // Team 4x2 - Cyan bed
        ItemStack team = new ItemStack(Material.BED, 1, (short) 9); // Cyan bed
        org.bukkit.inventory.meta.ItemMeta teamMeta = team.getItemMeta();
        teamMeta.setDisplayName("§b§lTeam §7(4x2)");
        int teamPlayers = getPlayingCount(LobbyManager.GameMode.TEAM);
        teamMeta.setLore(java.util.Arrays.asList(
                "§7Spiele im 4er Team!",
                "§f",
                "§8▸ §7Spieler: §b" + teamPlayers,
                "§f",
                "§aKlicke zum Spielen!"
        ));
        team.setItemMeta(teamMeta);
        inv.setItem(15, team);

        // Glass panes for decoration
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7); // Gray glass
        org.bukkit.inventory.meta.ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, glass);
            }
        }

        player.openInventory(inv);
    }

    private int getPlayingCount(LobbyManager.GameMode mode) {
        int count = 0;
        for (me.leon.bedwars.game.Arena arena : plugin.getArenaManager().getArenas()) {
            if (arena.getGameMode() == mode) {
                count += arena.getPlayers().size();
            }
        }
        return count;
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (event.getRightClicked().getType() == EntityType.VILLAGER) {
            Villager npc = (Villager) event.getRightClicked();

            if (npc.hasMetadata("bw_npc")) {
                event.setCancelled(true);
                String modeName = npc.getMetadata("bw_npc").get(0).asString();

                try {
                    LobbyManager.GameMode mode = LobbyManager.GameMode.valueOf(modeName);
                    plugin.getLobbyManager().joinQueue(player, mode);
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cFehler beim Beitreten!");
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (plugin.getBuildManager().isInBuildMode(player.getUniqueId())) {
            return;
        }

        if (plugin.getGameManager().getArenaByPlayer(player.getUniqueId()) == null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (plugin.getBuildManager().isInBuildMode(player.getUniqueId())) {
            return;
        }

        if (plugin.getGameManager().getArenaByPlayer(player.getUniqueId()) == null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());

        if (arena == null) {
            if (!plugin.getBuildManager().isInBuildMode(player.getUniqueId())) {
                event.setCancelled(true);
            }
            return;
        }

        if (arena.getGameState() != GameState.INGAME) {
            event.setCancelled(true);
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            return;
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());

        if (arena == null) {
            if (!plugin.getBuildManager().isInBuildMode(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
        if (arena != null) {
            if (arena.getGameState() != GameState.INGAME) {
                event.setCancelled(true);
            }
            return;
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (plugin.getGameManager().getArenaByPlayer(player.getUniqueId()) == null) {
            event.setCancelled(true);
        }
    }

    private void togglePlayerVisibility(Player player) {
        boolean hidden = hiddenPlayers.getOrDefault(player.getUniqueId(), false);

        if (hidden) {
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                player.showPlayer(online);
            }
            hiddenPlayers.put(player.getUniqueId(), false);

            ItemStack item = player.getInventory().getItem(6);
            item.setType(Material.BLAZE_ROD);
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§aSpieler sichtbar");
            meta.setLore(java.util.Arrays.asList(
                    "§7Spieler werden angezeigt",
                    "§f",
                    "§eRechtsklick §7zum Verstecken"
            ));
            item.setItemMeta(meta);

            player.sendMessage(BedWars.PREFIX + "§aSpieler werden wieder angezeigt!");
        } else {
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                if (!online.hasPermission("bedwars.see-hidden")) {
                    player.hidePlayer(online);
                }
            }
            hiddenPlayers.put(player.getUniqueId(), true);

            ItemStack item = player.getInventory().getItem(6);
            item.setType(Material.BLAZE_POWDER);
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§cSpieler versteckt");
            meta.setLore(java.util.Arrays.asList(
                    "§7Spieler werden versteckt",
                    "§f",
                    "§eRechtsklick §7zum Anzeigen"
            ));
            item.setItemMeta(meta);

            player.sendMessage(BedWars.PREFIX + "§cSpieler werden versteckt!");
        }
    }
}