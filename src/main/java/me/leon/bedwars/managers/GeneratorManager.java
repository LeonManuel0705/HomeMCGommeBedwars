package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.Arena.GeneratorData;
import me.leon.bedwars.game.Arena.GeneratorType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class GeneratorManager {

    private final BedWars plugin;

    public GeneratorManager(BedWars plugin) {
        this.plugin = plugin;
    }

    public void processGenerators(Arena arena) {
        for (GeneratorData gen : arena.getGenerators().values()) {
            gen.incrementTimer();

            boolean shouldSpawn = false;
            Material material = null;

            switch (gen.getType()) {
                case BRONZE:
                    if (gen.getTimer() >= 1.2) { // 1.2 Sekunden
                        shouldSpawn = true;
                        material = Material.CLAY_BRICK;
                    }
                    break;
                case IRON:
                    if (gen.getTimer() >= 25) { // 25 Sekunden
                        shouldSpawn = true;
                        material = Material.IRON_INGOT;
                    }
                    break;
                case GOLD:
                    if (arena.isGoldEnabled() && gen.getTimer() >= 80) { // 80 Sekunden
                        shouldSpawn = true;
                        material = Material.GOLD_INGOT;
                    }
                    break;
            }

            if (shouldSpawn && material != null) {
                spawnItem(gen.getLocation(), material);
                gen.setTimer(0);
            }
        }
    }

    private void spawnItem(Location location, Material material) {
        ItemStack item = new ItemStack(material, 1);
        Item droppedItem = location.getWorld().dropItem(location.clone().add(0.5, 1.0, 0.5), item);
        droppedItem.setVelocity(droppedItem.getVelocity().multiply(0));
        droppedItem.setPickupDelay(0);
    }

    public void createGenerator(Arena arena, Location location, GeneratorType type) {
        GeneratorData gen = new GeneratorData(type, location);
        arena.getGenerators().put(location, gen);

        createGeneratorHologram(location, type);
    }

    private void createGeneratorHologram(Location location, GeneratorType type) {
        Location hologramLoc = location.clone().add(0.5, 2.0, 0.5);

        org.bukkit.entity.ArmorStand stand = location.getWorld().spawn(hologramLoc,
                org.bukkit.entity.ArmorStand.class);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setCustomNameVisible(true);
        stand.setMarker(true);

        String name = "§e§lERROR";
        switch (type) {
            case BRONZE:
                name = "§6§lBRONZE";
                break;
            case IRON:
                name = "§f§lEISEN";
                break;
            case GOLD:
                name = "§e§lGOLD";
                break;
        }
        stand.setCustomName(name);
    }
}