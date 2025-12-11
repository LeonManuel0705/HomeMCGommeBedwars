package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GeneratorManager {

    private final BedWars plugin;
    private final Map<String, List<Generator>> gameGenerators;

    public GeneratorManager(BedWars plugin) {
        this.plugin = plugin;
        this.gameGenerators = new ConcurrentHashMap<>();
    }

    public void startGenerators(Game game) {
        String gameId = game.getGameId();
        List<Generator> generators = new ArrayList<>();

        for (TeamData team : game.getMapData().getTeams()) {
            for (Location loc : team.getGenerators()) {
                Generator gen = new Generator(loc, GeneratorType.IRON, 1);
                gen.start(game);
                generators.add(gen);
            }
        }

        for (Location loc : game.getMapData().getBronzeGenerators()) {
            Generator gen = new Generator(loc, GeneratorType.BRONZE, 2);
            gen.start(game);
            generators.add(gen);
        }

        for (Location loc : game.getMapData().getGoldGenerators()) {
            Generator gen = new Generator(loc, GeneratorType.GOLD, 3);
            gen.start(game);
            generators.add(gen);
        }

        gameGenerators.put(gameId, generators);
    }

    public void stopGenerators(String gameId) {
        List<Generator> generators = gameGenerators.remove(gameId);
        if (generators != null) {
            for (Generator gen : generators) {
                gen.stop();
            }
        }
    }

    public void upgradeGenerators(String gameId, GeneratorType type, int level) {
        List<Generator> generators = gameGenerators.get(gameId);
        if (generators != null) {
            for (Generator gen : generators) {
                if (gen.getType() == type) {
                    gen.setLevel(level);
                }
            }
        }
    }

    public class Generator {
        private final Location location;
        private final GeneratorType type;
        private int level;
        private BukkitRunnable task;
        private ArmorStand hologram;
        private int nextSpawnIn;

        public Generator(Location location, GeneratorType type, int level) {
            this.location = location.clone().add(0.5, 0, 0.5);
            this.type = type;
            this.level = level;
            this.nextSpawnIn = type.getBaseDelay(level);
        }

        public void start(Game game) {
            createHologram();

            task = new BukkitRunnable() {
                @Override
                public void run() {
                    nextSpawnIn--;

                    if (nextSpawnIn <= 0) {
                        spawnItem();
                        nextSpawnIn = type.getBaseDelay(level);
                    }

                    updateHologram();
                }
            };
            task.runTaskTimer(plugin, 0L, 20L);
        }

        public void stop() {
            if (task != null) {
                task.cancel();
            }
            if (hologram != null) {
                hologram.remove();
            }
        }

        private void createHologram() {
            hologram = location.getWorld().spawn(location.clone().add(0, 1.5, 0), ArmorStand.class);
            hologram.setVisible(false);
            hologram.setGravity(false);
            hologram.setCustomNameVisible(true);
            hologram.setMarker(true);
            updateHologram();
        }

        private void updateHologram() {
            if (hologram != null) {
                String name = type.getColor() + type.getDisplayName() + " " +
                        getRomanNumeral(level) + " §8┃ §f" + nextSpawnIn + "s";
                hologram.setCustomName(name);
            }
        }

        private void spawnItem() {
            ItemStack itemStack = type.getItemStack();
            Item item = location.getWorld().dropItem(location, itemStack);
            item.setVelocity(new Vector(0, 0.2, 0));
            item.setPickupDelay(0);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (item != null && !item.isDead()) {
                    item.remove();
                }
            }, 200L);
        }

        private String getRomanNumeral(int number) {
            switch (number) {
                case 1: return "I";
                case 2: return "II";
                case 3: return "III";
                case 4: return "IV";
                default: return String.valueOf(number);
            }
        }

        public GeneratorType getType() {
            return type;
        }

        public void setLevel(int level) {
            this.level = Math.min(level, 4);
        }

        public int getLevel() {
            return level;
        }
    }

    public enum GeneratorType {
        BRONZE("§6Bronze", "§6", Material.CLAY_BRICK, new int[]{2, 1, 1, 1}),
        IRON("§7Eisen", "§7", Material.IRON_INGOT, new int[]{4, 3, 2, 1}),
        GOLD("§eGold", "§e", Material.GOLD_INGOT, new int[]{10, 8, 6, 4});

        private final String displayName;
        private final String color;
        private final Material material;
        private final int[] delays;

        GeneratorType(String displayName, String color, Material material, int[] delays) {
            this.displayName = displayName;
            this.color = color;
            this.material = material;
            this.delays = delays;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getColor() {
            return color;
        }

        public ItemStack getItemStack() {
            return new ItemStack(material, 1);
        }

        public int getBaseDelay(int level) {
            return delays[Math.min(level - 1, delays.length - 1)];
        }
    }
}