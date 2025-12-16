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
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BedWarsLobby {

    private final BedWars plugin;
    private final Set<UUID> lobbyPlayers;
    private Location lobbySpawn;
    private BukkitRunnable updateTask;

    public BedWarsLobby(BedWars plugin) {
        this.plugin = plugin;
        this.lobbyPlayers = new HashSet<>();
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

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        giveItems(player);
    }

    private void giveItems(Player player) {
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

        // Ink Sack is deprecated, use RED_DYE or legacy support if pre-1.13
        Material dye = Material.matchMaterial("RED_DYE");
        if (dye == null) dye = Material.valueOf("INK_SACK"); // legacy fallback

        ItemStack lobby = new ItemBuilder(dye)
                .setDisplayName("§cZurück zur Lobby §7(Rechtsklick)")
                .build();
        player.getInventory().setItem(8, lobby);
    }

    public void updateScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("lobby", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName("§c§lBED§f§lWARS");

        int line = 15;

        setScore(obj, "§1", line--);
        setScore(obj, "§7Online: §a" + Bukkit.getOnlinePlayers().size(), line--);
        setScore(obj, "§2", line--);
        setScore(obj, "§7Spiele: §e" + plugin.getGameManager().getAllGames().size(), line--);
        setScore(obj, "§3", line--);

        if (CoreAPI.isCoreAvailable()) {
            try {
                int coins = CoreAPI.getCore().getCoinManager().getCoins(player.getUniqueId());
                setScore(obj, "§7Coins: §6" + coins, line--);
            } catch (Exception e) {
                setScore(obj, "§7Coins: §6-", line--);
            }
        }

        setScore(obj, "§4", line--);
        setScore(obj, "§eplay.homemc.org", line--);

        player.setScoreboard(board);
    }

    private void setScore(Objective obj, String text, int score) {
        obj.getScore(text).setScore(score);
    }

    // === Reflection-based TabList update (from your working TabManager) ===
    public void updateTabList(Player player) {
        try {
            String header = buildHeader(player).replace("\"", "\\\"");
            String footer = buildFooter().replace("\"", "\\\"");

            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." +
                    getServerVersion() + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            Object handle = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);
            Object connection = handle.getClass().getField("playerConnection").get(handle);

            Class<?> chatSerializer = Class.forName("net.minecraft.server." +
                    getServerVersion() + ".IChatBaseComponent$ChatSerializer");
            Class<?> packetClass = Class.forName("net.minecraft.server." +
                    getServerVersion() + ".PacketPlayOutPlayerListHeaderFooter");

            Object headerComponent = chatSerializer.getMethod("a", String.class)
                    .invoke(null, "{\"text\":\"" + header + "\"}");
            Object footerComponent = chatSerializer.getMethod("a", String.class)
                    .invoke(null, "{\"text\":\"" + footer + "\"}");

            Object packet = packetClass.getConstructor().newInstance();

            Field a = packetClass.getDeclaredField("a");
            Field b = packetClass.getDeclaredField("b");
            a.setAccessible(true);
            b.setAccessible(true);
            a.set(packet, headerComponent);
            b.set(packet, footerComponent);

            connection.getClass().getMethod("sendPacket",
                            Class.forName("net.minecraft.server." + getServerVersion() + ".Packet"))
                    .invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getServerVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    private String buildHeader(Player player) {
        int coins = 0;
        if (CoreAPI.isCoreAvailable()) {
            try {
                coins = CoreAPI.getCore().getCoinManager().getCoins(player.getUniqueId());
            } catch (Exception ignored) {}
        }

        return "\n" +
                "§6§l⚡ §c§lBED§f§lWARS §6§l⚡\n" +
                "§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "\n" +
                "§7Deine Coins§8: §6" + coins + "\n" +
                "\n";
    }

    private String buildFooter() {
        int online = Bukkit.getOnlinePlayers().size();
        int games = plugin.getGameManager().getAllGames().size();

        return "\n" +
                "§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "§7Spieler online§8: §a" + online + "\n" +
                "§7Aktive Spiele§8: §e" + games + "\n" +
                "\n" +
                "§e§lWebsite§8: §fwww.homemc.org\n" +
                "§9§lDiscord§8: §fdiscord.gg/homemc\n" +
                "§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━\n";
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
        updateTask.runTaskTimer(plugin, 20L, 100L); // alle 5 Sekunden
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
