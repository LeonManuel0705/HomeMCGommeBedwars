package me.leon.bedwars.npc;

import me.leon.bedwars.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;

public class PlayerNPCClickListener implements Listener {

    private final BedWars plugin;

    public PlayerNPCClickListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && plugin.getPlayerNPCManager() != null) {
                plugin.getPlayerNPCManager().showAllNPCs(player);
            }

            injectPacketListener(player);
        }, 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        removePacketListener(player);
    }

    private void injectPacketListener(Player player) {
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
            if (existingHandler == null) {
                Class<?> channelDuplexHandlerClass = Class.forName("io.netty.channel.ChannelDuplexHandler");
                Object handler = createPacketHandler(player, channelDuplexHandlerClass);

                pipeline.getClass().getMethod("addBefore", String.class, String.class,
                    Class.forName("io.netty.channel.ChannelHandler"))
                    .invoke(pipeline, "packet_handler", handlerName, handler);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Could not inject packet listener for " + player.getName() + ": " + e.getMessage());
        }
    }

    private Object createPacketHandler(Player player, Class<?> handlerClass) throws Exception {
        return java.lang.reflect.Proxy.newProxyInstance(
            handlerClass.getClassLoader(),
            new Class[]{Class.forName("io.netty.channel.ChannelHandler")},
            (proxy, method, args) -> {
                return null;
            }
        );
    }

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
        }
    }
}
