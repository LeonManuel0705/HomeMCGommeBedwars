package me.leon.bedwars.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.leon.bedwars.BedWars;
import me.leon.bedwars.managers.JoinMeIntegration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class PluginMessageListener implements PluginMessageListener {

    private final BedWars plugin;
    private final JoinMeIntegration joinMeIntegration;

    public PluginMessageListener(BedWars plugin) {
        this.plugin = plugin;
        this.joinMeIntegration = new JoinMeIntegration(plugin);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();

        if (subchannel.equals("JoinMe")) {
            handleJoinMeMessage(in);
        } else if (subchannel.equals("JoinGame")) {
            handleJoinGameMessage(in, player);
        }
    }

    private void handleJoinMeMessage(ByteArrayDataInput in) {
        try {
            String senderUUID = in.readUTF();
            String senderName = in.readUTF();
            String serverName = in.readUTF();
            String gameMode = in.readUTF();
            String gameName = in.readUTF();
            int currentPlayers = in.readInt();
            int maxPlayers = in.readInt();

        } catch (Exception e) {
            plugin.getLogger().severe("Fehler beim Verarbeiten der JoinMe Message!");
            e.printStackTrace();
        }
    }

    private void handleJoinGameMessage(ByteArrayDataInput in, Player player) {
        try {
            String playerName = in.readUTF();
            String targetServer = in.readUTF();
            String gameName = in.readUTF();

            joinMeIntegration.handleJoinMeJoin(player, gameName);

        } catch (Exception e) {
            plugin.getLogger().severe("Fehler beim Verarbeiten der JoinGame Message!");
            e.printStackTrace();
        }
    }
}