package me.leon.bedwars;

import me.leon.bedwars.commands.*;
import me.leon.bedwars.listeners.*;
import me.leon.bedwars.lobby.BedWarsLobby;
import me.leon.bedwars.managers.*;
import me.leon.core.managers.RankManager;
import me.leon.core.utils.CoreAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BedWars extends JavaPlugin {

    public static final String PREFIX = "§cBedWars §8» ";

    private static BedWars instance;

    private MySQL mySQL;
    private GameManager gameManager;
    private MapManager mapManager;
    private StatsManager statsManager;
    private ShopManager shopManager;
    private TeamManager teamManager;
    private SpectatorManager spectatorManager;
    private AchievementManager achievementManager;
    private GadgetManager gadgetManager;
    private LobbyManager lobbyManager;
    private GeneratorManager generatorManager;
    private HologramManager hologramManager;
    private PartyIntegration partyIntegration;
    private JoinMeIntegration joinMeIntegration;
    private BedWarsLobby bedWarsLobby;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        getLogger().info("Initialisiere MySQL Verbindung...");
        mySQL = new MySQL(this);
        mySQL.connect();
        createTables();

        getLogger().info("Lade Manager...");
        initializeManagers();

        getLogger().info("Registriere Commands...");
        registerCommands();

        getLogger().info("Registriere Listeners...");
        registerListeners();

        getLogger().info("BedWars wurde erfolgreich geladen!");

        if (CoreAPI.isCoreAvailable()) {
            getLogger().info("Core-Integration: AKTIV");
        } else {
            getLogger().warning("Core-Integration: INAKTIV - Einige Features funktionieren nicht!");
        }
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.shutdownAll();
        }

        if (hologramManager != null) {
            hologramManager.shutdown();
        }

        if (bedWarsLobby != null) {
            bedWarsLobby.shutdown();
        }

        if (mySQL != null) {
            mySQL.disconnect();
        }

        getLogger().info("BedWars wurde deaktiviert!");
    }

    private void initializeManagers() {
        mapManager = new MapManager(this);
        teamManager = new TeamManager(this);
        statsManager = new StatsManager(this);
        shopManager = new ShopManager(this);
        spectatorManager = new SpectatorManager(this);
        achievementManager = new AchievementManager(this);
        gadgetManager = new GadgetManager(this);
        lobbyManager = new LobbyManager(this);
        generatorManager = new GeneratorManager(this);
        hologramManager = new HologramManager(this);
        partyIntegration = new PartyIntegration(this);
        joinMeIntegration = new JoinMeIntegration(this);
        bedWarsLobby = new BedWarsLobby(this);
        gameManager = new GameManager(this);

        mapManager.loadMaps();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord",
                new BedWarsPluginMessageListener(this));
    }

    private void registerCommands() {
        getCommand("bw").setExecutor(new BedWarsCommand(this));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("start").setExecutor(new StartCommand(this));
        getCommand("leave").setExecutor(new LeaveCommand(this));
        getCommand("setup").setExecutor(new SetupCommand(this));
        getCommand("shout").setExecutor(new ShoutCommand(this));
        getCommand("joinme").setExecutor(new JoinMeCommand(this));
        getCommand("lobby").setExecutor(new LobbyCommand(this));
    }

    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();

        // Player Events
        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new PlayerQuitListener(this), this);
        pm.registerEvents(new PlayerInteractListener(this), this);
        pm.registerEvents(new PlayerMoveListener(this), this);
        pm.registerEvents(new PlayerDeathListener(this), this);
        pm.registerEvents(new PlayerRespawnListener(this), this);
        pm.registerEvents(new PlayerBedEnterListener(this), this);
        pm.registerEvents(new PlayerPickupItemListener(this), this);
        pm.registerEvents(new PlayerDropItemListener(this), this);

        // Block Events
        pm.registerEvents(new BlockBreakListener(this), this);
        pm.registerEvents(new BlockPlaceListener(this), this);

        // Entity Events
        pm.registerEvents(new EntityDamageListener(this), this);
        pm.registerEvents(new EntityDamageByEntityListener(this), this);
        pm.registerEvents(new EntitySpawnListener(this), this);

        // Inventory Events
        pm.registerEvents(new InventoryClickListener(this), this);
        pm.registerEvents(new CraftingListener(this), this);

        // Projectile Events
        pm.registerEvents(new ProjectileLaunchListener(this), this);
        pm.registerEvents(new ProjectileHitListener(this), this);

        // Explosion Events
        pm.registerEvents(new ExplosionListener(this), this);

        // Chat Events
        pm.registerEvents(new ChatListener(this), this);

        // Game-specific Events
        pm.registerEvents(new TeamSelectorListener(this), this);
        pm.registerEvents(new LobbyItemListener(this), this);
        pm.registerEvents(new LobbyInteractListener(this), this);
        pm.registerEvents(new NavigatorListener(this), this);

        // Food Events
        pm.registerEvents(new FoodLevelChangeListener(this), this);
    }

    private void createTables() {
        try (Connection conn = mySQL.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS bw_stats (" +
                            "uuid VARCHAR(36) PRIMARY KEY," +
                            "name VARCHAR(16)," +
                            "kills INT DEFAULT 0," +
                            "deaths INT DEFAULT 0," +
                            "wins INT DEFAULT 0," +
                            "losses INT DEFAULT 0," +
                            "beds_destroyed INT DEFAULT 0," +
                            "games_played INT DEFAULT 0," +
                            "coins INT DEFAULT 0," +
                            "current_winstreak INT DEFAULT 0," +
                            "highest_winstreak INT DEFAULT 0," +
                            "INDEX(name))"
            );
            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS bw_achievements (" +
                            "uuid VARCHAR(36)," +
                            "achievement_id VARCHAR(50)," +
                            "unlocked_at BIGINT," +
                            "PRIMARY KEY(uuid, achievement_id)," +
                            "INDEX(uuid))"
            );
            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS bw_gadgets (" +
                            "uuid VARCHAR(36)," +
                            "gadget_id VARCHAR(50)," +
                            "purchased_at BIGINT," +
                            "PRIMARY KEY(uuid, gadget_id)," +
                            "INDEX(uuid))"
            );
            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS bw_settings (" +
                            "uuid VARCHAR(36) PRIMARY KEY," +
                            "auto_armor BOOLEAN DEFAULT TRUE," +
                            "quick_buy TEXT," +
                            "selected_gadget VARCHAR(50)," +
                            "INDEX(uuid))"
            );
            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS bw_holograms (" +
                            "id VARCHAR(50) PRIMARY KEY," +
                            "type VARCHAR(20)," +
                            "world VARCHAR(50)," +
                            "x DOUBLE," +
                            "y DOUBLE," +
                            "z DOUBLE," +
                            "INDEX(type))"
            );
            ps.executeUpdate();
            ps.close();

        } catch (SQLException e) {
            getLogger().severe("Fehler beim Erstellen der Datenbank-Tabellen!");
            e.printStackTrace();
        }
    }

    public static BedWars getInstance() {
        return instance;
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public SpectatorManager getSpectatorManager() {
        return spectatorManager;
    }

    public AchievementManager getAchievementManager() {
        return achievementManager;
    }

    public GadgetManager getGadgetManager() {
        return gadgetManager;
    }

    public LobbyManager getLobbyManager() {
        return lobbyManager;
    }

    public GeneratorManager getGeneratorManager() {
        return generatorManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public PartyIntegration getPartyIntegration() {
        return partyIntegration;
    }

    public JoinMeIntegration getJoinMeIntegration() {
        return joinMeIntegration;
    }

    public BedWarsLobby getBedWarsLobby() {
        return bedWarsLobby;
    }

    public RankManager getRankManager() {
        if (CoreAPI.isCoreAvailable()) {
            return CoreAPI.getCore().getRankManager();
        }
        return null;
    }
}