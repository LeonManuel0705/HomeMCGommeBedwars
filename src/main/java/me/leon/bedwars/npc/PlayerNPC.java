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
    private float currentYaw;
    private float currentPitch;

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
        this.currentYaw = location.getYaw();
        this.currentPitch = location.getPitch();
    }

    public void spawn() {
        try {
            Object server = getMinecraftServer();
            Object world = getWorldServer(location.getWorld());

            String tabName = name.length() > 16 ? name.substring(0, 16) : name;
            Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            Constructor<?> gpConstructor = gameProfileClass.getConstructor(UUID.class, String.class);
            gameProfile = gpConstructor.newInstance(id, tabName);

            if (skinValue != null && !skinValue.isEmpty()) {
                Method getProperties = gameProfileClass.getMethod("getProperties");
                Object propertyMap = getProperties.invoke(gameProfile);

                Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
                Constructor<?> propConstructor = propertyClass.getConstructor(String.class, String.class, String.class);
                Object property = propConstructor.newInstance("textures", skinValue, skinSignature);

                Method putMethod = propertyMap.getClass().getMethod("put", Object.class, Object.class);
                putMethod.invoke(propertyMap, "textures", property);
            }

            Class<?> worldClass = getNMSClass("World");
            Class<?> worldServerClass = getNMSClass("WorldServer");
            Class<?> playerInteractManagerClass = getNMSClass("PlayerInteractManager");

            Constructor<?> pimConstructor = null;
            try {
                pimConstructor = playerInteractManagerClass.getConstructor(worldClass);
            } catch (NoSuchMethodException e) {
                pimConstructor = playerInteractManagerClass.getConstructor(worldServerClass);
            }
            Object playerInteractManager = pimConstructor.newInstance(world);

            Class<?> minecraftServerClass = getNMSClass("MinecraftServer");
            Class<?> entityPlayerClass = getNMSClass("EntityPlayer");
            Constructor<?> epConstructor = entityPlayerClass.getConstructor(
                    minecraftServerClass, worldServerClass, gameProfileClass, playerInteractManagerClass);
            entityPlayer = epConstructor.newInstance(server, world, gameProfile, playerInteractManager);

            Method setLocation = entityPlayerClass.getMethod("setLocation",
                    double.class, double.class, double.class, float.class, float.class);
            setLocation.invoke(entityPlayer, location.getX(), location.getY(), location.getZ(),
                    location.getYaw(), location.getPitch());

            Method getIdMethod = getNMSClass("Entity").getMethod("getId");
            entityId = (int) getIdMethod.invoke(entityPlayer);

            for (Player player : Bukkit.getOnlinePlayers()) {
                showTo(player);
            }

        } catch (Exception e) {
            BedWars.getInstance().getLogger().warning("Failed to spawn NPC: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showTo(Player player) {
        if (entityPlayer == null) return;

        try {
            Object connection = getPlayerConnection(player);

            sendPacket(connection, createPlayerInfoPacket("ADD_PLAYER"));
            sendPacket(connection, createNamedEntitySpawnPacket());
            sendPacket(connection, createHeadRotationPacket(currentYaw));

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

    public void hideFrom(Player player) {
        if (entityPlayer == null) return;

        try {
            Object connection = getPlayerConnection(player);
            sendPacket(connection, createDestroyPacket());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            hideFrom(player);
        }
        entityPlayer = null;
        entityId = -1;
    }

    public void lookAt(Location target) {
        if (entityPlayer == null) return;

        try {
            double npcEyeY = location.getY() + 1.62;
            double dx = target.getX() - location.getX();
            double dy = target.getY() - npcEyeY;
            double dz = target.getZ() - location.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);

            float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
            float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

            this.currentYaw = yaw;
            this.currentPitch = pitch;

            Field yawField = getNMSClass("Entity").getDeclaredField("yaw");
            Field pitchField = getNMSClass("Entity").getDeclaredField("pitch");
            yawField.setAccessible(true);
            pitchField.setAccessible(true);
            yawField.set(entityPlayer, yaw);
            pitchField.set(entityPlayer, pitch);

            for (Player player : Bukkit.getOnlinePlayers()) {
                Object connection = getPlayerConnection(player);
                sendPacket(connection, createHeadRotationPacket(yaw));
                sendPacket(connection, createLookPacket(yaw, pitch));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    private Object createHeadRotationPacket(float yaw) throws Exception {
        Class<?> packetClass = getNMSClass("PacketPlayOutEntityHeadRotation");
        Constructor<?> constructor = packetClass.getConstructor(getNMSClass("Entity"), byte.class);
        return constructor.newInstance(entityPlayer, (byte) ((yaw * 256.0F) / 360.0F));
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

    public UUID getId() { return id; }
    public String getName() { return name; }
    public Location getLocation() { return location; }
    public String getSkinValue() { return skinValue; }
    public String getSkinSignature() { return skinSignature; }
    public String getNpcType() { return npcType; }
    public int getEntityId() { return entityId; }
}
