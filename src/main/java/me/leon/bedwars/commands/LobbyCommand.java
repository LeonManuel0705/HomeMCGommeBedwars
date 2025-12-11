package me.leon.bedwars.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.leon.bedwars.BedWars;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyCommand implements CommandExecutor {

    private final BedWars plugin;

    public LobbyCommand(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl verwenden!");
            return true;
        }

        Player player = (Player) sender;

        if (plugin.getGameManager().isInGame(player)) {
            plugin.getGameManager().leaveGame(player);
            player.sendMessage(BedWars.PREFIX + "§aDu hast das Spiel verlassen!");
        }

        sendToLobbyServer(player);

        return true;
    }

    private void sendToLobbyServer(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF("lobby");

        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}