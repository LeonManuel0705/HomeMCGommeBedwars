package me.leon.bedwars.utils;

import me.leon.bedwars.BedWars;

public class ConfigManager {

    private final BedWars plugin;

    public ConfigManager(BedWars plugin) {
        this.plugin = plugin;
        createDefaultConfig();
    }

    private void createDefaultConfig() {
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }
}