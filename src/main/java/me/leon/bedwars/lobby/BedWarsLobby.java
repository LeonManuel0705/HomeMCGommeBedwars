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
        obj.setDisplayName("§c§l▸ BED§f§lWARS §c§l◂");

        int line = 15;

        setScore(obj, "§8┌─────────────────┐", line--);
        setScore(obj, "§f", line--);

        setScore(obj, "§b▸ SPIELER", line--);
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        setScore(obj, "§7Online:        §f" + onlinePlayers, line--);
        setScore(obj, "§f", line--);

        int lobbyCount = 0;
        int ingameCount = 0;
        int totalGames = 0;
        for (me.leon.bedwars.game.Game game : plugin.getGameManager().getAllGames()) {
            totalGames++;
            if (game.getState() == me.leon.bedwars.game.GameState.LOBBY) {
                lobbyCount++;
            } else if (game.getState() == me.leon.bedwars.game.GameState.INGAME) {
                ingameCount++;
            }
        }

        setScore(obj, "§6▸ SPIELE", line--);
        setScore(obj, "§7Verfügbar:     §a" + lobbyCount, line--);
        setScore(obj, "§7Laufend:       §c" + ingameCount, line--);
        setScore(obj, "§7Gesamt:        §e" + totalGames, line--);
        setScore(obj, "§f", line--);

        if (CoreAPI.isCoreAvailable()) {
            try {
                int coins = CoreAPI.getCore().getCoinManager().getCoins(player.getUniqueId());
                setScore(obj, "§e▸ WÄHRUNG", line--);
                setScore(obj, "§7Coins:         §6" + coins, line--);
                setScore(obj, "§f", line--);
            } catch (Exception e) {
            }
        }

        me.leon.bedwars.managers.StatsManager.PlayerStats stats = plugin.getStatsManager().getStats(player.getUniqueId());
        if (stats != null) {
            setScore(obj, "§5▸ DEINE STATS", line--);
            setScore(obj, "§7Siege:         §a" + stats.wins, line--);
            setScore(obj, "§7Kills:         §c" + stats.kills, line--);

            double winRatio = 0;
            if (stats.kills > 0) {
                winRatio = Math.round((double) stats.wins / stats.kills * 100) / 100.0;
            }
            setScore(obj, "§7K/D Ratio:     §f" + winRatio, line--);
            setScore(obj, "§f", line--);
        }

        setScore(obj, "§8└─────────────────┘", line--);
        setScore(obj, "§eplay.homemc.org", line--);

        player.setScoreboard(board);
    }

    private void setScore(Objective obj, String text, int score) {
        obj.getScore(text).setScore(score);
    }

    public void updateTabList(Player player) {
        if (!CoreAPI.isCoreAvailable()) return;

        try {
            String header = "§c§lBED§f§lWARS\n§7play.homemc.org";

            me.leon.bedwars.managers.StatsManager.PlayerStats stats = plugin.getStatsManager().getStats(player.getUniqueId());
            String statsLine = "";
            if (stats != null) {
                int coinBalance = 0;
                try {
                    coinBalance = CoreAPI.getCore().getCoinManager().getCoins(player.getUniqueId());
                } catch (Exception e) {
                }

                statsLine = "§7Siege: §a" + stats.wins + " §7| Kills: §c" + stats.kills + " §7| Coins: §6" + coinBalance;
            }

            int onlinePlayers = Bukkit.getOnlinePlayers().size();
            int lobbyGames = 0;
            int ingameGames = 0;

            for (me.leon.bedwars.game.Game game : plugin.getGameManager().getAllGames()) {
                if (game.getState() == me.leon.bedwars.game.GameState.LOBBY) {
                    lobbyGames++;
                } else if (game.getState() == me.leon.bedwars.game.GameState.INGAME) {
                    ingameGames++;
                }
            }

            String footer = "§7Online: §a" + onlinePlayers + " §7| Verfügbar: §a" + lobbyGames + " §7| Laufend: §c" + ingameGames;

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
                        
                        if (player.getLocation().getY() < -12 && lobbySpawn != null) {
                            player.teleport(lobbySpawn);
                            player.sendMessage("§cDu wurdest zum Spawn teleportiert!");
                        }
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