package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import org.bukkit.entity.Player;

import java.util.*;

// ==================== GAME MANAGER ====================
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

        if (arena.getGameState() == GameState.INGAME || arena.getGameState() == GameState.ENDING) {
            player.sendMessage("§cDas Spiel läuft bereits!");
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
