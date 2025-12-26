package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import me.leon.bedwars.lobby.LobbyManager;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
import me.leon.bedwars.managers.TeamManager.TeamColor;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PrivateRoundManager {

    private final BedWars plugin;
    private final Map<UUID, PrivateRound> privateRounds = new ConcurrentHashMap<>();
    private final Map<UUID, PrivateRound> playerToRound = new ConcurrentHashMap<>();

    public PrivateRoundManager(BedWars plugin) {
        this.plugin = plugin;
    }

    public PrivateRound createPrivateRound(Player creator, String mapName) {
        if (!creator.hasPermission("bedwars.private")) {
            creator.sendMessage(BedWars.PREFIX + "§cDu hast keine Berechtigung für private Runden!");
            return null;
        }

        if (playerToRound.containsKey(creator.getUniqueId())) {
            creator.sendMessage(BedWars.PREFIX + "§cDu hast bereits eine private Runde!");
            return null;
        }

        Arena templateArena = null;
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            if (arena.getName().equalsIgnoreCase(mapName)) {
                templateArena = arena;
                break;
            }
        }

        if (templateArena == null) {
            creator.sendMessage(BedWars.PREFIX + "§cMap '§e" + mapName + "§c' nicht gefunden!");
            creator.sendMessage(BedWars.PREFIX + "§7Verfügbare Maps:");
            for (Arena a : plugin.getArenaManager().getArenas()) {
                creator.sendMessage("  §8- §e" + a.getName() + " §7(" + a.getDisplayName() + ")");
            }
            return null;
        }

        World arenaWorld = null;
        for (BedWarsTeam team : templateArena.getTeams().values()) {
            if (team.getSpawn() != null) {
                arenaWorld = team.getSpawn().getWorld();
                break;
            }
        }

        if (arenaWorld == null && templateArena.getLobby() != null) {
            arenaWorld = templateArena.getLobby().getWorld();
        }

        if (arenaWorld == null) {
            creator.sendMessage(BedWars.PREFIX + "§cFehler: Arena-Welt nicht gefunden!");
            return null;
        }

        UUID roundId = UUID.randomUUID();
        String worldName = "private_" + roundId.toString().substring(0, 8);

        if (templateArena.getGameState() == GameState.INGAME) {
            creator.sendMessage(BedWars.PREFIX + "§eHinweis: Die Map ist derzeit in Benutzung. Der aktuelle Spielstand wird kopiert.");
        }

        World privateWorld = copyWorld(arenaWorld, worldName);
        if (privateWorld == null) {
            creator.sendMessage(BedWars.PREFIX + "§cFehler beim Erstellen der privaten Welt!");
            return null;
        }

        PrivateRound round = new PrivateRound(roundId, creator.getUniqueId(), templateArena, privateWorld);
        privateRounds.put(roundId, round);
        playerToRound.put(creator.getUniqueId(), round);

        Location privateLobby = translateLocation(templateArena.getLobby(), privateWorld);
        creator.teleport(privateLobby);
        creator.setGameMode(GameMode.SURVIVAL);
        creator.getInventory().clear();
        creator.setHealth(20.0);
        creator.setFoodLevel(20);

        creator.sendMessage(BedWars.PREFIX + "§aPrivate Runde erstellt!");
        creator.sendMessage(BedWars.PREFIX + "§7Map: §e" + templateArena.getDisplayName());
        creator.sendMessage(BedWars.PREFIX + "§7Nutze §e/private §7oder das GUI um Einstellungen anzupassen");
        creator.sendMessage(BedWars.PREFIX + "§7Nutze §e/private invite <spieler> §7um Spieler einzuladen");
        creator.sendMessage(BedWars.PREFIX + "§7Nutze §e/private start §7um zu starten");

        return round;
    }

    private World copyWorld(World source, String newName) {
        try {
            File sourceFolder = source.getWorldFolder();
            File targetFolder = new File(Bukkit.getWorldContainer(), newName);

            if (targetFolder.exists()) {
                deleteDirectory(targetFolder);
            }

            copyDirectory(sourceFolder, targetFolder);

            File uidFile = new File(targetFolder, "uid.dat");
            if (uidFile.exists()) {
                uidFile.delete();
            }

            WorldCreator creator = new WorldCreator(newName);
            creator.environment(source.getEnvironment());
            creator.type(source.getWorldType());

            World newWorld = creator.createWorld();
            if (newWorld != null) {
                newWorld.setAutoSave(false);
                newWorld.setKeepSpawnInMemory(false);
            }

            return newWorld;
        } catch (Exception e) {
            plugin.getLogger().severe("Error copying world: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void copyDirectory(File source, File target) throws Exception {
        if (!target.exists()) {
            target.mkdirs();
        }

        for (String child : source.list()) {
            if (child.equals("session.lock")) continue;
            if (child.equals("uid.dat")) continue;

            File sourceChild = new File(source, child);
            File targetChild = new File(target, child);

            if (sourceChild.isDirectory()) {
                copyDirectory(sourceChild, targetChild);
            } else {
                java.nio.file.Files.copy(sourceChild.toPath(), targetChild.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            for (File child : dir.listFiles()) {
                deleteDirectory(child);
            }
        }
        dir.delete();
    }

    private void clearPrivateRoundAbilities(Player player) {
        player.setAllowFlight(false);
        player.setFlying(false);

        if (plugin.getPrivateCommand() != null) {
            plugin.getPrivateCommand().removeGodMode(player.getUniqueId());
        }

        player.setGameMode(GameMode.SURVIVAL);
    }

    public void deletePrivateRound(PrivateRound round) {
        for (UUID playerId : round.getPlayers()) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                clearPrivateRoundAbilities(player);
                plugin.getLobbyManager().sendToLobby(player);
            }
            playerToRound.remove(playerId);
        }

        for (UUID playerId : round.getInvitedPlayers()) {
            playerToRound.remove(playerId);
        }

        playerToRound.remove(round.getCreator());

        World world = round.getPrivateWorld();
        if (world != null) {
            Bukkit.unloadWorld(world, false);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                deleteDirectory(world.getWorldFolder());
            }, 20L);
        }

        privateRounds.remove(round.getId());

        plugin.getLogger().info("Deleted private round " + round.getId());
    }

    public void invitePlayer(Player creator, Player target) {
        PrivateRound round = playerToRound.get(creator.getUniqueId());
        if (round == null) {
            creator.sendMessage(BedWars.PREFIX + "§cDu hast keine private Runde!");
            return;
        }

        if (!round.getCreator().equals(creator.getUniqueId())) {
            creator.sendMessage(BedWars.PREFIX + "§cNur der Ersteller kann Spieler einladen!");
            return;
        }

        if (round.getInvitedPlayers().contains(target.getUniqueId())) {
            creator.sendMessage(BedWars.PREFIX + "§cDieser Spieler ist bereits eingeladen!");
            return;
        }

        round.addInvitedPlayer(target.getUniqueId());

        creator.sendMessage(BedWars.PREFIX + "§aDu hast §e" + target.getName() + " §aeingeladen!");
        target.sendMessage(BedWars.PREFIX + "§aDu wurdest zu einer privaten BedWars Runde eingeladen!");
        target.sendMessage(BedWars.PREFIX + "§7Ersteller: §e" + creator.getName());
        target.sendMessage(BedWars.PREFIX + "§7Nutze §e/private join " + creator.getName() + " §7zum Beitreten");
    }

    public void joinPrivateRound(Player player, Player creator) {
        PrivateRound round = playerToRound.get(creator.getUniqueId());
        if (round == null) {
            player.sendMessage(BedWars.PREFIX + "§cDiese private Runde existiert nicht!");
            return;
        }

        if (!round.getInvitedPlayers().contains(player.getUniqueId()) &&
                !player.getUniqueId().equals(round.getCreator())) {
            player.sendMessage(BedWars.PREFIX + "§cDu bist nicht zu dieser Runde eingeladen!");
            return;
        }

        if (round.isStarted()) {
            player.sendMessage(BedWars.PREFIX + "§cDiese Runde hat bereits begonnen!");
            return;
        }

        playerToRound.put(player.getUniqueId(), round);
        round.addPlayer(player.getUniqueId());

        Location lobby = translateLocation(round.getTemplateArena().getLobby(), round.getPrivateWorld());
        player.teleport(lobby);

        round.broadcastMessage("§a" + player.getName() + " §7ist der Runde beigetreten!");
    }

    public void startPrivateRound(Player creator) {
        PrivateRound round = playerToRound.get(creator.getUniqueId());
        if (round == null) {
            creator.sendMessage(BedWars.PREFIX + "§cDu hast keine private Runde!");
            return;
        }

        if (!round.getCreator().equals(creator.getUniqueId())) {
            creator.sendMessage(BedWars.PREFIX + "§cNur der Ersteller kann die Runde starten!");
            return;
        }

        if (round.getPlayers().size() < 2) {
            creator.sendMessage(BedWars.PREFIX + "§cMindestens 2 Spieler werden benötigt!");
            return;
        }

        round.setStarted(true);
        startPrivateGame(round);
    }

    public void forceStartPrivateRound(PrivateRound round) {
        if (round == null || round.isStarted()) return;
        startPrivateGame(round);
    }

    private void startPrivateGame(PrivateRound round) {
        Arena template = round.getTemplateArena();

        round.broadcastMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━");
        round.broadcastMessage("§f");
        round.broadcastMessage("          §6§lPRIVATE RUNDE");
        round.broadcastMessage("§f");
        round.broadcastMessage("      §eMap: §f" + template.getDisplayName());
        round.broadcastMessage("§f");

        if (round.getSettings().isGoldEnabled()) {
            round.broadcastMessage("      §eGold: §aAktiviert");
        } else {
            round.broadcastMessage("      §eGold: §cDeaktiviert");
        }

        round.broadcastMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━");

        List<UUID> players = new ArrayList<>(round.getPlayers());
        Collections.shuffle(players);

        List<TeamColor> availableColors = new ArrayList<>();
        for (String teamName : template.getTeams().keySet()) {
            try {
                availableColors.add(TeamColor.valueOf(teamName));
            } catch (IllegalArgumentException ignored) {}
        }

        int teamSize = round.getSettings().getPlayersPerTeam();
        int teamIndex = 0;

        for (int i = 0; i < players.size(); i++) {
            UUID playerId = players.get(i);
            Player player = plugin.getServer().getPlayer(playerId);
            if (player == null) continue;

            TeamColor color = availableColors.get(teamIndex % availableColors.size());
            BedWarsTeam templateTeam = template.getTeams().get(color.name());

            if (templateTeam != null && templateTeam.getSpawn() != null) {
                Location spawn = translateLocation(templateTeam.getSpawn(), round.getPrivateWorld());
                player.teleport(spawn);
                player.setGameMode(GameMode.SURVIVAL);
                player.getInventory().clear();
                player.setHealth(round.getSettings().getMaxHealth());
                player.setMaxHealth(round.getSettings().getMaxHealth());
                player.setFoodLevel(20);
            }

            if ((i + 1) % teamSize == 0) {
                teamIndex++;
            }
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!round.isStarted()) return;

            round.broadcastMessage("§aDie Runde hat begonnen!");
        }, 60L);
    }

    private Location translateLocation(Location original, World newWorld) {
        return new Location(newWorld,
                original.getX(), original.getY(), original.getZ(),
                original.getYaw(), original.getPitch());
    }

    public void leavePrivateRound(Player player) {
        PrivateRound round = playerToRound.get(player.getUniqueId());
        if (round == null) {
            player.sendMessage(BedWars.PREFIX + "§cDu bist in keiner privaten Runde!");
            return;
        }

        round.removePlayer(player.getUniqueId());
        playerToRound.remove(player.getUniqueId());

        clearPrivateRoundAbilities(player);

        plugin.getLobbyManager().sendToLobby(player);

        if (player.getUniqueId().equals(round.getCreator())) {
            round.broadcastMessage("§cDer Ersteller hat die Runde verlassen. Runde wird beendet!");
            deletePrivateRound(round);
        } else {
            round.broadcastMessage("§c" + player.getName() + " §7hat die Runde verlassen.");
        }
    }

    public PrivateRound getPlayerRound(UUID playerId) {
        return playerToRound.get(playerId);
    }

    public void removePlayerFromRound(UUID playerId) {
        playerToRound.remove(playerId);
    }

    public Map<UUID, PrivateRound> getPrivateRounds() {
        return privateRounds;
    }

    public static class PrivateRound {
        private final UUID id;
        private final UUID creator;
        private final Arena templateArena;
        private final World privateWorld;
        private final Set<UUID> invitedPlayers = new HashSet<>();
        private final Set<UUID> players = new HashSet<>();
        private final PrivateSettings settings = new PrivateSettings();
        private boolean started = false;
        private long createdAt;

        public PrivateRound(UUID id, UUID creator, Arena templateArena, World privateWorld) {
            this.id = id;
            this.creator = creator;
            this.templateArena = templateArena;
            this.privateWorld = privateWorld;
            this.createdAt = System.currentTimeMillis();
            this.players.add(creator);
        }

        public void broadcastMessage(String message) {
            for (UUID playerId : players) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.sendMessage(BedWars.PREFIX + message);
                }
            }
        }

        public void addInvitedPlayer(UUID playerId) { invitedPlayers.add(playerId); }
        public void addPlayer(UUID playerId) { players.add(playerId); }
        public void removePlayer(UUID playerId) { players.remove(playerId); }

        public UUID getId() { return id; }
        public UUID getCreator() { return creator; }
        public Arena getTemplateArena() { return templateArena; }
        public World getPrivateWorld() { return privateWorld; }
        public Set<UUID> getInvitedPlayers() { return invitedPlayers; }
        public Set<UUID> getPlayers() { return players; }
        public PrivateSettings getSettings() { return settings; }
        public boolean isStarted() { return started; }
        public void setStarted(boolean started) { this.started = started; }
        public long getCreatedAt() { return createdAt; }
    }

    public static class PrivateSettings {
        private boolean goldEnabled = true;
        private boolean ironEnabled = true;
        private double maxHealth = 20.0;
        private int bronzeSpawnRate = 1;
        private int ironSpawnRate = 5;
        private int goldSpawnRate = 10;
        private int playersPerTeam = 1;

        public boolean isGoldEnabled() { return goldEnabled; }
        public void setGoldEnabled(boolean goldEnabled) { this.goldEnabled = goldEnabled; }

        public boolean isIronEnabled() { return ironEnabled; }
        public void setIronEnabled(boolean ironEnabled) { this.ironEnabled = ironEnabled; }

        public double getMaxHealth() { return maxHealth; }
        public void setMaxHealth(double maxHealth) { this.maxHealth = maxHealth; }

        public int getBronzeSpawnRate() { return bronzeSpawnRate; }
        public void setBronzeSpawnRate(int bronzeSpawnRate) { this.bronzeSpawnRate = bronzeSpawnRate; }

        public int getIronSpawnRate() { return ironSpawnRate; }
        public void setIronSpawnRate(int ironSpawnRate) { this.ironSpawnRate = ironSpawnRate; }

        public int getGoldSpawnRate() { return goldSpawnRate; }
        public void setGoldSpawnRate(int goldSpawnRate) { this.goldSpawnRate = goldSpawnRate; }

        public int getPlayersPerTeam() { return playersPerTeam; }
        public void setPlayersPerTeam(int playersPerTeam) { this.playersPerTeam = playersPerTeam; }
    }
}
