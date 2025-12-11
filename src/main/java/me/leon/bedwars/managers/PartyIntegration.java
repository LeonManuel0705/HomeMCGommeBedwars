package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.core.managers.PartyManager;
import me.leon.core.utils.CoreAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class PartyIntegration {

    private final BedWars plugin;

    public PartyIntegration(BedWars plugin) {
        this.plugin = plugin;
    }

    public void joinGameWithParty(Player leader, MapData mapData) {
        if (!CoreAPI.isPartyManagerAvailable()) {
            plugin.getGameManager().joinGame(leader,
                    plugin.getGameManager().getAvailableGame(mapData));
            return;
        }

        if (!CoreAPI.isInParty(leader.getUniqueId())) {
            plugin.getGameManager().joinGame(leader,
                    plugin.getGameManager().getAvailableGame(mapData));
            return;
        }

        PartyManager.Party party = CoreAPI.getParty(leader.getUniqueId());

        if (!party.isLeader(leader.getUniqueId())) {
            leader.sendMessage(BedWars.PREFIX + "§cNur der Party-Leader kann Spiele joinen!");
            return;
        }

        Set<UUID> members = party.getMembers();

        if (members.size() > mapData.getMaxPlayers()) {
            leader.sendMessage(BedWars.PREFIX + "§cDeine Party ist zu groß für dieses Spiel!");
            leader.sendMessage(BedWars.PREFIX + "§cMaximale Spieler: §e" + mapData.getMaxPlayers());
            return;
        }

        Game game = findAvailableGameForParty(mapData, members.size());

        if (game == null) {
            leader.sendMessage(BedWars.PREFIX + "§cKein passendes Spiel gefunden!");
            return;
        }

        for (UUID memberUUID : members) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null && member.isOnline()) {

                if (plugin.getGameManager().isInGame(member)) {
                    plugin.getGameManager().leaveGame(member);
                }

                plugin.getGameManager().joinGame(member, game);
            }
        }

        broadcastToParty(party, "§aAlle Party-Mitglieder sind dem Spiel beigetreten!");
    }

    private Game findAvailableGameForParty(MapData mapData, int partySize) {
        for (Game game : plugin.getGameManager().getAllGames()) {
            if (game.getMapData().equals(mapData) &&
                    game.getState() == me.leon.bedwars.game.GameState.LOBBY) {

                int freeSlots = mapData.getMaxPlayers() - game.getPlayers().size();

                if (freeSlots >= partySize) {
                    return game;
                }
            }
        }

        return plugin.getGameManager().createGame(mapData);
    }

    private void broadcastToParty(PartyManager.Party party, String message) {
        for (UUID memberUUID : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null && member.isOnline()) {
                member.sendMessage(BedWars.PREFIX + message);
            }
        }
    }

    public void handlePartyMemberJoin(Player leader, Player newMember, Game game) {
        if (!CoreAPI.isPartyManagerAvailable()) return;

        if (!CoreAPI.isInParty(leader.getUniqueId())) return;

        PartyManager.Party party = CoreAPI.getParty(leader.getUniqueId());
        if (party == null || !party.isLeader(leader.getUniqueId())) return;

        if (!party.isMember(newMember.getUniqueId())) return;

        if (game.getPlayers().size() >= game.getMapData().getMaxPlayers()) {
            newMember.sendMessage(BedWars.PREFIX + "§cDas Spiel ist voll!");
            return;
        }

        if (game.getState() != me.leon.bedwars.game.GameState.LOBBY) {
            newMember.sendMessage(BedWars.PREFIX + "§cDas Spiel läuft bereits!");
            return;
        }

        plugin.getGameManager().joinGame(newMember, game);

        broadcastToParty(party, "§e" + newMember.getName() + " §aist dem Spiel beigetreten!");
    }
}

