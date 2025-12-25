package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BuildManager {

    private final BedWars plugin;
    private final Set<UUID> buildMode;

    public BuildManager(BedWars plugin) {
        this.plugin = plugin;
        this.buildMode = new HashSet<>();
    }

    public void toggleBuildMode(Player player) {
        UUID uuid = player.getUniqueId();

        if (buildMode.contains(uuid)) {
            buildMode.remove(uuid);
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            plugin.getLobbyManager().sendToLobby(player);

            player.sendMessage(BedWars.PREFIX + "§cBau-Modus deaktiviert!");
        } else {
            buildMode.add(uuid);
            player.setGameMode(GameMode.CREATIVE);
            player.getInventory().clear();

            player.sendMessage(BedWars.PREFIX + "§aBau-Modus aktiviert!");
        }
    }

    public boolean isInBuildMode(UUID uuid) {
        return buildMode.contains(uuid);
    }

    public void removeBuildMode(UUID uuid) {
        buildMode.remove(uuid);
    }
}