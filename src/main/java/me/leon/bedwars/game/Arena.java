package me.leon.bedwars.game;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.lobby.LobbyManager;
import me.leon.bedwars.managers.PrivateRoundManager.PrivateRound;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Arena {

    private final String name;
    private final String displayName;
    private final int minPlayers;
    private final int maxPlayers;
    private final int teamsPerGame;
    private LobbyManager.GameMode gameMode;

    private GameState gameState;
    private GamePhase gamePhase;

    private Location lobby;
    private Location spectator;

    private final Map<String, BedWarsTeam> teams;
    private final Set<UUID> players;
    private final Set<UUID> spectators;
    private final Set<UUID> deadPlayers;

    private final Set<Location> placedBlocks;
    private final Map<Location, GeneratorData> generators;

    private int countdown;
    private BukkitTask countdownTask;

    private long startTime;
    private boolean goldEnabled;
    private int goldTimer;

    private final BedWars plugin;

    public Arena(String name, String displayName, int minPlayers, int maxPlayers, int teamsPerGame, LobbyManager.GameMode gameMode) {
        this.plugin = BedWars.getInstance();
        this.name = name;
        this.displayName = displayName;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.teamsPerGame = teamsPerGame;
        this.gameMode = gameMode != null ? gameMode : LobbyManager.GameMode.TEAM;

        this.gameState = GameState.WAITING;
        this.gamePhase = GamePhase.WAITING;

        this.teams = new HashMap<>();
        this.players = ConcurrentHashMap.newKeySet();
        this.spectators = ConcurrentHashMap.newKeySet();
        this.deadPlayers = ConcurrentHashMap.newKeySet();

        this.placedBlocks = ConcurrentHashMap.newKeySet();
        this.generators = new ConcurrentHashMap<>();

        this.countdown = 60;
        this.goldEnabled = false;
        this.goldTimer = 0;
    }

    public void addPlayer(Player player) {
        // Auto-delete private round when joining official arena
        PrivateRound privateRound = plugin.getPrivateRoundManager().getPlayerRound(player.getUniqueId());
        if (privateRound != null) {
            if (privateRound.getCreator().equals(player.getUniqueId())) {
                plugin.getPrivateRoundManager().deletePrivateRound(privateRound);
                player.sendMessage(BedWars.PREFIX + "§eDeine private Runde wurde gelöscht, da du einer offiziellen Runde beigetreten bist.");
            } else {
                plugin.getPrivateRoundManager().leavePrivateRound(player);
            }
        }

        players.add(player.getUniqueId());

        BedWarsTeam team = getSmallestTeam();
        if (team != null) {
            team.addPlayer(player.getUniqueId());
            player.teleport(lobby);

            player.setGameMode(org.bukkit.GameMode.SURVIVAL);

            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            giveTeamSelector(player);

            plugin.getNametagManager().setGameNametag(player, team);

            broadcastMessage("§8[§a+§8] " + getPlayerDisplayName(player) + " §7hat das Spiel betreten §8(§e" +
                    players.size() + "§8/§e" + maxPlayers + "§8)");

            // Update scoreboard for the player who joined
            plugin.getScoreboardManager().updateScoreboard(this);

            if (players.size() >= minPlayers && gameState == GameState.WAITING) {
                startCountdown();
            }
        }
    }

    public void removePlayer(Player player, boolean sendMessage) {
        UUID uuid = player.getUniqueId();

        if (!players.contains(uuid)) return;

        players.remove(uuid);
        spectators.remove(uuid);
        deadPlayers.remove(uuid);

        for (BedWarsTeam team : teams.values()) {
            team.removePlayer(uuid);
        }

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        plugin.getNametagManager().resetNametag(player);

        if (sendMessage) {
            broadcastMessage("§8[§c-§8] " + getPlayerDisplayName(player) + " §7hat das Spiel verlassen §8(§e" +
                    players.size() + "§8/§e" + maxPlayers + "§8)");
        }

        if (players.size() < minPlayers && gameState == GameState.STARTING) {
            stopCountdown();
        }

        if (gameState == GameState.INGAME) {
            checkWinCondition();
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Villager &&
                event.getEntity().hasMetadata("bw_npc")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager &&
                event.getRightClicked().hasMetadata("bw_npc")) {
            event.setCancelled(true);
        }
    }

    public void endGame(BedWarsTeam winner) {
        gameState = GameState.ENDING;

        if (countdownTask != null) {
            countdownTask.cancel();
        }

        broadcastMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━");
        broadcastMessage("§f");
        broadcastMessage("              §6§lSPIEL BEENDET");
        broadcastMessage("§f");
        if (winner != null) {
            broadcastMessage("         §eGewinner: " + winner.getColoredName());
        }
        broadcastMessage("§f");
        broadcastMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━");

        updateStats(winner);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (UUID uuid : new HashSet<>(players)) {
                Player player = plugin.getServer().getPlayer(uuid);
                if (player != null) {
                    removePlayer(player, false);
                    // Remove from GameManager's playerArenas map so lobby items work again
                    plugin.getGameManager().removePlayerFromArenaMap(uuid);
                    plugin.getLobbyManager().sendToLobby(player);
                    // Re-enable fly for players with permission
                    if (player.hasPermission("homemc.fly")) {
                        player.setAllowFlight(true);
                    }
                }
            }
            reset();
        }, 200L);
    }

    public void reset() {
        gameState = GameState.WAITING;
        gamePhase = GamePhase.WAITING;
        goldEnabled = false;
        goldTimer = 0;
        countdown = 60;

        cleanupMap();
        restoreBeds();

        players.clear();
        spectators.clear();
        deadPlayers.clear();
        placedBlocks.clear();
        goldVotes.clear();
        removeShopVillagers();

        for (BedWarsTeam team : teams.values()) {
            team.reset();
        }

        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
    }

    private void restoreBeds() {
        for (BedWarsTeam team : teams.values()) {
            Location bedLoc = team.getBedLocation();
            if (bedLoc != null && bedLoc.getWorld() != null) {
                org.bukkit.block.Block bedBlock = bedLoc.getBlock();

                if (bedBlock.getType() != Material.BED_BLOCK) {
                    byte bedData = getBedDataForTeam(team);

                    bedBlock.setType(Material.BED_BLOCK);
                    bedBlock.setData(bedData);

                    org.bukkit.block.Block headBlock = getSecondBedBlock(bedBlock, bedData);
                    if (headBlock != null && headBlock.getType() != Material.BED_BLOCK) {
                        headBlock.setType(Material.BED_BLOCK);
                        headBlock.setData((byte) (bedData | 0x8));
                    }

                    plugin.getLogger().info("Restored bed for team " + team.getColor().name());
                }
            }
        }
    }

    private org.bukkit.block.Block getSecondBedBlock(org.bukkit.block.Block footBlock, byte data) {
        int direction = data & 0x3;
        switch (direction) {
            case 0: return footBlock.getRelative(0, 0, 1);
            case 1: return footBlock.getRelative(-1, 0, 0);
            case 2: return footBlock.getRelative(0, 0, -1);
            case 3: return footBlock.getRelative(1, 0, 0);
            default: return footBlock.getRelative(0, 0, 1);
        }
    }

    private byte getBedDataForTeam(BedWarsTeam team) {
        return 0;
    }

    private void removeShopVillagers() {
        for (BedWarsTeam team : teams.values()) {
            if (team.getShopVillagerEntity() != null && !team.getShopVillagerEntity().isDead()) {
                team.getShopVillagerEntity().remove();
            }
            team.setShopVillagerEntity(null);
        }
    }

    private void startCountdown() {
        gameState = GameState.STARTING;
        countdown = 60;

        countdownTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (players.size() < minPlayers) {
                stopCountdown();
                return;
            }

            if (countdown <= 0) {
                countdownTask.cancel();
                startGame();
                return;
            }

            if (countdown == 60 || countdown == 30 || countdown == 15 || countdown == 10 ||
                    countdown <= 5) {
                broadcastMessage("§aDas Spiel startet in §e" + countdown + " §aSekunde" +
                        (countdown != 1 ? "n" : "") + "§a!");
            }

            countdown--;
        }, 20L, 20L);
    }

    public void forcestart() {
        gameState = GameState.STARTING;
        countdown = 5;

        countdownTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (players.size() < minPlayers) {
                stopCountdown();
                return;
            }

            if (countdown <= 0) {
                countdownTask.cancel();
                startGame();
                return;
            }

            broadcastMessage("§aDas Spiel startet in §e" + countdown + " §aSekunde" +
                    (countdown != 1 ? "n" : "") + "§a!");

            countdown--;
        }, 20L, 20L);
    }

    private void stopCountdown() {
        gameState = GameState.WAITING;
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        broadcastMessage("§cCountdown abgebrochen! Nicht genug Spieler.");
    }

    private BedWarsTeam getSmallestTeam() {
        return teams.values().stream()
                .min(Comparator.comparingInt(t -> t.getPlayers().size()))
                .orElse(null);
    }

    private void giveTeamSelector(Player player) {
        player.getInventory().setItem(0, plugin.getTeamManager().getTeamSelectorItem());

        // Give leave item in slot 8
        org.bukkit.inventory.ItemStack leaveItem = new org.bukkit.inventory.ItemStack(Material.MAGMA_CREAM);
        org.bukkit.inventory.meta.ItemMeta leaveMeta = leaveItem.getItemMeta();
        leaveMeta.setDisplayName("§c§lSpiel verlassen");
        leaveMeta.setLore(java.util.Arrays.asList("§7Klicke um das Spiel zu verlassen", "§7und zur Lobby zurückzukehren"));
        leaveItem.setItemMeta(leaveMeta);
        player.getInventory().setItem(8, leaveItem);
    }

    private void giveStartItems(Player player, BedWarsTeam team) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
    }

    private void checkWinCondition() {
        List<BedWarsTeam> aliveTeams = new ArrayList<>();

        for (BedWarsTeam team : teams.values()) {
            if (team.isAlive()) {
                aliveTeams.add(team);
            }
        }

        if (aliveTeams.size() == 1) {
            endGame(aliveTeams.get(0));
        } else if (aliveTeams.isEmpty()) {
            endGame(null);
        }
    }

    public void startGame() {
        gameState = GameState.INGAME;
        gamePhase = GamePhase.PROTECTION;
        startTime = System.currentTimeMillis();

        cleanupMap();

        setupWorldBorder();

        broadcastMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━");
        broadcastMessage("§f");
        broadcastMessage("                §6§lBEDWARS");
        broadcastMessage("§f");
        broadcastMessage("        §eSchütze dein Bett und zerstöre");
        broadcastMessage("        §edie Betten deiner Gegner!");
        broadcastMessage("§f");

        if (!goldEnabled) {
            broadcastMessage("        §eGold ist §cdeaktiviert");
        } else {
            broadcastMessage("        §eGold ist §aaktiviert");
        }

        broadcastMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━");

        spawnShopVillagers();

        for (BedWarsTeam team : teams.values()) {
            for (UUID uuid : team.getPlayers()) {
                Player player = plugin.getServer().getPlayer(uuid);
                if (player != null) {
                    player.teleport(team.getSpawn());
                    giveStartItems(player, team);
                }
            }
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (BedWarsTeam team : teams.values()) {
                for (UUID uuid : team.getPlayers()) {
                    Player player = plugin.getServer().getPlayer(uuid);
                    if (player != null) {
                        plugin.getNametagManager().setGameNametag(player, team);
                    }
                }
            }
        }, 5L);

        plugin.getScoreboardManager().createGameScoreboard(this);
    }

    private void cleanupMap() {
        plugin.getLogger().info("Cleaning up arena " + name);

        for (Location loc : new HashSet<>(placedBlocks)) {
            loc.getBlock().setType(Material.AIR);
        }
        placedBlocks.clear();

        if (lobby != null && lobby.getWorld() != null) {
            for (org.bukkit.Chunk chunk : lobby.getWorld().getLoadedChunks()) {
                for (org.bukkit.block.BlockState state : chunk.getTileEntities()) {
                    if (state instanceof org.bukkit.block.Chest) {
                        org.bukkit.block.Chest chest = (org.bukkit.block.Chest) state;
                        if (isInArenaArea(chest.getLocation())) {
                            chest.getInventory().clear();
                        }
                    }
                }
            }
        }

        for (UUID uuid : players) {
            org.bukkit.entity.Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                player.getEnderChest().clear();
            }
        }

        if (lobby != null && lobby.getWorld() != null) {
            for (org.bukkit.entity.Entity entity : lobby.getWorld().getEntities()) {
                if (entity instanceof org.bukkit.entity.Item) {
                    if (isInArenaArea(entity.getLocation())) {
                        entity.remove();
                    }
                }
                else if (entity instanceof org.bukkit.entity.TNTPrimed) {
                    if (isInArenaArea(entity.getLocation())) {
                        entity.remove();
                    }
                }
            }
        }

        for (GeneratorData gen : generators.values()) {
            if (gen.getLocation().getWorld() != null) {
                for (org.bukkit.entity.Entity entity : gen.getLocation().getWorld().getNearbyEntities(
                        gen.getLocation(), 3, 3, 3)) {
                    if (entity instanceof org.bukkit.entity.Item) {
                        entity.remove();
                    }
                    // Remove hologram armor stands near generators
                    if (entity instanceof org.bukkit.entity.ArmorStand) {
                        org.bukkit.entity.ArmorStand armorStand = (org.bukkit.entity.ArmorStand) entity;
                        if (!armorStand.isVisible() || armorStand.isSmall() || armorStand.hasGravity() == false) {
                            entity.remove();
                        }
                    }
                }
            }
        }

        plugin.getLogger().info("Cleanup completed for arena " + name);
    }

    private boolean isInArenaArea(Location loc) {
        if (lobby == null) return false;
        return loc.getWorld().equals(lobby.getWorld()) &&
                loc.distance(lobby) < 200;
    }

    private void setupWorldBorder() {
        if (lobby == null || lobby.getWorld() == null) return;

        org.bukkit.WorldBorder border = lobby.getWorld().getWorldBorder();
        border.setCenter(lobby);
        border.setSize(300);
        border.setWarningDistance(10);

        plugin.getLogger().info("WorldBorder set for arena " + name + " at " +
                lobby.getBlockX() + ", " + lobby.getBlockZ());
    }

    private final Map<UUID, Boolean> goldVotes = new ConcurrentHashMap<>();

    public void voteGold(Player player, boolean enable) {
        goldVotes.put(player.getUniqueId(), enable);

        int enableVotes = 0;
        int disableVotes = 0;

        for (Boolean vote : goldVotes.values()) {
            if (vote) enableVotes++;
            else disableVotes++;
        }

        goldEnabled = enableVotes > disableVotes;

        broadcastMessage("§7" + player.getName() + " §7hat für §e" +
                (enable ? "Gold aktiviert" : "Gold deaktiviert") + " §7gestimmt!");
        broadcastMessage("§7Aktuell: §a" + enableVotes + " §7dafür, §c" + disableVotes + " §7dagegen");
    }

    public int getGoldVotesYes() {
        int count = 0;
        for (Boolean vote : goldVotes.values()) {
            if (vote) count++;
        }
        return count;
    }

    public int getGoldVotesNo() {
        int count = 0;
        for (Boolean vote : goldVotes.values()) {
            if (!vote) count++;
        }
        return count;
    }

    private void spawnShopVillagers() {
        for (BedWarsTeam team : teams.values()) {
            if (team.getShopVillager() != null) {
                Location loc = team.getShopVillager();

                org.bukkit.entity.Villager villager = (org.bukkit.entity.Villager)
                        loc.getWorld().spawnEntity(loc, org.bukkit.entity.EntityType.VILLAGER);

                villager.setCustomName("§6§lSHOP");
                villager.setCustomNameVisible(true);
                villager.setCanPickupItems(false);
                villager.setProfession(org.bukkit.entity.Villager.Profession.LIBRARIAN);

                try {
                    Object nmsEntity = villager.getClass().getMethod("getHandle").invoke(villager);
                    Object goalSelector = nmsEntity.getClass().getDeclaredField("goalSelector").get(nmsEntity);
                    Object targetSelector = nmsEntity.getClass().getDeclaredField("targetSelector").get(nmsEntity);

                    java.lang.reflect.Field goalsField = goalSelector.getClass().getDeclaredField("b");
                    goalsField.setAccessible(true);
                    goalsField.set(goalSelector, new java.util.ArrayList<>());

                    java.lang.reflect.Field targetsField = targetSelector.getClass().getDeclaredField("b");
                    targetsField.setAccessible(true);
                    targetsField.set(targetSelector, new java.util.ArrayList<>());

                } catch (Exception e) {
                    plugin.getLogger().warning("Konnte AI nicht via NMS deaktivieren: " + e.getMessage());
                }

                villager.setMetadata("bw_shop", new org.bukkit.metadata.FixedMetadataValue(plugin, team.getColor().name()));

                Location blockLoc = loc.clone().subtract(0, 1, 0);
                blockLoc.getBlock().setType(org.bukkit.Material.BARRIER);

                team.setShopVillagerEntity(villager);
            }
        }
    }



    public void forceEnd() {
        gameState = GameState.ENDING;

        if (countdownTask != null) {
            countdownTask.cancel();
        }

        cleanupMap();

        for (UUID uuid : new HashSet<>(players)) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                removePlayer(player, false);
                // Remove from GameManager's playerArenas map so lobby items work again
                plugin.getGameManager().removePlayerFromArenaMap(uuid);
                plugin.getLobbyManager().sendToLobby(player);
                // Re-enable fly for players with permission
                if (player.hasPermission("homemc.fly")) {
                    player.setAllowFlight(true);
                }
            }
        }

        reset();
    }

    private void updateStats(BedWarsTeam winner) {
        for (UUID uuid : players) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player == null) continue;

            boolean isWinner = winner != null && winner.getPlayers().contains(uuid);
            plugin.getStatsManager().updateStats(uuid, isWinner);
        }
    }

    private String getPlayerDisplayName(Player player) {
        if (plugin.getCore().getNickManager().isNicked(player.getUniqueId())) {
            return "§7" + plugin.getCore().getNickManager().getDisplayName(player.getUniqueId());
        }
        return plugin.getCore().getRankManager().getRankColor(player) + player.getName();
    }

    public void broadcastMessage(String message) {
        for (UUID uuid : players) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                player.sendMessage(message);
            }
        }
    }

    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public int getMinPlayers() { return minPlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public LobbyManager.GameMode getGameMode() { return gameMode; }
    public void setGameMode(LobbyManager.GameMode gameMode) { this.gameMode = gameMode; }
    public GameState getGameState() { return gameState; }
    public void setGameState(GameState gameState) { this.gameState = gameState; }
    public GamePhase getGamePhase() { return gamePhase; }
    public void setGamePhase(GamePhase gamePhase) { this.gamePhase = gamePhase; }
    public Location getLobby() { return lobby; }
    public void setLobby(Location lobby) { this.lobby = lobby; }
    public Location getSpectator() { return spectator; }
    public void setSpectator(Location spectator) { this.spectator = spectator; }
    public Map<String, BedWarsTeam> getTeams() { return teams; }
    public Set<UUID> getPlayers() { return players; }
    public Set<UUID> getSpectators() { return spectators; }
    public Set<UUID> getDeadPlayers() { return deadPlayers; }
    public Set<Location> getPlacedBlocks() { return placedBlocks; }
    public Map<Location, GeneratorData> getGenerators() { return generators; }
    public boolean isGoldEnabled() { return goldEnabled; }
    public int getCountdown() { return countdown; }

    public static class GeneratorData {
        private final GeneratorType type;
        private final Location location;
        private int timer;

        public GeneratorData(GeneratorType type, Location location) {
            this.type = type;
            this.location = location;
            this.timer = 0;
        }

        public GeneratorType getType() { return type; }
        public Location getLocation() { return location; }
        public int getTimer() { return timer; }
        public void setTimer(int timer) { this.timer = timer; }
        public void incrementTimer() { this.timer++; }
    }

    public enum GeneratorType {
        BRONZE(1),
        IRON(5),
        GOLD(10);

        private final int spawnRate;

        GeneratorType(int spawnRate) {
            this.spawnRate = spawnRate;
        }

        public int getSpawnRate() { return spawnRate; }
    }
}