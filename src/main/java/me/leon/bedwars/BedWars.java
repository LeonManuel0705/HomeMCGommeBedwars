package me.leon.bedwars;

import me.leon.bedwars.commands.*;
import me.leon.bedwars.cosmetics.CosmeticsGUI;
import me.leon.bedwars.cosmetics.CosmeticsManager;
import me.leon.bedwars.game.*;
import me.leon.bedwars.gui.PrivateRoundGUI;
import me.leon.bedwars.listeners.*;
import me.leon.bedwars.lobby.*;
import me.leon.bedwars.managers.*;
import me.leon.bedwars.shop.ShopManager;
import me.leon.bedwars.utils.ConfigManager;
import me.leon.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BedWars extends JavaPlugin {

    private static BedWars instance;
    private Core core;

    private GameManager gameManager;
    private ArenaManager arenaManager;
    private TeamManager teamManager;
    private GeneratorManager generatorManager;
    private ShopManager shopManager;
    private StatsManager statsManager;
    private ScoreboardManager scoreboardManager;
    private ProtectionManager protectionManager;
    private ConfigManager configManager;
    private LobbyManager lobbyManager;
    private NametagManager nametagManager;
    private BuildManager buildManager;
    private NPCManager npcManager;
    private PrivateRoundManager privateRoundManager;
    private PrivateRoundGUI privateRoundGUI;
    private PrivateCommand privateCommand;
    private CosmeticsManager cosmeticsManager;
    private CosmeticsGUI cosmeticsGUI;
    private SignManager signManager;

    public static final String PREFIX = "§cBedwars §8» ";

    @Override
    public void onEnable() {
        instance = this;

        core = (Core) Bukkit.getPluginManager().getPlugin("Core");
        if (core == null) {
            getLogger().severe("Core-Plugin nicht gefunden! BedWars wird deaktiviert.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        configManager = new ConfigManager(this);

        initializeManagers();

        registerCommands();

        registerListeners();

        // Register BungeeCord channel for server switching
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        startTasks();

        getLogger().info("BedWars wurde erfolgreich geladen!");
        getLogger().info("Integration mit Core-Plugin aktiv");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.shutdown();
        }

        getLogger().info("BedWars wurde deaktiviert!");
    }

    private void initializeManagers() {
        this.arenaManager = new ArenaManager(this);
        this.teamManager = new TeamManager(this);
        this.generatorManager = new GeneratorManager(this);
        this.shopManager = new ShopManager(this);
        this.statsManager = new StatsManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.protectionManager = new ProtectionManager(this);
        this.nametagManager = new NametagManager(this);
        this.lobbyManager = new LobbyManager(this);
        this.buildManager = new BuildManager(this);
        this.gameManager = new GameManager(this);
        this.npcManager = new NPCManager(this);
        this.privateRoundManager = new PrivateRoundManager(this);
        this.privateRoundGUI = new PrivateRoundGUI(this);
        this.cosmeticsManager = new CosmeticsManager(this);
        this.cosmeticsGUI = new CosmeticsGUI(this);
        this.signManager = new SignManager(this);

        arenaManager.loadArenas();
    }

    private void registerCommands() {
        getCommand("bw").setExecutor(new BedWarsCommand(this));
        getCommand("setup").setExecutor(new SetupCommand(this));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("leave").setExecutor(new LeaveCommand(this));
        getCommand("start").setExecutor(new StartCommand(this));
        getCommand("stop").setExecutor(new StopCommand(this));
        getCommand("build").setExecutor(new BuildCommand(this));
        getCommand("hub").setExecutor(new HubCommand(this));
        getCommand("join").setExecutor(new BedWarsCommand(this));
        getCommand("forcestart").setExecutor(new ForceStartCommand(this));
        getCommand("forcestartprivate").setExecutor(new ForceStartPrivateCommand(this));
        getCommand("bwnpc").setExecutor(new NPCCommand(this));
        this.privateCommand = new PrivateCommand(this);
        getCommand("private").setExecutor(privateCommand);
        getCommand("bwjoinme").setExecutor(new JoinMeCommand(this));
    }

    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new PlayerQuitListener(this), this);
        pm.registerEvents(new PlayerInteractListener(this), this);
        pm.registerEvents(new PlayerMoveListener(this), this);
        pm.registerEvents(new BlockBreakListener(this), this);
        pm.registerEvents(new BlockPlaceListener(this), this);
        pm.registerEvents(new EntityDamageListener(this), this);
        pm.registerEvents(new PlayerDeathListener(this), this);
        pm.registerEvents(new PlayerRespawnListener(this), this);
        pm.registerEvents(new InventoryClickListener(this), this);
        pm.registerEvents(new EntitySpawnListener(this), this);
        pm.registerEvents(new WeatherChangeListener(), this);
        pm.registerEvents(new PlayerDropItemListener(this), this);
        pm.registerEvents(new PlayerPickupItemListener(this), this);
        pm.registerEvents(new PlayerChatListener(this), this);
        pm.registerEvents(new LobbyListener(this), this);
        pm.registerEvents(new VillagerProtectionListener(), this);
        pm.registerEvents(new ExplosionListener(this), this);
        pm.registerEvents(new PotionListener(this), this);
        pm.registerEvents(new RescuePlatformListener(this), this);
        pm.registerEvents(new NPCClickListener(this), this);
        pm.registerEvents(privateRoundGUI, this);
        pm.registerEvents(cosmeticsGUI, this);
    }

    private void startTasks() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Arena arena : arenaManager.getArenas()) {
                if (arena.getGameState() == GameState.INGAME) {
                    generatorManager.processGenerators(arena);
                }
            }
        }, 20L, 20L);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Arena arena : arenaManager.getArenas()) {
                if (arena.getGameState() != GameState.WAITING) {
                    scoreboardManager.updateScoreboard(arena);
                }
            }
        }, 20L, 20L);
    }

    public static BedWars getInstance() {
        return instance;
    }

    public Core getCore() {
        return core;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public GeneratorManager getGeneratorManager() {
        return generatorManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public ProtectionManager getProtectionManager() {
        return protectionManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LobbyManager getLobbyManager() {
        return lobbyManager;
    }

    public NametagManager getNametagManager() {
        return nametagManager;
    }

    public BuildManager getBuildManager() {
        return buildManager;
    }

    public NPCManager getNPCManager() {
        return npcManager;
    }

    public PrivateRoundManager getPrivateRoundManager() {
        return privateRoundManager;
    }

    public PrivateRoundGUI getPrivateRoundGUI() {
        return privateRoundGUI;
    }

    public PrivateCommand getPrivateCommand() {
        return privateCommand;
    }

    public CosmeticsManager getCosmeticsManager() {
        return cosmeticsManager;
    }

    public CosmeticsGUI getCosmeticsGUI() {
        return cosmeticsGUI;
    }

    public SignManager getSignManager() {
        return signManager;
    }

    /**
     * Gets the display name for a player, considering nick status.
     * If the player is nicked, returns the nick name.
     * Otherwise returns the real name.
     */
    public String getDisplayName(Player player) {
        if (core.getNickManager().isNicked(player.getUniqueId())) {
            return core.getNickManager().getDisplayName(player.getUniqueId());
        }
        return player.getName();
    }

    /**
     * Gets the display name for a player UUID, considering nick status.
     */
    public String getDisplayName(java.util.UUID uuid) {
        if (core.getNickManager().isNicked(uuid)) {
            return core.getNickManager().getDisplayName(uuid);
        }
        Player player = getServer().getPlayer(uuid);
        return player != null ? player.getName() : "Unknown";
    }
}