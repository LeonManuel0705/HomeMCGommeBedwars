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
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getGameManager().getArenaByPlayer(player.getUniqueId()) != null) {
            plugin.getGameManager().leaveArena(player);
        }

        hiddenPlayers.remove(player.getUniqueId());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        UUID UUID = player.getUniqueId();

        if (plugin.getBuildManager().isInBuildMode(UUID)) {
            return;
        }

        if (item == null) return;

        if (plugin.getGameManager().getArenaByPlayer(player.getUniqueId()) != null) {
            return;
        }

        if (item.getType() == Material.PAINTING) {
            event.setCancelled(true);
            player.performCommand("stats");
        }
        else if (item.getType() == Material.BLAZE_ROD) {
            event.setCancelled(true);
            togglePlayerVisibility(player);
        }
        else if (item.getType() == Material.BED) {
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

            ItemStack item = player.getInventory().getItem(7);
            item.setType(Material.BLAZE_ROD);
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§aSpieler angezeigt");
            meta.setLore(java.util.Arrays.asList("§7Klicke um Spieler zu verstecken"));
            item.setItemMeta(meta);

            player.sendMessage("§aSpieler werden wieder angezeigt!");
        } else {
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                if (!online.hasPermission("bedwars.see-hidden")) {
                    player.hidePlayer(online);
                }
            }
            hiddenPlayers.put(player.getUniqueId(), true);

            ItemStack item = player.getInventory().getItem(7);
            item.setType(Material.BLAZE_POWDER);
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§cSpieler versteckt");
            meta.setLore(java.util.Arrays.asList("§7Klicke um Spieler anzuzeigen"));
            item.setItemMeta(meta);

            player.sendMessage("§cSpieler werden versteckt!");
        }
    }
}