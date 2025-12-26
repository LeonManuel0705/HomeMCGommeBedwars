package me.leon.bedwars.npc;

import me.leon.bedwars.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Represents a player-like NPC with skin support.
 * Uses reflection to work with NMS classes for 1.8.8 compatibility.
 */
public class PlayerNPC {

    private final UUID id;
    private final String name;
    private final Location location;
    private final String skinValue;
    private final String skinSignature;
    private final String npcType;

    private Object entityPlayer;
    private Object gameProfile;
    private int entityId = -1;

    // NMS version string
    private static final String VERSION;

    static {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        VERSION = packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    public PlayerNPC(UUID id, String name, Location location, String skinValue, String skinSignature, String npcType) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.skinValue = skinValue;
        this.skinSignature = skinSignature;
        this.npcType = npcType;
    }

    /**
     * Creates and spawns the NPC entity.
     */
    public void spawn() {
        try {
            Object server = getMinecraftServer();
            Object world = getWorldServer(location.getWorld());

            // Create game profile
            String tabName = name.length() > 16 ? name.substring(0, 16) : name;
            Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            Constructor<?> gpConstructor = gameProfileClass.getConstructor(UUID.class, String.class);
            gameProfile = gpConstructor.newInstance(id, tabName);

            // Apply skin if provided
            if (skinValue != null && !skinValue.isEmpty()) {
                Method getProperties = gameProfileClass.getMethod("getProperties");
                Object propertyMap = getProperties.invoke(gameProfile);

                Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
                Constructor<?> propConstructor = propertyClass.getConstructor(String.class, String.class, String.class);
                Object property = propConstructor.newInstance("textures", skinValue, skinSignature);

                Method putMethod = propertyMap.getClass().getMethod("put", Object.class, Object.class);
                putMethod.invoke(propertyMap, "textures", property);
            }

            // Create PlayerInteractManager
            Class<?> worldServerClass = getNMSClass("WorldServer");
            Class<?> playerInteractManagerClass = getNMSClass("PlayerInteractManager");
            Constructor<?> pimConstructor = playerInteractManagerClass.getConstructor(worldServerClass);
            Object playerInteractManager = pimConstructor.newInstance(world);

            // Create EntityPlayer
            Class<?> minecraftServerClass = getNMSClass("MinecraftServer");
            Class<?> entityPlayerClass = getNMSClass("EntityPlayer");
            Constructor<?> epConstructor = entityPlayerClass.getConstructor(
                    minecraftServerClass, worldServerClass, gameProfileClass, playerInteractManagerClass);
            entityPlayer = epConstructor.newInstance(server, world, gameProfile, playerInteractManager);

            // Set location
            Method setLocation = entityPlayerClass.getMethod("setLocation",
                    double.class, double.class, double.class, float.class, float.class);
            setLocation.invoke(entityPlayer, location.getX(), location.getY(), location.getZ(),
                    location.getYaw(), location.getPitch());

            // Get entity ID
            Method getIdMethod = getNMSClass("Entity").getMethod("getId");
            entityId = (int) getIdMethod.invoke(entityPlayer);

            // Spawn for all online players
            for (Player player : Bukkit.getOnlinePlayers()) {
                showTo(player);
            }

        } catch (Exception e) {
            BedWars.getInstance().getLogger().warning("Failed to spawn NPC: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Shows the NPC to a specific player.
     */
    public void showTo(Player player) {
        if (entityPlayer == null) return;

        try {
            Object connection = getPlayerConnection(player);

            // Add to tab list
            sendPacket(connection, createPlayerInfoPacket("ADD_PLAYER"));

            // Spawn entity
            sendPacket(connection, createNamedEntitySpawnPacket());

            // Head rotation
            sendPacket(connection, createHeadRotationPacket());

            // Remove from tab list after delay
            Bukkit.getScheduler().runTaskLater(BedWars.getInstance(), () -> {
                try {
                    sendPacket(connection, createPlayerInfoPacket("REMOVE_PLAYER"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 20L);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Hides the NPC from a specific player.
     */
    public void hideFrom(Player player) {
        if (entityPlayer == null) return;

        try {
            Object connection = getPlayerConnection(player);
            sendPacket(connection, createDestroyPacket());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Destroys the NPC for all players.
     */
    public void destroy() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            hideFrom(player);
        }
        entityPlayer = null;
        entityId = -1;
    }

    /**
     * Makes the NPC look at a target location.
     */
    public void lookAt(Location target) {
        if (entityPlayer == null) return;

        try {
            double dx = target.getX() - location.getX();
            double dy = target.getY() - location.getY();
            double dz = target.getZ() - location.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);

            float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
            float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

            // Update entity yaw/pitch
            Field yawField = getNMSClass("Entity").getField("yaw");
            Field pitchField = getNMSClass("Entity").getField("pitch");
            yawField.set(entityPlayer, yaw);
            pitchField.set(entityPlayer, pitch);

            for (Player player : Bukkit.getOnlinePlayers()) {
                Object connection = getPlayerConnection(player);
                sendPacket(connection, createHeadRotationPacket());
                sendPacket(connection, createLookPacket(yaw, pitch));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper methods for NMS reflection

    private Object getMinecraftServer() throws Exception {
        Class<?> craftServerClass = getCraftBukkitClass("CraftServer");
        Method getServer = craftServerClass.getMethod("getServer");
        return getServer.invoke(Bukkit.getServer());
    }

    private Object getWorldServer(org.bukkit.World world) throws Exception {
        Class<?> craftWorldClass = getCraftBukkitClass("CraftWorld");
        Method getHandle = craftWorldClass.getMethod("getHandle");
        return getHandle.invoke(world);
    }

    private Object getPlayerConnection(Player player) throws Exception {
        Class<?> craftPlayerClass = getCraftBukkitClass("entity.CraftPlayer");
        Method getHandle = craftPlayerClass.getMethod("getHandle");
        Object entityPlayer = getHandle.invoke(player);

        Field connectionField = entityPlayer.getClass().getField("playerConnection");
        return connectionField.get(entityPlayer);
    }

    private void sendPacket(Object connection, Object packet) throws Exception {
        Method sendPacket = connection.getClass().getMethod("sendPacket", getNMSClass("Packet"));
        sendPacket.invoke(connection, packet);
    }

    private Object createPlayerInfoPacket(String action) throws Exception {
        Class<?> packetClass = getNMSClass("PacketPlayOutPlayerInfo");
        Class<?> enumAction = getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");

        Object actionEnum = null;
        for (Object constant : enumAction.getEnumConstants()) {
            if (constant.toString().equals(action)) {
                actionEnum = constant;
                break;
            }
        }

        Class<?> entityPlayerClass = getNMSClass("EntityPlayer");
        Object array = Array.newInstance(entityPlayerClass, 1);
        Array.set(array, 0, entityPlayer);

        Constructor<?> constructor = packetClass.getConstructor(enumAction, array.getClass());
        return constructor.newInstance(actionEnum, array);
    }

    private Object createNamedEntitySpawnPacket() throws Exception {
        Class<?> packetClass = getNMSClass("PacketPlayOutNamedEntitySpawn");
        Constructor<?> constructor = packetClass.getConstructor(getNMSClass("EntityHuman"));
        return constructor.newInstance(entityPlayer);
    }

    private Object createHeadRotationPacket() throws Exception {
        Class<?> packetClass = getNMSClass("PacketPlayOutEntityHeadRotation");
        Constructor<?> constructor = packetClass.getConstructor(getNMSClass("Entity"), byte.class);
        return constructor.newInstance(entityPlayer, (byte) ((location.getYaw() * 256.0F) / 360.0F));
    }

    private Object createLookPacket(float yaw, float pitch) throws Exception {
        Class<?> packetClass = getNMSClass("PacketPlayOutEntity$PacketPlayOutEntityLook");
        Constructor<?> constructor = packetClass.getConstructor(int.class, byte.class, byte.class, boolean.class);
        return constructor.newInstance(entityId,
                (byte) ((yaw * 256.0F) / 360.0F),
                (byte) ((pitch * 256.0F) / 360.0F),
                true);
    }

    private Object createDestroyPacket() throws Exception {
        Class<?> packetClass = getNMSClass("PacketPlayOutEntityDestroy");
        Constructor<?> constructor = packetClass.getConstructor(int[].class);
        return constructor.newInstance(new int[]{entityId});
    }

    private static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + VERSION + "." + name);
    }

    private static Class<?> getCraftBukkitClass(String name) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + VERSION + "." + name);
    }

    // Getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public Location getLocation() { return location; }
    public String getSkinValue() { return skinValue; }
    public String getSkinSignature() { return skinSignature; }
    public String getNpcType() { return npcType; }
    public int getEntityId() { return entityId; }
}
