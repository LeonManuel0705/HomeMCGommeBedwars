package me.leon.bedwars.npc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.leon.bedwars.BedWars;
import org.bukkit.Bukkit;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Utility class to fetch player skin data from Mojang API.
 */
public class SkinFetcher {

    private static final Map<String, String[]> skinCache = new ConcurrentHashMap<>();

    /**
     * Fetches skin data for a player name asynchronously.
     * @param playerName The player name to fetch skin for
     * @param callback Callback with [skinValue, skinSignature] or null if not found
     */
    public static void fetchSkinAsync(String playerName, Consumer<String[]> callback) {
        // Check cache first
        if (skinCache.containsKey(playerName.toLowerCase())) {
            callback.accept(skinCache.get(playerName.toLowerCase()));
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(BedWars.getInstance(), () -> {
            String[] skin = fetchSkin(playerName);
            if (skin != null) {
                skinCache.put(playerName.toLowerCase(), skin);
            }

            // Run callback on main thread
            Bukkit.getScheduler().runTask(BedWars.getInstance(), () -> callback.accept(skin));
        });
    }

    /**
     * Fetches skin data synchronously (blocking).
     * @param playerName The player name
     * @return [skinValue, skinSignature] or null if not found
     */
    public static String[] fetchSkin(String playerName) {
        try {
            // Step 1: Get UUID from name
            String uuid = getUUID(playerName);
            if (uuid == null) return null;

            // Step 2: Get profile with skin data
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != 200) return null;

            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            reader.close();

            JsonObject properties = json.getAsJsonArray("properties").get(0).getAsJsonObject();
            String value = properties.get("value").getAsString();
            String signature = properties.get("signature").getAsString();

            return new String[]{value, signature};

        } catch (Exception e) {
            BedWars.getInstance().getLogger().warning("Failed to fetch skin for " + playerName + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets the UUID for a player name from Mojang API.
     */
    private static String getUUID(String playerName) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != 200) return null;

            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            reader.close();

            return json.get("id").getAsString();

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Clears the skin cache.
     */
    public static void clearCache() {
        skinCache.clear();
    }

    /**
     * Pre-defined skins for common NPC types (Steve-like skins).
     */
    public static class DefaultSkins {
        // Default Steve skin
        public static final String STEVE_VALUE = "eyJ0aW1lc3RhbXAiOjE1ODc0OTU1MzU2NjMsInByb2ZpbGVJZCI6Ijg2NjdiYTcxYjg1YTQwMDRhZjU0NDU3YTk3MzRlZWQ3IiwicHJvZmlsZU5hbWUiOiJTdGV2ZSIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWE0YWY3MTg0NTVkNGFhYjUyOGU3YTYxZjg2ZmEyNWU2YTM2OWQxNzY4ZGNiMTNmN2RmMzE5YTcxM2ViODEwYiJ9fX0=";
        public static final String STEVE_SIGNATURE = "";

        // You can add more default skins here
    }
}
