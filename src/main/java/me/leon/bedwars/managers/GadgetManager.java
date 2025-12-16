package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.utils.ItemBuilder;
import me.leon.core.utils.CoreAPI;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GadgetManager {

    private final BedWars plugin;
    private final Map<UUID, Set<String>> ownedGadgets;
    private final Map<UUID, String> selectedGadgets;
    private final Map<String, Gadget> gadgets;
    private final Map<UUID, Long> cooldowns;

    public GadgetManager(BedWars plugin) {
        this.plugin = plugin;
        this.ownedGadgets = new ConcurrentHashMap<>();
        this.selectedGadgets = new ConcurrentHashMap<>();
        this.gadgets = new LinkedHashMap<>();
        this.cooldowns = new ConcurrentHashMap<>();

        registerGadgets();
    }

    private void registerGadgets() {
        gadgets.put("FIREWORK_PARTY", new Gadget(
                "FIREWORK_PARTY", "§cFeuerwerk Party",
                "Zünde ein buntes Feuerwerk!", Material.FIREWORK,
                500, GadgetType.WIN_EFFECT, 10
        ));

        gadgets.put("LIGHTNING_STRIKE", new Gadget(
                "LIGHTNING_STRIKE", "§eBlitzschlag",
                "Rufe einen Blitz herab!", Material.GOLD_SWORD,
                750, GadgetType.WIN_EFFECT, 15
        ));

        gadgets.put("ENDER_DRAGON", new Gadget(
                "ENDER_DRAGON", "§5Enderdrachen Ruf",
                "Spiele den Enderdrachen Sound!", Material.DRAGON_EGG,
                1000, GadgetType.WIN_EFFECT, 20
        ));

        gadgets.put("PARTICLE_HEARTS", new Gadget(
                "PARTICLE_HEARTS", "§cHerz Partikel",
                "Herzen um dich herum!", Material.RED_ROSE,
                600, GadgetType.PARTICLE, 5
        ));

        gadgets.put("PARTICLE_FLAME", new Gadget(
                "PARTICLE_FLAME", "§6Flammen Partikel",
                "Brennende Flammen!", Material.BLAZE_POWDER,
                800, GadgetType.PARTICLE, 5
        ));

        gadgets.put("PARTICLE_MAGIC", new Gadget(
                "PARTICLE_MAGIC", "§dMagische Partikel",
                "Magische Verzauberung!", Material.ENCHANTMENT_TABLE,
                1200, GadgetType.PARTICLE, 5
        ));

        gadgets.put("KILL_MSG_BOOM", new Gadget(
                "KILL_MSG_BOOM", "§4BOOM! Kill-Message",
                "Explosive Kill-Nachricht!", Material.TNT,
                400, GadgetType.KILL_MESSAGE, 0
        ));

        gadgets.put("KILL_MSG_RIP", new Gadget(
                "KILL_MSG_RIP", "§7R.I.P Kill-Message",
                "Respektvolle Verabschiedung!", Material.SKULL_ITEM,
                400, GadgetType.KILL_MESSAGE, 0
        ));

        gadgets.put("KILL_MSG_OWNED", new Gadget(
                "KILL_MSG_OWNED", "§cOWNED Kill-Message",
                "Zeig ihnen wer der Boss ist!", Material.DIAMOND_SWORD,
                400, GadgetType.KILL_MESSAGE, 0
        ));

        gadgets.put("BED_DESTROY_EXPLOSION", new Gadget(
                "BED_DESTROY_EXPLOSION", "§4Explosions-Effekt",
                "Bett mit Explosion zerstören!", Material.TNT,
                900, GadgetType.BED_DESTROY, 0
        ));

        gadgets.put("BED_DESTROY_WITHER", new Gadget(
                "BED_DESTROY_WITHER", "§8Wither-Effekt",
                "Wither Sound beim Zerstören!", Material.SKULL_ITEM, (short) 1,
                1200, GadgetType.BED_DESTROY, 0
        ));

        gadgets.put("DANCE_MOVES", new Gadget(
                "DANCE_MOVES", "§aTanz-Moves",
                "Tanze nach dem Sieg!", Material.JUKEBOX,
                1500, GadgetType.WIN_EFFECT, 30
        ));
    }

    public void loadPlayer(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement ps = plugin.getMySQL().getConnection().prepareStatement(
                        "SELECT gadget_id FROM bw_gadgets WHERE uuid = ?"
                );
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();

                Set<String> owned = ConcurrentHashMap.newKeySet();
                while (rs.next()) {
                    owned.add(rs.getString("gadget_id"));
                }

                ownedGadgets.put(uuid, owned);

                rs.close();
                ps.close();

                ps = plugin.getMySQL().getConnection().prepareStatement(
                        "SELECT selected_gadget FROM bw_settings WHERE uuid = ?"
                );
                ps.setString(1, uuid.toString());
                rs = ps.executeQuery();

                if (rs.next()) {
                    String selected = rs.getString("selected_gadget");
                    if (selected != null && !selected.isEmpty()) {
                        selectedGadgets.put(uuid, selected);
                    }
                }

                rs.close();
                ps.close();

            } catch (Exception e) {
                plugin.getLogger().severe("Fehler beim Laden der Gadgets für " + uuid);
                e.printStackTrace();
            }
        });
    }

    public void purchaseGadget(Player player, String gadgetId) {
        UUID uuid = player.getUniqueId();

        if (ownsGadget(uuid, gadgetId)) {
            player.sendMessage(BedWars.PREFIX + "§cDu besitzt dieses Gadget bereits!");
            return;
        }

        Gadget gadget = gadgets.get(gadgetId);
        if (gadget == null) return;

        if (!CoreAPI.isCoreAvailable()) {
            player.sendMessage(BedWars.PREFIX + "§cCoin-System nicht verfügbar!");
            return;
        }

        try {
            int coins = CoreAPI.getCore().getCoinManager().getCoins(uuid);

            if (coins < gadget.getPrice()) {
                player.sendMessage(BedWars.PREFIX + "§cDu hast nicht genug Coins! Benötigt: §6" + gadget.getPrice());
                return;
            }

            CoreAPI.getCore().getCoinManager().removeCoins(uuid, gadget.getPrice());

            ownedGadgets.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet()).add(gadgetId);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    PreparedStatement ps = plugin.getMySQL().getConnection().prepareStatement(
                            "INSERT INTO bw_gadgets (uuid, gadget_id, purchased_at) VALUES (?, ?, ?)"
                    );
                    ps.setString(1, uuid.toString());
                    ps.setString(2, gadgetId);
                    ps.setLong(3, System.currentTimeMillis());
                    ps.executeUpdate();
                    ps.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            player.sendMessage(BedWars.PREFIX + "§aDu hast §e" + gadget.getName() + " §agekauft!");
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);

        } catch (Exception e) {
            player.sendMessage(BedWars.PREFIX + "§cFehler beim Kaufen!");
        }
    }

    public void selectGadget(Player player, String gadgetId) {
        UUID uuid = player.getUniqueId();

        if (!ownsGadget(uuid, gadgetId)) {
            player.sendMessage(BedWars.PREFIX + "§cDu besitzt dieses Gadget nicht!");
            return;
        }

        selectedGadgets.put(uuid, gadgetId);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement ps = plugin.getMySQL().getConnection().prepareStatement(
                        "INSERT INTO bw_settings (uuid, selected_gadget) VALUES (?, ?) " +
                                "ON DUPLICATE KEY UPDATE selected_gadget = ?"
                );
                ps.setString(1, uuid.toString());
                ps.setString(2, gadgetId);
                ps.setString(3, gadgetId);
                ps.executeUpdate();
                ps.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Gadget gadget = gadgets.get(gadgetId);
        player.sendMessage(BedWars.PREFIX + "§aDu hast §e" + gadget.getName() + " §aausgewählt!");
    }

    public void activateGadget(Player player, GadgetType type) {
        UUID uuid = player.getUniqueId();
        String gadgetId = selectedGadgets.get(uuid);

        if (gadgetId == null) return;

        Gadget gadget = gadgets.get(gadgetId);
        if (gadget == null || gadget.getType() != type) return;

        if (gadget.getCooldown() > 0) {
            long lastUse = cooldowns.getOrDefault(uuid, 0L);
            long remaining = (lastUse + (gadget.getCooldown() * 1000L)) - System.currentTimeMillis();

            if (remaining > 0) {
                player.sendMessage(BedWars.PREFIX + "§cCooldown: §e" + (remaining / 1000) + "s");
                return;
            }

            cooldowns.put(uuid, System.currentTimeMillis());
        }

        executeGadget(player, gadget);
    }

    private void executeGadget(Player player, Gadget gadget) {
        Location loc = player.getLocation();

        switch (gadget.getId()) {
            case "FIREWORK_PARTY":
                for (int i = 0; i < 5; i++) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> spawnFirework(loc), i * 10L);
                }
                break;

            case "LIGHTNING_STRIKE":
                loc.getWorld().strikeLightningEffect(loc);
                loc.getWorld().playSound(loc, Sound.AMBIENCE_THUNDER, 2.0f, 1.0f);
                break;

            case "ENDER_DRAGON":
                loc.getWorld().playSound(loc, Sound.ENDERDRAGON_GROWL, 2.0f, 1.0f);
                break;

            case "PARTICLE_HEARTS":
                spawnParticleEffect(player, Effect.HEART, 30);
                break;

            case "PARTICLE_FLAME":
                spawnParticleEffect(player, Effect.FLAME, 30);
                break;

            case "PARTICLE_MAGIC":
                spawnParticleEffect(player, Effect.MAGIC_CRIT, 30);
                break;

            case "DANCE_MOVES":
                performDance(player);
                break;

            case "BED_DESTROY_EXPLOSION":
                loc.getWorld().playEffect(loc, Effect.EXPLOSION_HUGE, 0);
                loc.getWorld().playSound(loc, Sound.EXPLODE, 2.0f, 1.0f);
                break;

            case "BED_DESTROY_WITHER":
                loc.getWorld().playSound(loc, Sound.WITHER_SPAWN, 1.5f, 1.0f);
                break;
        }
    }

    private void spawnFirework(Location loc) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();

        FireworkEffect effect = FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL_LARGE)
                .withColor(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW)
                .withFade(Color.WHITE)
                .trail(true)
                .flicker(true)
                .build();

        meta.addEffect(effect);
        meta.setPower(1);
        fw.setFireworkMeta(meta);
    }

    private void spawnParticleEffect(Player player, Effect effect, int duration) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= duration || !player.isOnline()) {
                    cancel();
                    return;
                }

                Location loc = player.getLocation().add(0, 1, 0);
                for (int i = 0; i < 3; i++) {
                    double angle = Math.toRadians(ticks * 20 + (i * 120));
                    double x = Math.cos(angle) * 0.5;
                    double z = Math.sin(angle) * 0.5;
                    loc.getWorld().playEffect(loc.add(x, 0, z), effect, 0);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void performDance(Player player) {
        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                if (step >= 10 || !player.isOnline()) {
                    cancel();
                    return;
                }

                Location loc = player.getLocation();
                loc.setYaw(loc.getYaw() + 36);
                player.teleport(loc);

                if (step % 2 == 0) {
                    player.getWorld().playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f + (step * 0.1f));
                }

                step++;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    public String getKillMessage(UUID killer, String victimName) {
        String gadgetId = selectedGadgets.get(killer);
        if (gadgetId == null) return null;

        Gadget gadget = gadgets.get(gadgetId);
        if (gadget == null || gadget.getType() != GadgetType.KILL_MESSAGE) return null;

        switch (gadgetId) {
            case "KILL_MSG_BOOM":
                return "§4§l💥 BOOM! §c" + victimName + " §7wurde weggesprengt!";
            case "KILL_MSG_RIP":
                return "§7§l✟ R.I.P §c" + victimName + " §8(F in den Chat)";
            case "KILL_MSG_OWNED":
                return "§c§l⚔ OWNED! §c" + victimName + " §7wurde zerstört!";
            default:
                return null;
        }
    }

    public void openGadgetShop(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Gadget Shop");

        int slot = 10;
        for (Gadget gadget : gadgets.values()) {
            boolean owned = ownsGadget(player.getUniqueId(), gadget.getId());
            boolean selected = gadget.getId().equals(selectedGadgets.get(player.getUniqueId()));

            ItemStack item = new ItemBuilder(
                    gadget.getIcon(), 1, gadget.getData()
            )
                    .setDisplayName((owned ? "§a" : "§7") + gadget.getName())
                    .setLore(
                            "§7" + gadget.getDescription(),
                            "",
                            "§7Typ: §e" + gadget.getType().getDisplayName(),
                            gadget.getCooldown() > 0 ? "§7Cooldown: §e" + gadget.getCooldown() + "s" : "",
                            "",
                            owned ? (selected ? "§a§lAUSGEWÄHLT" : "§eKlicke zum Auswählen") : "§7Preis: §6" + gadget.getPrice() + " Coins",
                            owned ? "" : "§eKlicke zum Kaufen"
                    )
                    .build();

            inv.setItem(slot, item);

            slot++;
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
            if (slot == 35) slot = 37;
        }

        player.openInventory(inv);
    }

    public boolean ownsGadget(UUID uuid, String gadgetId) {
        Set<String> owned = ownedGadgets.get(uuid);
        return owned != null && owned.contains(gadgetId);
    }

    public Gadget getGadget(String id) {
        return gadgets.get(id);
    }

    public void unloadPlayer(UUID uuid) {
        ownedGadgets.remove(uuid);
        selectedGadgets.remove(uuid);
        cooldowns.remove(uuid);
    }

    public static class Gadget {
        private final String id;
        private final String name;
        private final String description;
        private final Material icon;
        private final short data;
        private final int price;
        private final GadgetType type;
        private final int cooldown;

        public Gadget(String id, String name, String description, Material icon,
                      int price, GadgetType type, int cooldown) {
            this(id, name, description, icon, (short) 0, price, type, cooldown);
        }

        public Gadget(String id, String name, String description, Material icon,
                      short data, int price, GadgetType type, int cooldown) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.icon = icon;
            this.data = data;
            this.price = price;
            this.type = type;
            this.cooldown = cooldown;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Material getIcon() { return icon; }
        public short getData() { return data; }
        public int getPrice() { return price; }
        public GadgetType getType() { return type; }
        public int getCooldown() { return cooldown; }
    }

    public enum GadgetType {
        WIN_EFFECT("Sieg-Effekt"),
        PARTICLE("Partikel"),
        KILL_MESSAGE("Kill-Nachricht"),
        BED_DESTROY("Bett-Zerstörung");

        private final String displayName;

        GadgetType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}