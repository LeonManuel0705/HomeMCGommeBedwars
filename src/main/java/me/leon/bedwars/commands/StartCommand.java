package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.game.GameState;
import me.leon.core.managers.RankManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommand implements CommandExecutor {

    private final BedWars plugin;

    public StartCommand(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl verwenden!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bedwars.forcestart")) {
            RankManager.RankData yt = plugin.getRankManager().getRankData("homemc.yt");
            String ytPrefix = yt.prefix;
            String ytColor = yt.color;
            String ytName = yt.displayName;
            String ytFormatted = ytColor + ytName;
            player.sendMessage("§8§m=======§r §c§lZugriff verweigert §8§m=======§r");
            player.sendMessage("§3Um diesen Befehl zu nutzen, benötigst du:");
            player.sendMessage(" §8• §2Rang: " + ytFormatted);
            player.sendMessage("§8§m====================================");
            return true;
        }

        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game == null) {
            player.sendMessage(BedWars.PREFIX + "§cDu bist in keinem Spiel!");
            return true;
        }

        // ForceStop
        if (args.length > 0 && args[0].equalsIgnoreCase("stop")) {
            if (game.getState() == GameState.LOBBY) {
                player.sendMessage(BedWars.PREFIX + "§cDas Spiel hat noch nicht begonnen!");
                return true;
            }

            if (game.getState() == GameState.ENDING) {
                player.sendMessage(BedWars.PREFIX + "§cDas Spiel wird bereits beendet!");
                return true;
            }

            if (!player.hasPermission("homemc.forcestop")) {
                RankManager.RankData mod = plugin.getRankManager().getRankData("homemc.mod");
                String modPrefix = mod.prefix;
                String modColor = mod.color;
                String modName = mod.displayName;
                String modFormatted = modColor + modName;
                player.sendMessage("§8§m=======§r §c§lZugriff verweigert §8§m=======§r");
                player.sendMessage("§3Um das Spiel zu stoppen, benötigst du:");
                player.sendMessage(" §8• §2Rang: " + modFormatted);
                player.sendMessage("§8§m====================================");
                return true;
            }

            game.broadcast("§c§lSPIEL GESTOPPT!");
            game.broadcast("§7Das Spiel wurde von einem Teammitglied beendet.");
            game.forceEnd();
            game.removePlayer(player);
            player.sendMessage(BedWars.PREFIX + "§aSpiel wurde gestoppt!");
            return true;
        }

        // ForceStart
        if (game.getState() != GameState.LOBBY) {
            player.sendMessage(BedWars.PREFIX + "§cDas Spiel läuft bereits!");
            return true;
        }

        if (game.getPlayers().size() < 1) {
            player.sendMessage(BedWars.PREFIX + "§cEs werden mindestens 2 Spieler benötigt!");
            return true;
        }

        player.sendMessage(BedWars.PREFIX + "§bStarte das Spiel...");
        game.broadcast("§6§lSPIEL GESTARTET!");
        game.broadcast("§aDas Spiel wurde frühzeitig gestartet.");
        game.forceStart();

        return true;
    }
}