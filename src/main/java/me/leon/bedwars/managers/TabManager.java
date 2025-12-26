package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.GameState;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class TabManager {

    private final BedWars plugin;
    private static final String VERSION;

    static {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        VERSION = packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    public TabManager(BedWars plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    private void startUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateTab(player);
            }
        }, 20L, 40L);
    }

    public void updateTab(Player player) {
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());

        if (arena != null) {
            updateArenaTab(player, arena);
        } else {
            updateLobbyTab(player);
        }
    }

    private void updateLobbyTab(Player player) {
        String header = "\n§6§lBEDWARS\n§7Lobby\n";
        String footer = "\n§eSpieler online: §f" + Bukkit.getOnlinePlayers().size() + "\n§7play.homemc.net\n";

        sendTabHeaderFooter(player, header, footer);
    }

    private void updateArenaTab(Player player, Arena arena) {
        StringBuilder header = new StringBuilder();
        header.append("\n§6§lBEDWARS\n");
        header.append("§7Map: §e").append(arena.getDisplayName()).append("\n");

        StringBuilder footer = new StringBuilder();

        if (arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING) {
            header.append("§7Spieler: §e").append(arena.getPlayers().size())
                    .append("§7/§e").append(arena.getMaxPlayers()).append("\n");

            if (arena.getGameState() == GameState.STARTING) {
                footer.append("\n§aSpiel startet in §e").append(arena.getCountdown()).append("§a Sekunden\n");
            } else {
                footer.append("\n§7Warte auf Spieler...\n");
            }
        } else if (arena.getGameState() == GameState.INGAME) {
            footer.append("\n");
            for (BedWarsTeam team : arena.getTeams().values()) {
                String bedStatus = team.isBedAlive() ? "§a✔" : "§c✘";
                int alivePlayers = countAlivePlayers(team);
                footer.append(team.getColor().getColorCode())
                        .append(team.getColor().getDisplayName())
                        .append(" ").append(bedStatus)
                        .append(" §7(").append(alivePlayers).append(")\n");
            }
        }

        footer.append("§7play.homemc.net\n");

        sendTabHeaderFooter(player, header.toString(), footer.toString());
    }

    private int countAlivePlayers(BedWarsTeam team) {
        int count = 0;
        for (UUID uuid : team.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline() && !BedWars.getInstance().getGameManager()
                    .getArenaByPlayer(uuid).getDeadPlayers().contains(uuid)) {
                count++;
            }
        }
        return count;
    }

    private void sendTabHeaderFooter(Player player, String header, String footer) {
        try {
            Object headerComponent = createChatComponent(header);
            Object footerComponent = createChatComponent(footer);

            Class<?> packetClass = getNMSClass("PacketPlayOutPlayerListHeaderFooter");
            Object packet = packetClass.newInstance();

            Field headerField = packetClass.getDeclaredField("a");
            headerField.setAccessible(true);
            headerField.set(packet, headerComponent);

            Field footerField = packetClass.getDeclaredField("b");
            footerField.setAccessible(true);
            footerField.set(packet, footerComponent);

            sendPacket(player, packet);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send tab header/footer: " + e.getMessage());
        }
    }

    private Object createChatComponent(String text) throws Exception {
        Class<?> chatSerializerClass;
        try {
            chatSerializerClass = getNMSClass("IChatBaseComponent$ChatSerializer");
        } catch (ClassNotFoundException e) {
            chatSerializerClass = getNMSClass("ChatSerializer");
        }

        Method aMethod = chatSerializerClass.getMethod("a", String.class);
        return aMethod.invoke(null, "{\"text\":\"" + text.replace("\"", "\\\"") + "\"}");
    }

    private void sendPacket(Player player, Object packet) throws Exception {
        Object handle = player.getClass().getMethod("getHandle").invoke(player);
        Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
        playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
    }

    private static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + VERSION + "." + name);
    }
}
