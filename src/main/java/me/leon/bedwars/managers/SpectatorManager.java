package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.utils.ItemBuilder;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpectatorManager {

    private final BedWars plugin;

    public SpectatorManager(BedWars plugin) {
        this.plugin = plugin;
    }

    public void setSpectator(Player player, Game game) {
        player.setGameMode(GameMode.SPECTATOR);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.getInventory().clear();

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));

        ItemStack teleporter = new ItemBuilder(Material.COMPASS)
                .setDisplayName("§aTeleporter §7(Rechtsklick)")
                .setLore("§7Teleportiere zu Spielern")
                .build();
        player.getInventory().setItem(0, teleporter);

        ItemStack leave = new ItemBuilder(Material.INK_SACK, 1, (short) 1)
                .setDisplayName("§cZurück zur Lobby")
                .build();
        player.getInventory().setItem(8, leave);

        player.teleport(game.getMapData().getSpectator());
    }
}