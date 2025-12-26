package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import org.bukkit.entity.Player;

import java.util.*;

public class GameManager {

    private final BedWars plugin;
    private final Map<String, Arena> arenas;
    private final Map<UUID, Arena> playerArenas;

    public GameManager(BedWars plugin) {
        this.plugin = plugin;
        this.arenas = new HashMap<>();
        this.playerArenas = new HashMap<>();
    }

    public void joinArena(Player player, String arenaName) {
        Arena arena = arenas.get(arenaName);

        if (arena == null) {
            player.sendMessage("§cArena nicht gefunden!");
            return;
        }

        if (arena.getGameState() == GameState.ENDING) {
            player.sendMessage("§cDas Spiel endet gerade!");
            return;
        }

        // Allow spectators to join running games
        if (arena.getGameState() == GameState.INGAME) {
            joinAsSpectator(player, arena);
            return;
        }

        if (arena.getPlayers().size() >= arena.getMaxPlayers()) {
            player.sendMessage("§cDie Arena ist voll!");
            return;
        }

        if (playerArenas.containsKey(player.getUniqueId())) {
            Arena oldArena = playerArenas.get(player.getUniqueId());
            oldArena.removePlayer(player, true);
        }

        player.setAllowFlight(false);
        arena.addPlayer(player);
        playerArenas.put(player.getUniqueId(), arena);
    }

    public void leaveArena(Player player) {
        Arena arena = playerArenas.get(player.getUniqueId());

        if (arena == null) {
            player.sendMessage("§cDu bist in keiner Arena!");
            return;
        }

        arena.removePlayer(player, true);
        playerArenas.remove(player.getUniqueId());

        plugin.getLobbyManager().sendToLobby(player);
        if (player.hasPermission("homemc.fly")) {
            player.setAllowFlight(true);
        }
    }

    public Arena getArenaByPlayer(UUID uuid) {
        return playerArenas.get(uuid);
    }

    public Arena getArena(String name) {
        return arenas.get(name);
    }

    public void addArena(Arena arena) {
        arenas.put(arena.getName(), arena);
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    public void joinAsSpectator(Player player, Arena arena) {
        if (playerArenas.containsKey(player.getUniqueId())) {
            Arena oldArena = playerArenas.get(player.getUniqueId());
            oldArena.removePlayer(player, true);
        }

        player.setAllowFlight(true);
        player.setFlying(true);
        player.setGameMode(org.bukkit.GameMode.ADVENTURE);

        arena.getPlayers().add(player.getUniqueId());
        arena.getSpectators().add(player.getUniqueId());
        playerArenas.put(player.getUniqueId(), arena);

        if (arena.getSpectator() != null) {
            player.teleport(arena.getSpectator());
        } else if (arena.getLobby() != null) {
            player.teleport(arena.getLobby());
        }

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        // Hide spectator from all players in arena
        for (UUID uuid : arena.getPlayers()) {
            Player arenaPlayer = plugin.getServer().getPlayer(uuid);
            if (arenaPlayer != null && !arena.getSpectators().contains(uuid)) {
                arenaPlayer.hidePlayer(player);
            }
        }

        player.sendMessage(BedWars.PREFIX + "§aDu schaust nun als Zuschauer zu!");
        plugin.getScoreboardManager().updateScoreboard(arena);
    }

    public void shutdown() {
        for (Arena arena : arenas.values()) {
            for (UUID uuid : new HashSet<>(arena.getPlayers())) {
                Player player = plugin.getServer().getPlayer(uuid);
                if (player != null) {
                    arena.removePlayer(player, false);
                }
            }
            arena.reset();
        }
    }
}
