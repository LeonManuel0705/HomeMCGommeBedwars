package me.leon.bedwars.managers;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;

import java.util.List;

public class TeamData {

    private final String name;
    private final String shortName;
    private final ChatColor chatColor;
    private final Location spawn;
    private final Location bed;
    private final Location shopNPC;
    private final Location upgradeNPC;
    private final List<Location> generators;

    public TeamData(String name, String shortName, ChatColor chatColor,
                    Location spawn, Location bed, Location shopNPC,
                    Location upgradeNPC, List<Location> generators) {
        this.name = name;
        this.shortName = shortName;
        this.chatColor = chatColor;
        this.spawn = spawn;
        this.bed = bed;
        this.shopNPC = shopNPC;
        this.upgradeNPC = upgradeNPC;
        this.generators = generators;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public String getColor() {
        return chatColor.toString();
    }

    public Color getArmorColor() {
        switch (chatColor) {
            case RED: return Color.RED;
            case BLUE: return Color.BLUE;
            case GREEN: return Color.GREEN;
            case YELLOW: return Color.YELLOW;
            case AQUA: return Color.AQUA;
            case LIGHT_PURPLE: return Color.PURPLE;
            case GRAY: return Color.GRAY;
            case WHITE: return Color.WHITE;
            default: return Color.WHITE;
        }
    }

    public Location getSpawn() {
        return spawn;
    }

    public Location getBed() {
        return bed;
    }

    public Location getShopNPC() {
        return shopNPC;
    }

    public Location getUpgradeNPC() {
        return upgradeNPC;
    }

    public List<Location> getGenerators() {
        return generators;
    }
}