package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import me.leon.core.managers.RankManager;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildCommand implements CommandExecutor {

    private final BedWars plugin;

    public BuildCommand(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl verwenden!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bedwars.lobby.build")) {
            RankManager.RankData builder = plugin.getRankManager().getRankData("homemc.builder");
            String builderPrefix = builder.prefix;
            String builderColor = builder.color;
            String builderName = builder.displayName;
            String builderFormatted = builderColor + builderName;
            player.sendMessage("§8§m=======§r §c§lZugriff verweigert §8§m=======§r");
            player.sendMessage("§3Um diesen Befehl auszuführen, benötigst du:");
            player.sendMessage(" §8• §2Rang: " + builderFormatted);
            player.sendMessage("§8§m====================================");
            return true;
        }

        if (!plugin.getBedWarsLobby().isInLobby(player.getUniqueId())) {
            player.sendMessage(BedWars.PREFIX + "§cDu kannst den Bau-Modus nur in der Lobby verwenden!");
            return true;
        }

        boolean isBuildMode = plugin.getBedWarsLobby().toggleBuildMode(player);

        if (isBuildMode) {
            player.sendMessage(BedWars.PREFIX + "§aBau-Modus aktiviert!");
            player.getInventory().clear();
            player.setGameMode(GameMode.CREATIVE);
        } else {
            player.sendMessage(BedWars.PREFIX + "§cBau-Modus deaktiviert!");
            player.getInventory().clear();
            player.setGameMode(GameMode.SURVIVAL);
            plugin.getBedWarsLobby().giveItems(player);
        }

        return true;
    }
}