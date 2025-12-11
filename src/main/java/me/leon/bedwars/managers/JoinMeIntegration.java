package me.leon.bedwars.managers;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.core.utils.CoreAPI;
import org.bukkit.entity.Player;

public class JoinMeIntegration {

    private final BedWars plugin;

    public JoinMeIntegration(BedWars plugin) {
        this.plugin = plugin;
    }

    public void sendJoinMe(Player player, Game game) {
        if (!CoreAPI.isJoinMeAvailable()) {
            player.sendMessage(BedWars.PREFIX + "§cJoinMe ist nicht verfügbar!");
            return;
        }

        if (game == null) {
            player.sendMessage(BedWars.PREFIX + "§cDu bist in keinem Spiel!");
            return;
        }

        if (game.getState() != me.leon.bedwars.game.GameState.LOBBY) {
            player.sendMessage(BedWars.PREFIX + "§cDas Spiel läuft bereits!");
            return;
        }

        String serverName = plugin.getConfig().getString("server-name", "bedwars-1");

        int currentPlayers = game.getPlayers().size();
        int maxPlayers = game.getMapData().getMaxPlayers();

        if (currentPlayers >= maxPlayers) {
            player.sendMessage(BedWars.PREFIX + "§cDas Spiel ist voll!");
            return;
        }

        try {
            CoreAPI.getCore().getJoinMeManager().sendJoinMe(
                    player,
                    serverName,
                    "BedWars",
                    game.getMapData().getName(),
                    currentPlayers,
                    maxPlayers
            );

            player.sendMessage(BedWars.PREFIX + "§aJoinMe wurde gesendet!");

        } catch (Exception e) {
            player.sendMessage(BedWars.PREFIX + "§cFehler beim Senden der JoinMe Anfrage!");
            plugin.getLogger().warning("JoinMe Fehler: " + e.getMessage());
        }
    }

    public void handleJoinMeJoin(Player player, String mapName) {
        MapData mapData = plugin.getMapManager().getMap(mapName);

        if (mapData == null) {
            player.sendMessage(BedWars.PREFIX + "§cDiese Map existiert nicht!");
            return;
        }

        for (Game game : plugin.getGameManager().getAllGames()) {
            if (game.getMapData().equals(mapData) &&
                    game.getState() == me.leon.bedwars.game.GameState.LOBBY) {

                if (game.getPlayers().size() < game.getMapData().getMaxPlayers()) {
                    plugin.getGameManager().joinGame(player, game);
                    return;
                }
            }
        }

        player.sendMessage(BedWars.PREFIX + "§cKein passendes Spiel gefunden!");
    }
}