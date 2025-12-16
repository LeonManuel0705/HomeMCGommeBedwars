package me.leon.bedwars.lobby;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.utils.ItemBuilder;
import me.leon.core.utils.CoreAPI;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BedWarsLobby {

    private final BedWars plugin;
    private final Set<UUID> lobbyPlayers;
    private final Set<UUID> buildModePlayers;
    private Location lobbySpawn;
    private BukkitRunnable updateTask;

    public BedWarsLobby(BedWars plugin) {
        this.plugin = plugin;
        this.lobbyPlayers = new HashSet<>();
        this.buildModePlayers = new HashSet<>();
        loadLobbyLocation();
        startUpdateTask();
    }

    private void loadLobbyLocation() {
        if (plugin.getConfig().contains("lobby.world")) {
            String world = plugin.getConfig().getString("lobby.world");
            double x = plugin.getConfig().getDouble("lobby.x");
            double y = plugin.getConfig().getDouble("lobby.y");
            double z = plugin.getConfig().getDouble("lobby.z");
            float yaw = (float) plugin.getConfig().getDouble("lobby.yaw");
            float pitch = (float) plugin.getConfig().getDouble("lobby.pitch");

            lobbySpawn = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
        }
    }

    public void addPlayer(Player player) {
        lobbyPlayers.add(player.getUniqueId());

        if (lobbySpawn != null) {
            player.teleport(lobbySpawn);
        }

        setupPlayer(player);
        updateScoreboard(player);
        updateTabList(player);
    }

    public void removePlayer(Player player) {
        lobbyPlayers.remove(player.getUniqueId());
        buildModePlayers.remove(player.getUniqueId());
        player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
    }

    private void setupPlayer(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setFireTicks(0);
        player.setLevel(0);
        player.setExp(0);

        for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        if (!buildModePlayers.contains(player.getUniqueId())) {
            giveItems(player);
        }
    }

    public void giveItems(Player player) {
        ItemStack navigator = new ItemBuilder(Material.COMPASS)
                .setDisplayName("§aNavigator §7(Rechtsklick)")
                .setLore("§7Wähle ein Spiel aus!")
                .build();
        player.getInventory().setItem(0, navigator);

        ItemStack stats = new ItemBuilder(Material.PAPER)
                .setDisplayName("§6Deine Statistiken §7(Rechtsklick)")
                .build();
        player.getInventory().setItem(1, stats);

        ItemStack achievements = new ItemBuilder(Material.EMERALD)
                .setDisplayName("§eAchievements §7(Rechtsklick)")
                .build();
        player.getInventory().setItem(4, achievements);

        ItemStack gadgets = new ItemBuilder(Material.CHEST)
                .setDisplayName("§bGadget Shop §7(Rechtsklick)")
                .build();
        player.getInventory().setItem(7, gadgets);

        ItemStack lobby = new ItemBuilder(Material.INK_SACK, 1, (short) 1)
                .setDisplayName("§cZurück zur Lobby §7(Rechtsklick)")
                .build();
        player.getInventory().setItem(8, lobby);
    }

    public boolean toggleBuildMode(Player player) {
        UUID uuid = player.getUniqueId();

        if (buildModePlayers.contains(uuid)) {
            buildModePlayers.remove(uuid);
            giveItems(player);
            return false;
        } else {
            buildModePlayers.add(uuid);
            player.getInventory().clear();
            return true;
        }
    }

    public boolean isInBuildMode(UUID uuid) {
        return buildModePlayers.contains(uuid);
    }

    public void updateScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("lobby", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName("§c§lBED§f§lWARS");

        int line = 15;

        setScore(obj, "§1", line--);
        setScore(obj, "§7Spieler:", line--);
        setScore(obj, "  §e" + Bukkit.getOnlinePlayers().size(), line--);
        setScore(obj, "§2", line--);

        int lobbyCount = 0;
        int ingameCount = 0;
        for (me.leon.bedwars.game.Game game : plugin.getGameManager().getAllGames()) {
            if (game.getState() == me.leon.bedwars.game.GameState.LOBBY) {
                lobbyCount++;
            } else if (game.getState() == me.leon.bedwars.game.GameState.INGAME) {
                ingameCount++;
            }
        }

        setScore(obj, "§7Spiele:", line--);
        setScore(obj, "  §aLobby: §e" + lobbyCount, line--);
        setScore(obj, "  §cIngame: §e" + ingameCount, line--);
        setScore(obj, "§3", line--);

        if (CoreAPI.isCoreAvailable()) {
            try {
                int coins = CoreAPI.getCore().getCoinManager().getCoins(player.getUniqueId());
                setScore(obj, "§7Coins:", line--);
                setScore(obj, "  §6" + coins, line--);
                setScore(obj, "§4", line--);
            } catch (Exception e) {
            }
        }

        me.leon.bedwars.managers.StatsManager.PlayerStats stats = plugin.getStatsManager().getStats(player.getUniqueId());
        if (stats != null) {
            setScore(obj, "§7Wins:", line--);
            setScore(obj, "  §a" + stats.wins, line--);
            setScore(obj, "§7Kills:", line--);
            setScore(obj, "  §c" + stats.kills, line--);
            setScore(obj, "§5", line--);
        }

        setScore(obj, "§eplay.homemc.org", line--);

        player.setScoreboard(board);
    }

    private void setScore(Objective obj, String text, int score) {
        obj.getScore(text).setScore(score);
    }

    public void updateTabList(Player player) {
        if (!CoreAPI.isCoreAvailable()) return;

        try {
            // === Header (oben im Tab) ===
            String header = "§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "§c§lBED§f§lWARS\n" +
                    "§7play.homemc.org\n" +
                    "§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━\n";

            // === Stats Zeile ===
            me.leon.bedwars.managers.StatsManager.PlayerStats stats = plugin.getStatsManager().getStats(player.getUniqueId());
            String statsLine = "";
            if (stats != null) {
                statsLine = "§7Deine Stats: §e" + stats.wins + " Wins §8| §c" + stats.kills + " Kills\n";
            }

            // === Footer (unten im Tab) ===
            String footer = "\n§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    statsLine +
                    "§7Online: §a" + Bukkit.getOnlinePlayers().size() + " §8| " +
                    "§7Spiele: §e" + plugin.getGameManager().getAllGames().size() + "\n" +
                    "§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━";

            // === Reflection für ältere Versionen ===
            String version = getServerVersion();

            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            Object handle = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);
            Object connection = handle.getClass().getField("playerConnection").get(handle);

            Class<?> chatSerializer = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent$ChatSerializer");
            Class<?> packetClass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutPlayerListHeaderFooter");

            Object headerComponent = chatSerializer.getMethod("a", String.class)
                    .invoke(null, "{\"text\":\"" + header.replace("\"", "\\\"") + "\"}");
            Object footerComponent = chatSerializer.getMethod("a", String.class)
                    .invoke(null, "{\"text\":\"" + footer.replace("\"", "\\\"") + "\"}");

            Object packet = packetClass.getConstructor().newInstance();

            java.lang.reflect.Field a = packetClass.getDeclaredField("a");
            java.lang.reflect.Field b = packetClass.getDeclaredField("b");
            a.setAccessible(true);
            b.setAccessible(true);
            a.set(packet, headerComponent);
            b.set(packet, footerComponent);

            connection.getClass().getMethod("sendPacket",
                            Class.forName("net.minecraft.server." + version + ".Packet"))
                    .invoke(connection, packet);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getServerVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }


    private void startUpdateTask() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : lobbyPlayers) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        updateScoreboard(player);
                        updateTabList(player);
                    }
                }
            }
        };
        updateTask.runTaskTimer(plugin, 20L, 20L);
    }

    public void shutdown() {
        if (updateTask != null) {
            updateTask.cancel();
        }
    }

    public void setLobbySpawn(Location location) {
        this.lobbySpawn = location;

        plugin.getConfig().set("lobby.world", location.getWorld().getName());
        plugin.getConfig().set("lobby.x", location.getX());
        plugin.getConfig().set("lobby.y", location.getY());
        plugin.getConfig().set("lobby.z", location.getZ());
        plugin.getConfig().set("lobby.yaw", location.getYaw());
        plugin.getConfig().set("lobby.pitch", location.getPitch());
        plugin.saveConfig();
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public boolean isInLobby(UUID uuid) {
        return lobbyPlayers.contains(uuid);
    }
}