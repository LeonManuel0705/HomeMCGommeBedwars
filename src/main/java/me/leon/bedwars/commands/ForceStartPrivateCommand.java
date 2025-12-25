package me.leon.bedwars.commands;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.managers.PrivateRoundManager.PrivateRound;
import me.leon.core.managers.RankManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceStartPrivateCommand implements CommandExecutor {

    private final BedWars plugin;

    public ForceStartPrivateCommand(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl nutzen!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bedwars.forcestartprivate")) {
            RankManager.RankData mod = plugin.getCore().getRankManager().getRankData("homemc.mod");
            String modColor = mod != null ? mod.color : "§c";
            String modName = mod != null ? mod.displayName : "Mod";
            String modFormatted = modColor + modName;
            player.sendMessage("§8§m=======§r §c§lZugriff verweigert §8§m=======§r");
            player.sendMessage("§3Um diesen Befehl zu nutzen, benötigst du:");
            player.sendMessage(" §8• §2Rang: " + modFormatted + " §7oder höher");
            player.sendMessage("§8§m====================================");
            return true;
        }

        PrivateRound round = plugin.getPrivateRoundManager().getPlayerRound(player.getUniqueId());

        if (round == null) {
            player.sendMessage(BedWars.PREFIX + "§cDu bist in keiner privaten Runde!");
            return true;
        }

        if (round.isStarted()) {
            player.sendMessage(BedWars.PREFIX + "§cDie Runde wurde bereits gestartet!");
            return true;
        }

        if (round.getPlayers().size() < 1) {
            player.sendMessage(BedWars.PREFIX + "§cMindestens 1 Spieler wird benötigt!");
            return true;
        }

        round.broadcastMessage("§c§lFORCESTART! §7Die Runde wird von einem Teammitglied gestartet...");

        round.setStarted(true);
        plugin.getPrivateRoundManager().forceStartPrivateRound(round);

        player.sendMessage(BedWars.PREFIX + "§aPrivate Runde wurde gestartet!");

        return true;
    }
}
