package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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

        // Team-Generatoren (Bronze + Eisen getrennt)
        for (TeamData team : game.getMapData().getTeams()) {
            for (Location loc : team.getGenerators()) {
                // Bronze Generator
                Generator bronzeGen = new Generator(loc.clone(), GeneratorType.BRONZE);
                bronzeGen.start();
                generators.add(bronzeGen);

                // Eisen Generator (leicht versetzt wenn gleicher Block)
                Generator ironGen = new Generator(loc.clone().add(0.1, 0, 0.1), GeneratorType.IRON);
                ironGen.start();
                generators.add(ironGen);
            }
        }

        // Bronze-Generatoren auf der Map
        for (Location loc : game.getMapData().getBronzeGenerators()) {
            Generator gen = new Generator(loc, GeneratorType.BRONZE);
            gen.start();
            generators.add(gen);
        }

        // Gold-Generatoren auf der Map
        for (Location loc : game.getMapData().getGoldGenerators()) {
            Generator gen = new Generator(loc, GeneratorType.GOLD);
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
        private BukkitRunnable task;
        private int nextSpawnIn;
        private int currentAmount;

        public Generator(Location location, GeneratorType type) {
            this.location = location.clone().add(0.5, 0, 0.5);
            this.type = type;
            this.nextSpawnIn = type.getDelay();
            this.currentAmount = 0;
        }

        public void start() {
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    nextSpawnIn--;

                    if (nextSpawnIn <= 0) {
                        if (currentAmount < type.getMaxItems()) {
                            spawnItem();
                            currentAmount++;
                        }
                        nextSpawnIn = type.getDelay();
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
            // Prüfe ob bereits zu viele Items am Boden liegen
            int nearbyItems = 0;
            for (org.bukkit.entity.Entity entity : location.getWorld().getNearbyEntities(location, 1, 1, 1)) {
                if (entity instanceof Item) {
                    Item item = (Item) entity;
                    if (item.getItemStack().getType() == type.getMaterial()) {
                        nearbyItems += item.getItemStack().getAmount();
                    }
                }
            }

            if (nearbyItems >= type.getMaxItems()) {
                return;
            }

            ItemStack itemStack = type.getItemStack();
            Item item = location.getWorld().dropItem(location, itemStack);
            item.setVelocity(new Vector(0, 0.1, 0));
            item.setPickupDelay(0);

            // Item nach 30 Sekunden entfernen
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (item != null && !item.isDead()) {
                    item.remove();
                    currentAmount = Math.max(0, currentAmount - 1);
                }
            }, 600L);
        }

        public GeneratorType getType() {
            return type;
        }
    }

    public enum GeneratorType {
        BRONZE("§6Bronze", Material.CLAY_BRICK, 2, 128),
        IRON("§7Eisen", Material.IRON_INGOT, 20, 32),
        GOLD("§eGold", Material.GOLD_INGOT, 60, 16);

        private final String displayName;
        private final Material material;
        private final int delay;
        private final int maxItems;

        GeneratorType(String displayName, Material material, int delay, int maxItems) {
            this.displayName = displayName;
            this.material = material;
            this.delay = delay;
            this.maxItems = maxItems;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Material getMaterial() {
            return material;
        }

        public ItemStack getItemStack() {
            ItemStack item = new ItemStack(material, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(displayName);
            item.setItemMeta(meta);
            return item;
        }

        public int getDelay() {
            return delay;
        }

        public int getMaxItems() {
            return maxItems;
        }
    }
}