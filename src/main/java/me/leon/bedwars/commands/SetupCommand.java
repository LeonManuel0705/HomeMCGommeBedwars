package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetupCommand implements CommandExecutor {

    private final BedWars plugin;

    public SetupCommand(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl verwenden!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bedwars.admin")) {
            player.sendMessage(BedWars.PREFIX + "§cDazu hast du keine Rechte!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(BedWars.PREFIX + "§7Setup Befehle:");
            player.sendMessage("§e/setup setlobby §7- Setze Lobby Spawn");
            return true;
        }

        if (args[0].equalsIgnoreCase("setlobby")) {
            plugin.getLobbyManager().setLobbySpawn(player.getLocation());
            player.sendMessage(BedWars.PREFIX + "§aLobby Spawn gesetzt!");
        }

        return true;
    }
}