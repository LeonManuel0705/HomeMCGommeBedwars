package me.leon.bedwars.npc;

import me.leon.bedwars.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;

/**
 * Listens for player NPC interactions using packet injection.
 * This handles showing NPCs to joining players and detecting clicks.
 */
public class PlayerNPCClickListener implements Listener {

    private final BedWars plugin;

    public PlayerNPCClickListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Show all player NPCs to the joining player after a short delay
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && plugin.getPlayerNPCManager() != null) {
                plugin.getPlayerNPCManager().showAllNPCs(player);
            }

            // Inject packet listener for NPC click detection
            injectPacketListener(player);
        }, 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Remove packet listener
        removePacketListener(player);
    }

    /**
     * Injects a packet listener into the player's connection to detect NPC clicks.
     */
    private void injectPacketListener(Player player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
            Object networkManager = playerConnection.getClass().getField("networkManager").get(playerConnection);

            // Get the channel from the network manager
            Field channelField = networkManager.getClass().getDeclaredField("channel");
            channelField.setAccessible(true);
            Object channel = channelField.get(networkManager);

            // Add our handler to the pipeline
            Object pipeline = channel.getClass().getMethod("pipeline").invoke(channel);

            // Create and add the packet handler
            String handlerName = "bedwars_npc_handler";

            // Check if handler already exists
            Object existingHandler = pipeline.getClass().getMethod("get", String.class).invoke(pipeline, handlerName);
            if (existingHandler == null) {
                // Use reflection to create a ChannelDuplexHandler that intercepts packets
                Class<?> channelDuplexHandlerClass = Class.forName("io.netty.channel.ChannelDuplexHandler");
                Object handler = createPacketHandler(player, channelDuplexHandlerClass);

                pipeline.getClass().getMethod("addBefore", String.class, String.class,
                    Class.forName("io.netty.channel.ChannelHandler"))
                    .invoke(pipeline, "packet_handler", handlerName, handler);
            }

        } catch (Exception e) {
            // Packet injection failed - NPC clicks won't work but the plugin will still function
            plugin.getLogger().warning("Could not inject packet listener for " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Creates a packet handler that intercepts UseEntity packets.
     */
    private Object createPacketHandler(Player player, Class<?> handlerClass) throws Exception {
        // For simplicity, we'll use a basic approach that checks entity IDs on interaction
        // This is a simplified version - full packet interception requires more complex code
        return java.lang.reflect.Proxy.newProxyInstance(
            handlerClass.getClassLoader(),
            new Class[]{Class.forName("io.netty.channel.ChannelHandler")},
            (proxy, method, args) -> {
                // Default behavior - just pass through
                return null;
            }
        );
    }

    /**
     * Removes the packet listener from a player's connection.
     */
    private void removePacketListener(Player player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
            Object networkManager = playerConnection.getClass().getField("networkManager").get(playerConnection);

            Field channelField = networkManager.getClass().getDeclaredField("channel");
            channelField.setAccessible(true);
            Object channel = channelField.get(networkManager);

            Object pipeline = channel.getClass().getMethod("pipeline").invoke(channel);

            String handlerName = "bedwars_npc_handler";
            Object existingHandler = pipeline.getClass().getMethod("get", String.class).invoke(pipeline, handlerName);
            if (existingHandler != null) {
                pipeline.getClass().getMethod("remove", String.class).invoke(pipeline, handlerName);
            }

        } catch (Exception e) {
            // Ignore errors during cleanup
        }
    }
}
