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
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();
        compassMeta.setDisplayName("§a§lSpielmodus");
        compassMeta.setLore(Arrays.asList(
                "§7Wähle einen Spielmodus:",
                "§f",
                "§8▸ §eSolo §7(1x8)",
                "§8▸ §aDuo §7(2x4)",
                "§8▸ §bTeam §7(4x2)",
                "§f",
                "§eRechtsklick §7um Modus zu wählen!"
        ));
        compass.setItemMeta(compassMeta);
        player.getInventory().setItem(0, compass);

        ItemStack cosmetics = new ItemStack(Material.CHEST);
        ItemMeta cosmeticsMeta = cosmetics.getItemMeta();
        cosmeticsMeta.setDisplayName("§d§lKosmetik");
        cosmeticsMeta.setLore(Arrays.asList(
                "§7Passe dein Spielerlebnis an!",
                "§f",
                "§8▸ §cKill-Nachrichten",
                "§8▸ §6Todes-Nachrichten",
                "§8▸ §bBett-Sounds",
                "§8▸ §dSieges-Tänze",
                "§f",
                "§eRechtsklick §7zum Öffnen"
        ));
        cosmetics.setItemMeta(cosmeticsMeta);
        player.getInventory().setItem(2, cosmetics);

        ItemStack stats = new ItemStack(Material.PAPER);
        ItemMeta statsMeta = stats.getItemMeta();
        statsMeta.setDisplayName("§6§lStatistiken");
        List<String> statsLore = new ArrayList<>();
        statsLore.add("§7Zeige deine BedWars Statistiken");
        statsLore.add("§f");
        me.leon.bedwars.managers.StatsManager.PlayerStats playerStats =
                plugin.getStatsManager().getStats(player.getUniqueId());
        if (playerStats != null) {
            statsLore.add("§8▸ §7Kills: §f" + playerStats.kills);
            statsLore.add("§8▸ §7Tode: §f" + playerStats.deaths);
            statsLore.add("§8▸ §7Siege: §f" + playerStats.wins);
            statsLore.add("§8▸ §7K/D: §f" + String.format("%.2f", playerStats.getKD()));
        }
        statsLore.add("§f");
        statsLore.add("§eRechtsklick §7für Details!");
        statsMeta.setLore(statsLore);
        stats.setItemMeta(statsMeta);
        player.getInventory().setItem(4, stats);

        ItemStack hidePlayer = new ItemStack(Material.BLAZE_ROD);
        ItemMeta hideMeta = hidePlayer.getItemMeta();
        hideMeta.setDisplayName("§aSpieler sichtbar");
        hideMeta.setLore(Arrays.asList(
                "§7Spieler werden angezeigt",
                "§f",
                "§eRechtsklick §7zum Verstecken"
        ));
        hidePlayer.setItemMeta(hideMeta);
        player.getInventory().setItem(6, hidePlayer);

        if (player.hasPermission("bedwars.private")) {
            ItemStack privateItem = new ItemStack(Material.NETHER_STAR);
            ItemMeta privateMeta = privateItem.getItemMeta();
            privateMeta.setDisplayName("§d§lPrivate Runde");
            privateMeta.setLore(Arrays.asList(
                    "§7Erstelle deine eigene Runde!",
                    "§f",
                    "§8▸ §7Lade Freunde ein",
                    "§8▸ §7Wähle Einstellungen",
                    "§8▸ §7Spiele auf deiner Map",
                    "§f",
                    "§eRechtsklick §7zum Öffnen"
            ));
            privateItem.setItemMeta(privateMeta);
            player.getInventory().setItem(7, privateItem);
        }

        ItemStack leave = new ItemStack(Material.SLIME_BALL);
        ItemMeta leaveMeta = leave.getItemMeta();
        leaveMeta.setDisplayName("§c§lZur Lobby");
        leaveMeta.setLore(Arrays.asList(
                "§7Zurück zur Hauptlobby",
                "§f",
                "§eRechtsklick §7zum Verlassen"
        ));
        leave.setItemMeta(leaveMeta);
        player.getInventory().setItem(8, leave);
    }

    public void updateLobbyScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("lobby", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName("§f§lHOMEMC");

        int line = 15;
        obj.getScore("§8" + getCurrentDate()).setScore(line--);
        obj.getScore("§f").setScore(line--);

        String rankColor = plugin.getCore().getRankManager().getRankColor(player);
        obj.getScore("§7Hallo, " + rankColor + plugin.getDisplayName(player)).setScore(line--);
        obj.getScore("§f ").setScore(line--);

        obj.getScore("§e§lSpielmodi:").setScore(line--);
        obj.getScore(" §8▸ §eSolo: §7" + getPlayingCount(GameMode.SOLO)).setScore(line--);
        obj.getScore(" §8▸ §aDuo: §7" + getPlayingCount(GameMode.DUO)).setScore(line--);
        obj.getScore(" §8▸ §bTeam: §7" + getPlayingCount(GameMode.TEAM)).setScore(line--);
        obj.getScore("§f  ").setScore(line--);

        me.leon.bedwars.managers.StatsManager.PlayerStats stats =
                plugin.getStatsManager().getStats(player.getUniqueId());

        if (stats != null) {
            obj.getScore("§6§lDeine Stats:").setScore(line--);
            obj.getScore(" §8▸ §fKills: §a" + stats.kills).setScore(line--);
            obj.getScore(" §8▸ §fSiege: §a" + stats.wins).setScore(line--);
            obj.getScore("§f   ").setScore(line--);
        }

        obj.getScore("§eplay.homemc.org").setScore(line--);

        player.setScoreboard(board);
    }

    private String getCurrentDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy");
        return sdf.format(new java.util.Date());
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