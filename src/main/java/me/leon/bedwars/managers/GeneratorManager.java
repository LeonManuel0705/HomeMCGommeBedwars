package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import org.bukkit.Location;
import org.bukkit.Material;
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

        // Team-Generatoren (Bronze + Eisen)
        for (TeamData team : game.getMapData().getTeams()) {
            for (Location loc : team.getGenerators()) {
                // Bronze Generator
                Generator bronzeGen = new Generator(loc.clone().add(0, 0, 0), GeneratorType.BRONZE, 1);
                bronzeGen.start();
                generators.add(bronzeGen);

                // Eisen Generator
                Generator ironGen = new Generator(loc.clone().add(0, 0.5, 0), GeneratorType.IRON, 1);
                ironGen.start();
                generators.add(ironGen);
            }
        }

        // Bronze-Generatoren auf der Map
        for (Location loc : game.getMapData().getBronzeGenerators()) {
            Generator gen = new Generator(loc, GeneratorType.BRONZE, 1);
            gen.start();
            generators.add(gen);
        }

        // Gold-Generatoren auf der Map
        for (Location loc : game.getMapData().getGoldGenerators()) {
            Generator gen = new Generator(loc, GeneratorType.GOLD, 1);
            gen.start();
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

    public class Generator {
        private final Location location;
        private final GeneratorType type;
        private final int level;
        private BukkitRunnable task;
        private int nextSpawnIn;

        public Generator(Location location, GeneratorType type, int level) {
            this.location = location.clone().add(0.5, 0, 0.5);
            this.type = type;
            this.level = level;
            this.nextSpawnIn = type.getBaseDelay(level);
        }

        public void start() {
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    nextSpawnIn--;

                    if (nextSpawnIn <= 0) {
                        spawnItem();
                        nextSpawnIn = type.getBaseDelay(level);
                    }
                }
            };
            task.runTaskTimer(plugin, 0L, 20L);
        }

        public void stop() {
            if (task != null) {
                task.cancel();
            }
        }

        private void spawnItem() {
            ItemStack itemStack = type.getItemStack();
            Item item = location.getWorld().dropItem(location, itemStack);
            item.setVelocity(new Vector(0, 0.1, 0));
            item.setPickupDelay(0);

            // Item nach 10 Sekunden entfernen
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (item != null && !item.isDead()) {
                    item.remove();
                }
            }, 200L);
        }

        public GeneratorType getType() {
            return type;
        }

        public void setLevel(int level) {
            // Keine Upgrades in diesem System
        }

        public int getLevel() {
            return level;
        }
    }

    public enum GeneratorType {
        BRONZE("§6Bronze", Material.CLAY_BRICK, new int[]{2, 2, 2, 2}),
        IRON("§7Eisen", Material.IRON_INGOT, new int[]{4, 4, 4, 4}),
        GOLD("§eGold", Material.GOLD_INGOT, new int[]{10, 10, 10, 10});

        private final String displayName;
        private final Material material;
        private final int[] delays;

        GeneratorType(String displayName, Material material, int[] delays) {
            this.displayName = displayName;
            this.material = material;
            this.delays = delays;
        }

        public String getDisplayName() {
            return displayName;
        }

        public ItemStack getItemStack() {
            return new ItemStack(material, 1);
        }

        public int getBaseDelay(int level) {
            return delays[Math.min(level - 1, delays.length - 1)];
        }
    }
}