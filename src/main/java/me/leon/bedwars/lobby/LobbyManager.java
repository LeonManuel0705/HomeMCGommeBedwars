package me.leon.bedwars.lobby;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class LobbyManager {

    private final BedWars plugin;
    private Location lobbySpawn;
    private final Map<GameMode, List<Arena>> modeArenas;
    private final Map<UUID, Long> lastInteract;

    public LobbyManager(BedWars plugin) {
        this.plugin = plugin;
        this.modeArenas = new HashMap<>();
        this.lastInteract = new HashMap<>();

        for (GameMode mode : GameMode.values()) {
            modeArenas.put(mode, new ArrayList<>());
        }

        loadLobbySpawn();
    }

    private void loadLobbySpawn() {
        String world = plugin.getConfig().getString("lobby.spawn.world", "world");
        double x = plugin.getConfig().getDouble("lobby.spawn.x", 0.5);
        double y = plugin.getConfig().getDouble("lobby.spawn.y", 100.0);
        double z = plugin.getConfig().getDouble("lobby.spawn.z", 0.5);
        float yaw = (float) plugin.getConfig().getDouble("lobby.spawn.yaw", 0.0);
        float pitch = (float) plugin.getConfig().getDouble("lobby.spawn.pitch", 0.0);

        lobbySpawn = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    public void sendToLobby(Player player) {
        player.teleport(lobbySpawn);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setExp(0);
        player.setLevel(0);
        player.setFireTicks(0);
        player.setGameMode(org.bukkit.GameMode.SURVIVAL);

        giveLobbyItems(player);

        updateLobbyScoreboard(player);

        plugin.getNametagManager().resetNametag(player);
    }

    public void giveLobbyItems(Player player) {
        ItemStack stats = new ItemStack(Material.PAINTING);
        ItemMeta statsMeta = stats.getItemMeta();
        statsMeta.setDisplayName("§6§lDeine Stats §7(Rechtsklick)");
        List<String> lore = new ArrayList<>();
        lore.add("§7Klicke um deine Statistiken");
        lore.add("§7anzuzeigen!");
        statsMeta.setLore(lore);
        stats.setItemMeta(statsMeta);
        player.getInventory().setItem(0, stats);

        ItemStack hidePlayer = new ItemStack(Material.BLAZE_ROD);
        ItemMeta hideMeta = hidePlayer.getItemMeta();
        hideMeta.setDisplayName("§aSpieler angezeigt");
        hideMeta.setLore(Arrays.asList("§7Klicke um Spieler zu verstecken"));
        hidePlayer.setItemMeta(hideMeta);
        player.getInventory().setItem(7, hidePlayer);

        ItemStack leave = new ItemStack(Material.BED);
        ItemMeta leaveMeta = leave.getItemMeta();
        leaveMeta.setDisplayName("§cZur Hauptlobby");
        leaveMeta.setLore(Arrays.asList("§7Zurück zur Hauptlobby"));
        leave.setItemMeta(leaveMeta);
        player.getInventory().setItem(8, leave);
    }

    public void updateLobbyScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("lobby", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName("§c§lBedWars");

        int line = 15;
        obj.getScore("§8§m━━━━━━━━━━━━━━━").setScore(line--);
        obj.getScore("§7Willkommen, " + plugin.getCore().getRankManager().getRankColor(player) + player.getName()).setScore(line--);
        obj.getScore("§f ").setScore(line--);

        obj.getScore("§6§lSpielmodi").setScore(line--);
        obj.getScore(" §eSolo §8• §7" + getPlayingCount(GameMode.SOLO) + " Spieler").setScore(line--);
        obj.getScore(" §aDuo  §8• §7" + getPlayingCount(GameMode.DUO) + " Spieler").setScore(line--);
        obj.getScore(" §b4x4  §8• §7" + getPlayingCount(GameMode.TEAM) + " Spieler").setScore(line--);
        obj.getScore("§f  ").setScore(line--);

        me.leon.bedwars.managers.StatsManager.PlayerStats stats =
                plugin.getStatsManager().getStats(player.getUniqueId());

        if (stats != null) {
            obj.getScore("§6§lDeine Stats").setScore(line--);
            obj.getScore(" §7Kills§8: §f" + stats.kills).setScore(line--);
            obj.getScore(" §7Wins§8: §f" + stats.wins).setScore(line--);
            obj.getScore(" §7K/D§8: §f" + String.format("%.2f", stats.getKD())).setScore(line--);
            obj.getScore("§f   ").setScore(line--);
        }

        obj.getScore("§7➥ §eplay.homemc.org").setScore(line--);
        obj.getScore("§8§m━━━━━━━━━━━━━━━").setScore(line--);

        player.setScoreboard(board);
    }

    private int getPlayingCount(GameMode mode) {
        int count = 0;
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            if (arena.getGameMode() == mode) {
                count += arena.getPlayers().size();
            }
        }
        return count;
    }

    public void joinQueue(Player player, GameMode mode) {
        if (lastInteract.containsKey(player.getUniqueId())) {
            long last = lastInteract.get(player.getUniqueId());
            if (System.currentTimeMillis() - last < 1000) {
                return;
            }
        }
        lastInteract.put(player.getUniqueId(), System.currentTimeMillis());

        Arena arena = findBestArena(mode);

        if (arena == null) {
            player.sendMessage("§cDerzeit ist keine Arena verfügbar!");
            return;
        }

        plugin.getGameManager().joinArena(player, arena.getName());
    }

    private Arena findBestArena(GameMode mode) {
        List<Arena> availableArenas = new ArrayList<>();

        for (Arena arena : plugin.getArenaManager().getArenas()) {
            if (arena.getGameMode() == mode &&
                    arena.getGameState() == me.leon.bedwars.game.GameState.WAITING &&
                    arena.getPlayers().size() < arena.getMaxPlayers()) {
                availableArenas.add(arena);
            }
        }

        if (availableArenas.isEmpty()) {
            return null;
        }

        availableArenas.sort((a1, a2) ->
                Integer.compare(a2.getPlayers().size(), a1.getPlayers().size()));

        return availableArenas.get(0);
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public enum GameMode {
        SOLO("§e", "Solo", "⚔"),
        DUO("§a", "Duo", "⚔⚔"),
        TEAM("§b", "4x4", "⚔⚔⚔⚔");

        private final String color;
        private final String displayName;
        private final String icon;

        GameMode(String color, String displayName, String icon) {
            this.color = color;
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getColor() { return color; }
        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
    }
}