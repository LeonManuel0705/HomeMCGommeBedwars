package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.game.GameState;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {

    private final BedWars plugin;
    private final Map<String, Game> games;
    private final Map<UUID, Game> playerGames;

    public GameManager(BedWars plugin) {
        this.plugin = plugin;
        this.games = new ConcurrentHashMap<>();
        this.playerGames = new ConcurrentHashMap<>();
    }

    public Game createGame(MapData mapData) {
        Game game = new Game(plugin, mapData);
        games.put(game.getGameId(), game);

        plugin.getLogger().info("Spiel erstellt: " + game.getGameId() + " (Map: " + mapData.getName() + ")");

        return game;
    }

    public void joinGame(Player player, Game game) {
        if (playerGames.containsKey(player.getUniqueId())) {
            player.sendMessage(BedWars.PREFIX + "§cDu bist bereits in einem Spiel!");
            return;
        }

        if (plugin.getBedWarsLobby().isInBuildMode(player.getUniqueId())) {
            plugin.getBedWarsLobby().toggleBuildMode(player);
            player.sendMessage(BedWars.PREFIX + "§7Bau-Modus wurde automatisch deaktiviert.");
        }

        game.addPlayer(player);
        playerGames.put(player.getUniqueId(), game);
    }

    public void leaveGame(Player player) {
        Game game = playerGames.remove(player.getUniqueId());
        if (game != null) {
            game.removePlayer(player);
            player.teleport(plugin.getBedWarsLobby().getLobbySpawn());
            player.getInventory().clear();
            plugin.getBedWarsLobby().giveItems(player);
        }
    }

    public Game getAvailableGame(MapData mapData) {
        for (Game game : games.values()) {
            if (game.getMapData().equals(mapData) &&
                    game.getState() == GameState.LOBBY &&
                    game.getPlayers().size() < mapData.getMaxPlayers()) {
                return game;
            }
        }

        return createGame(mapData);
    }

    public Game getPlayerGame(Player player) {
        return playerGames.get(player.getUniqueId());
    }

    public boolean isInGame(Player player) {
        return playerGames.containsKey(player.getUniqueId());
    }

    public void removeGame(Game game) {
        games.remove(game.getGameId());
        plugin.getLogger().info("Spiel entfernt: " + game.getGameId());
    }

    public void shutdownAll() {
        for (Game game : new ArrayList<>(games.values())) {
            game.shutdown();
        }
        games.clear();
        playerGames.clear();
    }

    public Collection<Game> getAllGames() {
        return games.values();
    }
}