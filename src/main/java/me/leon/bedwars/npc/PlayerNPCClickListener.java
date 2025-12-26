package me.leon.bedwars.npc;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
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
            Channel channel = (Channel) channelField.get(networkManager);

            ChannelPipeline pipeline = channel.pipeline();

            String handlerName = "bedwars_npc_handler";

            if (pipeline.get(handlerName) == null) {
                pipeline.addBefore("packet_handler", handlerName, new ChannelDuplexHandler() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        if (msg.getClass().getSimpleName().equals("PacketPlayInUseEntity")) {
                            int entityId = getEntityIdFromPacket(msg);

                            Bukkit.getScheduler().runTask(plugin, () -> {
                                if (plugin.getPlayerNPCManager().getNPCByEntityId(entityId) != null) {
                                    plugin.getPlayerNPCManager().handleNPCClick(player, entityId);
                                } else {
                                    handleShopNPCClick(player, entityId);
                                }
                            });
                        }
                        super.channelRead(ctx, msg);
                    }
                });
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Could not inject packet listener for " + player.getName() + ": " + e.getMessage());
        }
    }

    private int getEntityIdFromPacket(Object packet) {
        try {
            Field field = packet.getClass().getDeclaredField("a");
            field.setAccessible(true);
            return field.getInt(packet);
        } catch (Exception e) {
            return -1;
        }
    }

    private void removePacketListener(Player player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
            Object networkManager = playerConnection.getClass().getField("networkManager").get(playerConnection);

            Field channelField = networkManager.getClass().getDeclaredField("channel");
            channelField.setAccessible(true);
            Channel channel = (Channel) channelField.get(networkManager);

            ChannelPipeline pipeline = channel.pipeline();

            String handlerName = "bedwars_npc_handler";
            if (pipeline.get(handlerName) != null) {
                pipeline.remove(handlerName);
            }

        } catch (Exception e) {
        }
    }

    private void handleShopNPCClick(Player player, int entityId) {
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getGameState() != GameState.INGAME) return;

        for (BedWarsTeam team : arena.getTeams().values()) {
            ShopNPC shopNPC = team.getShopNPC();
            if (shopNPC != null && shopNPC.getEntityId() == entityId) {
                plugin.getShopManager().openShop(player, arena);
                return;
            }
        }
    }
}
