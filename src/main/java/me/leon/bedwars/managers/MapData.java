package me.leon.bedwars.managers;

import org.bukkit.Location;

import java.util.List;

public class MapData {

    private final String name;
    private final String worldName;
    private final int minPlayers;
    private final int maxPlayers;

    private final Location lobby;
    private final Location spectator;

    private final List<TeamData> teams;
    private final List<Location> bronzeGenerators;
    private final List<Location> goldGenerators;

    public MapData(String name, String worldName, int minPlayers, int maxPlayers,
                   Location lobby, Location spectator, List<TeamData> teams,
                   List<Location> bronzeGenerators, List<Location> goldGenerators) {
        this.name = name;
        this.worldName = worldName;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.lobby = lobby;
        this.spectator = spectator;
        this.teams = teams;
        this.bronzeGenerators = bronzeGenerators;
        this.goldGenerators = goldGenerators;
    }

    public String getName() {
        return name;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public Location getLobby() {
        return lobby;
    }

    public Location getSpectator() {
        return spectator;
    }

    public List<TeamData> getTeams() {
        return teams;
    }

    public List<Location> getBronzeGenerators() {
        return bronzeGenerators;
    }

    public List<Location> getGoldGenerators() {
        return goldGenerators;
    }
}