package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;

import java.util.*;

public class ShopManager {

    private final BedWars plugin;
    private final Map<String, ShopCategory> categories;

    public ShopManager(BedWars plugin) {
        this.plugin = plugin;
        this.categories = new LinkedHashMap<>();

        setupShop();
    }

    private void setupShop() {
        ShopCategory blocks = new ShopCategory("Blöcke", Material.SANDSTONE, 0);
        blocks.addItem(new ShopItem(
                new ItemBuilder(Material.SANDSTONE, 2)
                        .setDisplayName("§eSandstein")
                        .setLore("§7Preis: §64 Bronze")
                        .build(),
                new ItemStack(Material.CLAY_BRICK, 4), ShopCurrency.BRONZE
        ));
        blocks.addItem(new ShopItem(
                new ItemBuilder(Material.ENDER_STONE, 1)
                        .setDisplayName("§eEndstein")
                        .setLore("§7Preis: §624 Bronze")
                        .build(),
                new ItemStack(Material.CLAY_BRICK, 24), ShopCurrency.BRONZE
        ));
        categories.put("blocks", blocks);

        ShopCategory weapons = new ShopCategory("Waffen", Material.GOLD_SWORD, 1);
        weapons.addItem(new ShopItem(
                new ItemBuilder(Material.STICK)
                        .setDisplayName("§eStock §7(Knockback I)")
                        .setLore("§7Preis: §610 Bronze")
                        .addEnchantment(org.bukkit.enchantments.Enchantment.KNOCKBACK, 1)
                        .build(),
                new ItemStack(Material.CLAY_BRICK, 10), ShopCurrency.BRONZE
        ));
        categories.put("weapons", weapons);

        ShopCategory armor = new ShopCategory("Rüstung", Material.CHAINMAIL_CHESTPLATE, 2);
        armor.addItem(new ShopItem(
                new ItemBuilder(Material.CHAINMAIL_BOOTS)
                        .setDisplayName("§eKettenrüstung Schuhe")
                        .setLore("§7Preis: §64 Eisen")
                        .build(),
                new ItemStack(Material.IRON_INGOT, 4), ShopCurrency.IRON
        ));
        categories.put("armor", armor);

        ShopCategory tools = new ShopCategory("Werkzeuge", Material.STONE_PICKAXE, 3);
        tools.addItem(new ShopItem(
                new ItemBuilder(Material.WOOD_PICKAXE)
                        .setDisplayName("§eHolzspitzhacke")
                        .setLore("§7Preis: §66 Bronze")
                        .build(),
                new ItemStack(Material.CLAY_BRICK, 6), ShopCurrency.BRONZE
        ));
        categories.put("tools", tools);

        ShopCategory food = new ShopCategory("Essen", Material.COOKED_BEEF, 4);
        food.addItem(new ShopItem(
                new ItemBuilder(Material.COOKED_BEEF, 2)
                        .setDisplayName("§eSteak")
                        .setLore("§7Preis: §64 Bronze")
                        .build(),
                new ItemStack(Material.CLAY_BRICK, 4), ShopCurrency.BRONZE
        ));
        categories.put("food", food);

        ShopCategory special = new ShopCategory("Spezial", Material.TNT, 5);
        special.addItem(new ShopItem(
                new ItemBuilder(Material.TNT)
                        .setDisplayName("§eTNT")
                        .setLore("§7Preis: §68 Eisen")
                        .build(),
                new ItemStack(Material.IRON_INGOT, 8), ShopCurrency.IRON
        ));
        categories.put("special", special);
    }

    public void openShop(Player player, Game game) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Shop");

        int slot = 10;
        for (ShopCategory category : categories.values()) {
            inv.setItem(slot, category.getIcon());
            slot++;
        }

        ItemStack close = new ItemBuilder(Material.BARRIER)
                .setDisplayName("§cSchließen")
                .build();
        inv.setItem(49, close);

        player.openInventory(inv);
    }

    public void openCategory(Player player, String categoryKey) {
        ShopCategory category = categories.get(categoryKey);
        if (category == null) return;

        Inventory inv = Bukkit.createInventory(null, 54, "§8Shop - " + category.getName());

        int slot = 10;
        for (ShopItem item : category.getItems()) {
            inv.setItem(slot, item.getDisplay());
            slot++;
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
        }

        ItemStack back = new ItemBuilder(Material.ARROW)
                .setDisplayName("§aZurück")
                .build();
        inv.setItem(49, back);

        player.openInventory(inv);
    }

    public boolean purchase(Player player, ShopItem item) {
        if (!hasEnoughCurrency(player, item)) {
            player.sendMessage(BedWars.PREFIX + "§cDu hast nicht genug Ressourcen!");
            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        removeCurrency(player, item);
        player.getInventory().addItem(item.getItem());

        player.sendMessage(BedWars.PREFIX + "§aDu hast §e" + item.getDisplay().getItemMeta().getDisplayName() + " §agekauft!");
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.5f);

        return true;
    }

    private boolean hasEnoughCurrency(Player player, ShopItem item) {
        int has = 0;
        Material currency = item.getCurrency() == ShopCurrency.BRONZE ?
                Material.CLAY_BRICK : Material.IRON_INGOT;

        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == currency) {
                has += stack.getAmount();
            }
        }

        return has >= item.getPrice().getAmount();
    }

    private void removeCurrency(Player player, ShopItem item) {
        int toRemove = item.getPrice().getAmount();
        Material currency = item.getCurrency() == ShopCurrency.BRONZE ?
                Material.CLAY_BRICK : Material.IRON_INGOT;

        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == currency) {
                if (stack.getAmount() >= toRemove) {
                    stack.setAmount(stack.getAmount() - toRemove);
                    break;
                } else {
                    toRemove -= stack.getAmount();
                    stack.setAmount(0);
                }
            }
        }
    }

    public ShopCategory getCategory(String key) {
        return categories.get(key);
    }

    public static class ShopCategory {
        private final String name;
        private final ItemStack icon;
        private final List<ShopItem> items;

        public ShopCategory(String name, Material iconMaterial, int iconSlot) {
            this.name = name;
            this.icon = new ItemBuilder(iconMaterial)
                    .setDisplayName("§e" + name)
                    .setLore("§7Klicke zum Öffnen")
                    .build();
            this.items = new ArrayList<>();
        }

        public void addItem(ShopItem item) {
            items.add(item);
        }

        public String getName() { return name; }
        public ItemStack getIcon() { return icon; }
        public List<ShopItem> getItems() { return items; }
    }

    public static class ShopItem {
        private final ItemStack display;
        private final ItemStack price;
        private final ShopCurrency currency;

        public ShopItem(ItemStack display, ItemStack price, ShopCurrency currency) {
            this.display = display;
            this.price = price;
            this.currency = currency;
        }

        public ItemStack getDisplay() { return display.clone(); }
        public ItemStack getItem() {
            ItemStack item = display.clone();
            item.setItemMeta(null);
            return item;
        }
        public ItemStack getPrice() { return price; }
        public ShopCurrency getCurrency() { return currency; }
    }

    public enum ShopCurrency {
        BRONZE, IRON, GOLD
    }
}