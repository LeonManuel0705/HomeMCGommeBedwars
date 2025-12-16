package me.leon.bedwars.game;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.managers.GadgetManager;
import me.leon.bedwars.managers.MapData;
import me.leon.bedwars.managers.StatsManager;
import me.leon.bedwars.managers.TeamData;
import me.leon.bedwars.utils.ItemBuilder;
import me.leon.core.utils.CoreAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Game {

    private final BedWars plugin;
    private final String gameId;
    private final MapData mapData;

    private GameState state;
    private int lobbyCountdown;
    private int gameTime;

    private final Map<UUID, TeamData> playerTeams;
    private final Map<TeamData, Set<UUID>> teamPlayers;
    private final Map<TeamData, Boolean> bedStatus;
    private final Set<UUID> alivePlayers;
    private final Set<UUID> spectators;
    private final Map<UUID, Integer> kills;
    private final Map<UUID, Integer> deaths;
    private final Map<UUID, Integer> bedsDestroyed;

    private final Set<Location> placedBlocks;

    private boolean forceStopped = false; // Neues Flag

    private BukkitRunnable gameTask;

    public Game(BedWars plugin, MapData mapData) {
        this.plugin = plugin;
        this.gameId = UUID.randomUUID().toString().substring(0, 8);
        this.mapData = mapData;

        this.state = GameState.LOBBY;
        this.lobbyCountdown = 60;
        this.gameTime = 0;

        this.playerTeams = new ConcurrentHashMap<>();
        this.teamPlayers = new ConcurrentHashMap<>();
        this.bedStatus = new ConcurrentHashMap<>();
        this.alivePlayers = ConcurrentHashMap.newKeySet();
        this.spectators = ConcurrentHashMap.newKeySet();
        this.kills = new ConcurrentHashMap<>();
        this.deaths = new ConcurrentHashMap<>();
        this.bedsDestroyed = new ConcurrentHashMap<>();
        this.placedBlocks = ConcurrentHashMap.newKeySet();

        for (TeamData team : mapData.getTeams()) {
            teamPlayers.put(team, ConcurrentHashMap.newKeySet());
            bedStatus.put(team, true);
        }

        fixWeatherAndTime();
    }

    private void fixWeatherAndTime() {
        World world = mapData.getLobby().getWorld();
        if (world != null) {
            world.setStorm(false);
            world.setThundering(false);
            world.setWeatherDuration(Integer.MAX_VALUE);
            world.setTime(6000);
        }
    }

    public void addPlayer(Player player) {
        if (state != GameState.LOBBY) {
            player.sendMessage(BedWars.PREFIX + "§cDieses Spiel läuft bereits!");
            return;
        }

        if (getPlayers().size() >= mapData.getMaxPlayers()) {
            player.sendMessage(BedWars.PREFIX + "§cDieses Spiel ist voll!");
            return;
        }

        TeamData team = getSmallestTeam();
        if (team == null) {
            player.sendMessage(BedWars.PREFIX + "§cKein Team verfügbar!");
            return;
        }

        playerTeams.put(player.getUniqueId(), team);
        teamPlayers.get(team).add(player.getUniqueId());

        player.teleport(mapData.getLobby());
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        // AutoNick aktivieren
        if (CoreAPI.isCoreAvailable()) {
            if (CoreAPI.getCore().getNickManager().hasAutoNick(player.getUniqueId())) {
                if (!CoreAPI.getCore().getNickManager().isNicked(player.getUniqueId())) {
                    CoreAPI.getCore().getNickManager().nickPlayer(player);
                }
            }
        }

        giveTeamSelector(player);

        broadcast("§a" + getPlayerDisplayName(player) + " §7hat das Spiel betreten! §8(§e" +
                getPlayers().size() + "§7/§e" + mapData.getMaxPlayers() + "§8)");

        updateScoreboard(player);

        if (getPlayers().size() >= mapData.getMinPlayers() && gameTask == null) {
            startLobbyCountdown();
        }
    }

    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();

        TeamData team = playerTeams.remove(uuid);
        if (team != null) {
            teamPlayers.get(team).remove(uuid);
        }

        alivePlayers.remove(uuid);
        spectators.remove(uuid);

        player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);

        resetNameTag(player);

        if (state == GameState.LOBBY) {
            broadcast("§c" + getPlayerDisplayName(player) + " §7hat das Spiel verlassen! §8(§e" +
                    getPlayers().size() + "§7/§e" + mapData.getMaxPlayers() + "§8)");
        } else if (state == GameState.INGAME) {
            checkWinCondition();
        }
    }

    private void giveTeamSelector(Player player) {
        ItemStack selector = new ItemBuilder(Material.BED)
                .setDisplayName("§aTeam Auswahl §7(Rechtsklick)")
                .setLore("§7Wähle dein Team aus!")
                .build();
        player.getInventory().setItem(0, selector);

        ItemStack stats = new ItemBuilder(Material.PAPER)
                .setDisplayName("§6Deine Statistiken §7(Rechtsklick)")
                .build();
        player.getInventory().setItem(4, stats);

        ItemStack leave = new ItemBuilder(Material.INK_SACK, 1, (short) 1)
                .setDisplayName("§cZurück zur Lobby §7(Rechtsklick)")
                .build();
        player.getInventory().setItem(8, leave);
    }

    private void startLobbyCountdown() {
        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (state != GameState.LOBBY) {
                    cancel();
                    return;
                }

                if (getPlayers().size() < mapData.getMinPlayers()) {
                    broadcast("§cZu wenig Spieler! Countdown gestoppt.");
                    lobbyCountdown = 60;
                    cancel();
                    gameTask = null;
                    return;
                }

                lobbyCountdown--;

                for (Player player : getOnlinePlayers()) {
                    updateScoreboard(player);
                }

                if (lobbyCountdown == 30 || lobbyCountdown == 20 || lobbyCountdown == 10 ||
                        lobbyCountdown == 5 || lobbyCountdown <= 3) {
                    broadcast("§eDas Spiel startet in §c" + lobbyCountdown + " §eSekunde" +
                            (lobbyCountdown == 1 ? "" : "n") + "!");
                    playSound(Sound.NOTE_PLING);
                }

                if (lobbyCountdown <= 0) {
                    cancel();
                    startGame();
                }
            }
        };
        gameTask.runTaskTimer(plugin, 20L, 20L);
    }

    public void forceStart() {
        if (gameTask != null) {
            gameTask.cancel();
        }
        lobbyCountdown = 0;
        startGame();
    }

    public void forceEnd() {
        forceStopped = true; // Flag setzen
        endGame(null);
    }

    private void startGame() {
        state = GameState.INGAME;

        broadcast("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        broadcast("§c§lBedWars");
        broadcast("");
        broadcast("§7Schütze dein Bett und zerstöre");
        broadcast("§7die Betten deiner Gegner!");
        broadcast("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        for (Player player : getOnlinePlayers()) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);

            TeamData team = playerTeams.get(player.getUniqueId());
            if (team != null) {
                player.teleport(team.getSpawn());
                alivePlayers.add(player.getUniqueId());

                updateNameTag(player, team);
            }
        }

        plugin.getGeneratorManager().startGenerators(this);
        clearChests();

        startGameLoop();
    }

    private void clearChests() {
        World world = mapData.getLobby().getWorld();
        for (Chunk chunk : world.getLoadedChunks()) {
            for (org.bukkit.block.BlockState state : chunk.getTileEntities()) {
                if (state instanceof org.bukkit.block.Chest) {
                    org.bukkit.block.Chest chest = (org.bukkit.block.Chest) state;
                    chest.getInventory().clear();
                }
            }
        }
    }

    private void updateNameTag(Player player, TeamData team) {
        String displayName = getPlayerDisplayName(player);

        for (Player online : getOnlinePlayers()) {
            Scoreboard board = online.getScoreboard();
            if (board == null || board == Bukkit.getScoreboardManager().getMainScoreboard()) {
                board = Bukkit.getScoreboardManager().getNewScoreboard();
                online.setScoreboard(board);
            }

            Team sbTeam = board.getTeam(team.getName());
            if (sbTeam == null) {
                sbTeam = board.registerNewTeam(team.getName());
                sbTeam.setPrefix(team.getColor());
                sbTeam.setCanSeeFriendlyInvisibles(true);
            }

            if (!sbTeam.hasEntry(displayName)) {
                sbTeam.addEntry(displayName);
            }
        }
    }

    private void resetNameTag(Player player) {
        String displayName = getPlayerDisplayName(player);

        for (Player online : Bukkit.getOnlinePlayers()) {
            Scoreboard board = online.getScoreboard();
            if (board != null) {
                for (Team team : board.getTeams()) {
                    if (team.hasEntry(displayName)) {
                        team.removeEntry(displayName);
                    }
                }
            }
        }
    }

    private void startGameLoop() {
        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (state != GameState.INGAME) {
                    cancel();
                    return;
                }

                gameTime++;

                for (Player player : getOnlinePlayers()) {
                    updateScoreboard(player);
                }
            }
        };
        gameTask.runTaskTimer(plugin, 20L, 20L);
    }

    public void handlePlayerDeath(Player player, Player killer) {
        UUID uuid = player.getUniqueId();
        TeamData team = playerTeams.get(uuid);

        deaths.put(uuid, deaths.getOrDefault(uuid, 0) + 1);

        if (killer != null) {
            kills.put(killer.getUniqueId(), kills.getOrDefault(killer.getUniqueId(), 0) + 1);
            plugin.getGadgetManager().activateGadget(killer, GadgetManager.GadgetType.KILL_MESSAGE);
        }

        if (!hasBed(team)) {
            alivePlayers.remove(uuid);
            spectators.add(uuid);

            String deathMsg = team.getColor() + "§l" + getPlayerDisplayName(player);
            if (killer != null) {
                TeamData killerTeam = playerTeams.get(killer.getUniqueId());

                String customMsg = plugin.getGadgetManager().getKillMessage(killer.getUniqueId(), getPlayerDisplayName(player));
                if (customMsg != null) {
                    broadcast(customMsg);
                } else {
                    deathMsg += " §7wurde von " + killerTeam.getColor() + "§l" +
                            getPlayerDisplayName(killer) + " §7getötet!";
                    deathMsg += " §b§lFINAL KILL!";
                    broadcast(deathMsg);
                }
            } else {
                deathMsg += " §7ist gestorben!";
                deathMsg += " §c§lFINAL KILL!";
                broadcast(deathMsg);
            }

            plugin.getSpectatorManager().setSpectator(player, this);

            checkWinCondition();
        } else {
            String deathMsg = team.getColor() + "§l" + getPlayerDisplayName(player);
            if (killer != null) {
                TeamData killerTeam = playerTeams.get(killer.getUniqueId());
                deathMsg += " §7wurde von " + killerTeam.getColor() + "§l" +
                        getPlayerDisplayName(killer) + " §7getötet!";
            } else {
                deathMsg += " §7ist gestorben!";
            }

            broadcast(deathMsg);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && state == GameState.INGAME) {
                    player.spigot().respawn();
                    player.teleport(team.getSpawn());
                }
            }, 40L);
        }
    }

    public void destroyBed(TeamData team, Player breaker) {
        if (!bedStatus.get(team)) {
            return;
        }

        bedStatus.put(team, false);

        TeamData breakerTeam = playerTeams.get(breaker.getUniqueId());
        bedsDestroyed.put(breaker.getUniqueId(), bedsDestroyed.getOrDefault(breaker.getUniqueId(), 0) + 1);

        broadcast("§f§lBETT ZERSTÖRUNG > " + team.getColor() + "§l" + team.getName() +
                " Bett §7wurde von " + breakerTeam.getColor() + "§l" +
                getPlayerDisplayName(breaker) + " §7zerstört!");

        playSound(Sound.ENDERDRAGON_GROWL);

        plugin.getGadgetManager().activateGadget(breaker, GadgetManager.GadgetType.BED_DESTROY);

        for (UUID uuid : teamPlayers.get(team)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendTitle("§c§lBETT ZERSTÖRT!", "§7Du kannst nicht mehr respawnen!");
                player.playSound(player.getLocation(), Sound.WITHER_DEATH, 1.0f, 1.0f);
            }
        }
    }

    public void trackPlacedBlock(Block block) {
        placedBlocks.add(block.getLocation());
    }

    public boolean isPlacedBlock(Block block) {
        return placedBlocks.contains(block.getLocation());
    }

    public void removeTrackedBlock(Block block) {
        placedBlocks.remove(block.getLocation());
    }

    private void checkWinCondition() {
        List<TeamData> aliveTeams = new ArrayList<>();

        for (TeamData team : mapData.getTeams()) {
            boolean hasAlivePlayers = false;
            for (UUID uuid : teamPlayers.get(team)) {
                if (alivePlayers.contains(uuid)) {
                    hasAlivePlayers = true;
                    break;
                }
            }
            if (hasAlivePlayers) {
                aliveTeams.add(team);
            }
        }

        if (aliveTeams.size() == 1) {
            endGame(aliveTeams.get(0));
        } else if (aliveTeams.isEmpty()) {
            endGame(null);
        }
    }

    private void endGame(TeamData winnerTeam) {
        state = GameState.ENDING;

        if (gameTask != null) {
            gameTask.cancel();
        }

        plugin.getGeneratorManager().stopGenerators(gameId);

        broadcast("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (winnerTeam != null) {
            broadcast("§e§lSIEG!");
            broadcast("");
            broadcast(winnerTeam.getColor() + "§l" + winnerTeam.getName() + " §ahat gewonnen!");
            broadcast("");

            Set<UUID> winners = teamPlayers.get(winnerTeam);
            for (UUID uuid : winners) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.sendTitle("§6§lVICTORY!", "§aDu hast gewonnen!");
                    giveRewards(player, true);

                    plugin.getGadgetManager().activateGadget(player, GadgetManager.GadgetType.WIN_EFFECT);
                    plugin.getGadgetManager().activateGadget(player, GadgetManager.GadgetType.PARTICLE);
                }
            }

            for (Player player : getOnlinePlayers()) {
                if (!winners.contains(player.getUniqueId())) {
                    player.sendTitle("§c§lGAME OVER!", "§7Vielleicht beim nächsten Mal!");
                    giveRewards(player, false);
                }
            }
        } else {
            broadcast("§7Das Spiel endet unentschieden!");
        }

        broadcast("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Nur Stats updaten, wenn nicht force-stopped
        if (!forceStopped) {
            updateAllStats(winnerTeam);
        }

        recoverMap();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player player : getOnlinePlayers()) {
                resetNameTag(player);

                if (CoreAPI.isCoreAvailable()) {
                    if (CoreAPI.getCore().getNickManager().isNicked(player.getUniqueId())) {
                        CoreAPI.getCore().getNickManager().unnickPlayer(player);
                    }
                }

                plugin.getLobbyManager().sendToLobby(player);
            }
            plugin.getGameManager().removeGame(this);
        }, 200L);
    }

    private void recoverMap() {
        for (Location loc : placedBlocks) {
            Block block = loc.getBlock();
            if (block != null) {
                block.setType(Material.AIR);
            }
        }
        placedBlocks.clear();

        for (TeamData team : mapData.getTeams()) {
            Location bedLoc = team.getBed();
            if (bedLoc != null) {
                bedLoc.getBlock().setType(Material.BED_BLOCK);
            }
        }
    }

    private void giveRewards(Player player, boolean won) {
        if (!CoreAPI.isCoreAvailable()) return;

        UUID uuid = player.getUniqueId();

        int baseCoins = won ? 50 : 10;
        int killCoins = kills.getOrDefault(uuid, 0) * 5;
        int bedCoins = bedsDestroyed.getOrDefault(uuid, 0) * 10;

        int totalCoins = baseCoins + killCoins + bedCoins;

        plugin.getStatsManager().addCoins(uuid, totalCoins);

        player.sendMessage("");
        player.sendMessage("§6§l+ " + totalCoins + " Coins");
        player.sendMessage("  §7Spiel: §e+" + baseCoins);
        if (killCoins > 0) player.sendMessage("  §7Kills: §e+" + killCoins);
        if (bedCoins > 0) player.sendMessage("  §7Betten: §e+" + bedCoins);
        player.sendMessage("");
    }

    private void updateAllStats(TeamData winnerTeam) {
        for (Player player : getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            TeamData team = playerTeams.get(uuid);

            boolean won = team != null && team.equals(winnerTeam);

            plugin.getStatsManager().updateStats(
                    uuid,
                    player.getName(),
                    kills.getOrDefault(uuid, 0),
                    deaths.getOrDefault(uuid, 0),
                    won ? 1 : 0,
                    won ? 0 : 1,
                    bedsDestroyed.getOrDefault(uuid, 0)
            );

            StatsManager.PlayerStats stats = plugin.getStatsManager().getStats(uuid);
            if (stats != null) {
                plugin.getAchievementManager().checkAchievements(player, stats);
            }
        }
    }

    private void updateScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("bw", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName("§c§lBED WARS");

        int line = 15;

        if (state == GameState.LOBBY) {
            setScore(obj, "§1", line--);
            setScore(obj, "§fMap: §a" + mapData.getName(), line--);
            setScore(obj, "§fSpieler: §a" + getPlayers().size() + "§7/§a" + mapData.getMaxPlayers(), line--);
            setScore(obj, "§2", line--);

            if (getPlayers().size() >= mapData.getMinPlayers()) {
                setScore(obj, "§fStart in: §a" + lobbyCountdown + "s", line--);
            } else {
                setScore(obj, "§fWarte auf Spieler...", line--);
            }
        } else {
            setScore(obj, "§1", line--);
            setScore(obj, "§fZeit: §a" + formatTime(gameTime), line--);
            setScore(obj, "§2", line--);

            for (TeamData team : mapData.getTeams()) {
                String status = hasBed(team) ? "§a✓" : "§c✗";
                int alive = getAlivePlayersInTeam(team);

                setScore(obj, team.getColor() + team.getShortName() + " " + status + " §7" + alive, line--);
            }

            setScore(obj, "§3", line--);
            setScore(obj, "§fKills: §a" + kills.getOrDefault(player.getUniqueId(), 0), line--);
        }

        setScore(obj, "§4", line--);
        setScore(obj, "§eplay.homemc.org", line--);

        player.setScoreboard(board);
    }

    private void setScore(Objective obj, String text, int score) {
        obj.getScore(text).setScore(score);
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    private int getAlivePlayersInTeam(TeamData team) {
        int count = 0;
        for (UUID uuid : teamPlayers.get(team)) {
            if (alivePlayers.contains(uuid)) {
                count++;
            }
        }
        return count;
    }

    private TeamData getSmallestTeam() {
        TeamData smallest = null;
        int minSize = Integer.MAX_VALUE;

        for (TeamData team : mapData.getTeams()) {
            int size = teamPlayers.get(team).size();
            if (size < minSize) {
                minSize = size;
                smallest = team;
            }
        }

        return smallest;
    }

    public void broadcast(String message) {
        for (Player player : getOnlinePlayers()) {
            player.sendMessage(BedWars.PREFIX + message);
        }
    }

    private void playSound(Sound sound) {
        for (Player player : getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
    }

    private String getPlayerDisplayName(Player player) {
        if (CoreAPI.isCoreAvailable()) {
            if (CoreAPI.getCore().getNickManager().isNicked(player.getUniqueId())) {
                return CoreAPI.getCore().getNickManager().getDisplayName(player.getUniqueId());
            }
            return CoreAPI.getRankColor(player) + player.getName();
        }
        return player.getName();
    }

    public Set<Player> getOnlinePlayers() {
        Set<Player> online = new HashSet<>();
        for (UUID uuid : getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                online.add(player);
            }
        }
        return online;
    }

    public Set<UUID> getPlayers() {
        return playerTeams.keySet();
    }

    public boolean hasBed(TeamData team) {
        return bedStatus.getOrDefault(team, false);
    }

    public boolean isAlive(UUID uuid) {
        return alivePlayers.contains(uuid);
    }

    public boolean isSpectator(UUID uuid) {
        return spectators.contains(uuid);
    }

    public TeamData getPlayerTeam(UUID uuid) {
        return playerTeams.get(uuid);
    }

    public void setPlayerTeam(Player player, TeamData team) {
        TeamData oldTeam = playerTeams.get(player.getUniqueId());
        if (oldTeam != null) {
            teamPlayers.get(oldTeam).remove(player.getUniqueId());
        }

        playerTeams.put(player.getUniqueId(), team);
        teamPlayers.get(team).add(player.getUniqueId());

        player.sendMessage(BedWars.PREFIX + "§7Du bist jetzt in Team " + team.getColor() + team.getName());

        giveTeamSelector(player);
    }

    public GameState getState() {
        return state;
    }

    public String getGameId() {
        return gameId;
    }

    public MapData getMapData() {
        return mapData;
    }

    public void shutdown() {
        forceStopped = true;

        if (gameTask != null) {
            gameTask.cancel();
        }

        plugin.getGeneratorManager().stopGenerators(gameId);

        for (Player player : getOnlinePlayers()) {
            resetNameTag(player);

            if (CoreAPI.isCoreAvailable()) {
                if (CoreAPI.getCore().getNickManager().isNicked(player.getUniqueId())) {
                    CoreAPI.getCore().getNickManager().unnickPlayer(player);
                }
            }

            plugin.getLobbyManager().sendToLobby(player);
        }

        recoverMap();
    }
}