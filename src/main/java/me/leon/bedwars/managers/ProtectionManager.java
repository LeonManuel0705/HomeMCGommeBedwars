package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class ProtectionManager {

    private final BedWars plugin;

    public ProtectionManager(BedWars plugin) {
        this.plugin = plugin;
    }

    public boolean isProtected(Block block) {
        return true;
    }
}