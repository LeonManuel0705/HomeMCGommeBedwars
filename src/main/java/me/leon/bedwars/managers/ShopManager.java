package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Game;
import me.leon.bedwars.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
        // Blöcke
        ShopCategory blocks = new ShopCategory("Blöcke", Material.SANDSTONE);
        blocks.addItem(new ShopItem(
                new ItemBuilder(Material.SANDSTONE, 2).setDisplayName("§eSandstein §7x2").build(),
                1, ShopCurrency.BRONZE
        ));
        blocks.addItem(new ShopItem(
                new ItemBuilder(Material.ENDER_STONE, 3).setDisplayName("§eEndstein §7x3").build(),
                7, ShopCurrency.BRONZE
        ));
        blocks.addItem(new ShopItem(
                new ItemBuilder(Material.GLASS, 2).setDisplayName("§eGlas §7x2").build(),
                1, ShopCurrency.BRONZE
        ));
        blocks.addItem(new ShopItem(
                new ItemBuilder(Material.WOOD, 3).setDisplayName("§eHolz §7x3").build(),
                1, ShopCurrency.BRONZE
        ));
        categories.put("blocks", blocks);

        // Waffen
        ShopCategory weapons = new ShopCategory("Waffen", Material.STONE_SWORD);
        weapons.addItem(new ShopItem(
                new ItemBuilder(Material.STONE_SWORD).setDisplayName("§eSteinschwert").build(),
                10, ShopCurrency.BRONZE
        ));
        weapons.addItem(new ShopItem(
                new ItemBuilder(Material.IRON_SWORD).setDisplayName("§eEisenschwert").build(),
                5, ShopCurrency.IRON
        ));
        weapons.addItem(new ShopItem(
                new ItemBuilder(Material.DIAMOND_SWORD).setDisplayName("§eDiamantschwert").build(),
                3, ShopCurrency.GOLD
        ));
        weapons.addItem(new ShopItem(
                new ItemBuilder(Material.STICK)
                        .setDisplayName("§eKnockback-Stock")
                        .addEnchantment(Enchantment.KNOCKBACK, 1)
                        .build(),
                5, ShopCurrency.BRONZE
        ));
        categories.put("weapons", weapons);

        // Rüstung
        ShopCategory armor = new ShopCategory("Rüstung", Material.CHAINMAIL_CHESTPLATE);
        armor.addItem(new ShopItem(
                new ItemBuilder(Material.CHAINMAIL_BOOTS).setDisplayName("§eKettenrüstung Schuhe").build(),
                1, ShopCurrency.IRON
        ));
        armor.addItem(new ShopItem(
                new ItemBuilder(Material.CHAINMAIL_LEGGINGS).setDisplayName("§eKettenrüstung Hose").build(),
                1, ShopCurrency.IRON
        ));
        armor.addItem(new ShopItem(
                new ItemBuilder(Material.CHAINMAIL_CHESTPLATE).setDisplayName("§eKettenrüstung Brustpanzer").build(),
                3, ShopCurrency.IRON
        ));
        armor.addItem(new ShopItem(
                new ItemBuilder(Material.IRON_CHESTPLATE).setDisplayName("§eEisenrüstung Brustpanzer").build(),
                12, ShopCurrency.IRON
        ));
        armor.addItem(new ShopItem(
                new ItemBuilder(Material.DIAMOND_BOOTS).setDisplayName("§eDiamantrüstung Schuhe").build(),
                4, ShopCurrency.GOLD
        ));
        armor.addItem(new ShopItem(
                new ItemBuilder(Material.DIAMOND_LEGGINGS).setDisplayName("§eDiamantrüstung Hose").build(),
                6, ShopCurrency.GOLD
        ));
        categories.put("armor", armor);

        // Werkzeuge
        ShopCategory tools = new ShopCategory("Werkzeuge", Material.STONE_PICKAXE);
        tools.addItem(new ShopItem(
                new ItemBuilder(Material.WOOD_PICKAXE).setDisplayName("§eHolzspitzhacke").build(),
                3, ShopCurrency.BRONZE
        ));
        tools.addItem(new ShopItem(
                new ItemBuilder(Material.STONE_PICKAXE).setDisplayName("§eSteinspitzhacke").build(),
                1, ShopCurrency.IRON
        ));
        tools.addItem(new ShopItem(
                new ItemBuilder(Material.IRON_PICKAXE).setDisplayName("§eEisenspitzhacke").build(),
                3, ShopCurrency.IRON
        ));
        tools.addItem(new ShopItem(
                new ItemBuilder(Material.DIAMOND_PICKAXE)
                        .setDisplayName("§eDiamantspitzhacke")
                        .addEnchantment(Enchantment.DIG_SPEED, 1)
                        .build(),
                6, ShopCurrency.GOLD
        ));
        tools.addItem(new ShopItem(
                new ItemBuilder(Material.WOOD_AXE).setDisplayName("§eHolzaxt").build(),
                3, ShopCurrency.BRONZE
        ));
        tools.addItem(new ShopItem(
                new ItemBuilder(Material.STONE_AXE).setDisplayName("§eSteinsaxt").build(),
                1, ShopCurrency.IRON
        ));
        tools.addItem(new ShopItem(
                new ItemBuilder(Material.SHEARS).setDisplayName("§eSchere").build(),
                2, ShopCurrency.IRON
        ));
        categories.put("tools", tools);

        // Bögen
        ShopCategory bows = new ShopCategory("Bögen", Material.BOW);
        bows.addItem(new ShopItem(
                new ItemBuilder(Material.BOW).setDisplayName("§eBogen").build(),
                6, ShopCurrency.GOLD
        ));
        bows.addItem(new ShopItem(
                new ItemBuilder(Material.BOW)
                        .setDisplayName("§eBogen Power I")
                        .addEnchantment(Enchantment.ARROW_DAMAGE, 1)
                        .build(),
                12, ShopCurrency.GOLD
        ));
        bows.addItem(new ShopItem(
                new ItemBuilder(Material.BOW)
                        .setDisplayName("§eBogen Power I, Punch I")
                        .addEnchantment(Enchantment.ARROW_DAMAGE, 1)
                        .addEnchantment(Enchantment.ARROW_KNOCKBACK, 1)
                        .build(),
                20, ShopCurrency.GOLD
        ));
        bows.addItem(new ShopItem(
                new ItemBuilder(Material.ARROW, 6).setDisplayName("§ePfeile §7x6").build(),
                2, ShopCurrency.GOLD
        ));
        categories.put("bows", bows);

        // Essen
        ShopCategory food = new ShopCategory("Essen", Material.COOKED_BEEF);
        food.addItem(new ShopItem(
                new ItemBuilder(Material.COOKED_BEEF, 3).setDisplayName("§eSteak §7x3").build(),
                3, ShopCurrency.BRONZE
        ));
        food.addItem(new ShopItem(
                new ItemBuilder(Material.GOLDEN_APPLE).setDisplayName("§eGoldener Apfel").build(),
                3, ShopCurrency.GOLD
        ));
        categories.put("food", food);

        // Tränke
        ShopCategory potions = new ShopCategory("Tränke", Material.POTION);
        potions.addItem(new ShopItem(
                new ItemBuilder(Material.POTION, 1, (short) 8193).setDisplayName("§eHeilungstrank").build(),
                1, ShopCurrency.IRON
        ));
        potions.addItem(new ShopItem(
                new ItemBuilder(Material.POTION, 1, (short) 8201).setDisplayName("§eSchnelligkeitstrank").build(),
                3, ShopCurrency.IRON
        ));
        potions.addItem(new ShopItem(
                new ItemBuilder(Material.POTION, 1, (short) 8233).setDisplayName("§eStärketrank").build(),
                5, ShopCurrency.IRON
        ));
        categories.put("potions", potions);

        // Spezial
        ShopCategory special = new ShopCategory("Spezial", Material.TNT);
        special.addItem(new ShopItem(
                new ItemBuilder(Material.TNT).setDisplayName("§eTNT").build(),
                3, ShopCurrency.IRON
        ));
        special.addItem(new ShopItem(
                new ItemBuilder(Material.WATER_BUCKET).setDisplayName("§eWassereimer").build(),
                5, ShopCurrency.GOLD
        ));
        special.addItem(new ShopItem(
                new ItemBuilder(Material.ENDER_PEARL).setDisplayName("§eEnderperle").build(),
                7, ShopCurrency.GOLD
        ));
        special.addItem(new ShopItem(
                new ItemBuilder(Material.WEB, 2).setDisplayName("§eSpinnennetz §7x2").build(),
                5, ShopCurrency.IRON
        ));
        categories.put("special", special);
    }

    public void openShop(Player player, Game game) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Shop");

        // Kategorien
        inv.setItem(10, createCategoryIcon("blocks", "§eBlöcke"));
        inv.setItem(12, createCategoryIcon("weapons", "§eWaffen"));
        inv.setItem(14, createCategoryIcon("armor", "§eRüstung"));
        inv.setItem(16, createCategoryIcon("tools", "§eWerkzeuge"));
        inv.setItem(28, createCategoryIcon("bows", "§eBögen"));
        inv.setItem(30, createCategoryIcon("food", "§eEssen"));
        inv.setItem(32, createCategoryIcon("potions", "§eTränke"));
        inv.setItem(34, createCategoryIcon("special", "§eSpezial"));

        // Glasscheiben zur Dekoration
        for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 50, 51, 52, 53}) {
            inv.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (short) 7).setDisplayName(" ").build());
        }

        // Schließen
        inv.setItem(49, new ItemBuilder(Material.BARRIER).setDisplayName("§cSchließen").build());

        player.openInventory(inv);
    }

    private ItemStack createCategoryIcon(String categoryKey, String displayName) {
        ShopCategory category = categories.get(categoryKey);
        return new ItemBuilder(category.getIcon())
                .setDisplayName(displayName)
                .setLore("§7Klicke zum Öffnen")
                .build();
    }

    public void openCategory(Player player, String categoryKey) {
        ShopCategory category = categories.get(categoryKey);
        if (category == null) return;

        Inventory inv = Bukkit.createInventory(null, 54, "§8" + category.getName());

        int slot = 10;
        int row = 0;
        for (ShopItem item : category.getItems()) {
            ItemStack display = item.getDisplay().clone();
            ItemBuilder builder = new ItemBuilder(display.getType(), display.getAmount(), display.getDurability());

            if (display.hasItemMeta() && display.getItemMeta().hasDisplayName()) {
                builder.setDisplayName(display.getItemMeta().getDisplayName());
            }

            builder.setLore(
                    "",
                    "§7Preis: " + getCurrencyColor(item.getCurrency()) + item.getPrice() + " " + getCurrencyName(item.getCurrency()),
                    "",
                    "§eKlicke zum Kaufen!"
            );

            inv.setItem(slot, builder.build());

            slot++;
            if (slot == 17 || slot == 26 || slot == 35) {
                row++;
                slot = 10 + (row * 9);
            }
        }

        // Glasscheiben
        for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 50, 51, 52, 53}) {
            inv.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (short) 7).setDisplayName(" ").build());
        }

        inv.setItem(49, new ItemBuilder(Material.ARROW).setDisplayName("§aZurück").build());

        player.openInventory(inv);
    }

    public boolean purchase(Player player, ShopItem item, Game game) {
        if (!hasEnoughCurrency(player, item)) {
            player.sendMessage(BedWars.PREFIX + "§cDu hast nicht genug Ressourcen!");
            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        removeCurrency(player, item);

        ItemStack itemToGive = item.getDisplay().clone();
        itemToGive.setItemMeta(null);

        player.getInventory().addItem(itemToGive);

        player.sendMessage(BedWars.PREFIX + "§aDu hast §e" + item.getDisplay().getItemMeta().getDisplayName() + " §agekauft!");
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.5f);

        return true;
    }

    private boolean hasEnoughCurrency(Player player, ShopItem item) {
        int has = 0;
        Material currency = getCurrencyMaterial(item.getCurrency());
        String requiredName = getCurrencyDisplayName(item.getCurrency());

        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == currency) {
                if (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()) {
                    if (stack.getItemMeta().getDisplayName().equals(requiredName)) {
                        has += stack.getAmount();
                    }
                }
            }
        }

        return has >= item.getPrice();
    }

    private void removeCurrency(Player player, ShopItem item) {
        int toRemove = item.getPrice();
        Material currency = getCurrencyMaterial(item.getCurrency());
        String requiredName = getCurrencyDisplayName(item.getCurrency());

        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == currency) {
                if (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()) {
                    if (stack.getItemMeta().getDisplayName().equals(requiredName)) {
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
        }
    }

    private String getCurrencyDisplayName(ShopCurrency currency) {
        switch (currency) {
            case BRONZE: return "§6Bronze";
            case IRON: return "§7Eisen";
            case GOLD: return "§eGold";
            default: return "";
        }
    }

    private Material getCurrencyMaterial(ShopCurrency currency) {
        switch (currency) {
            case BRONZE: return Material.CLAY_BRICK;
            case IRON: return Material.IRON_INGOT;
            case GOLD: return Material.GOLD_INGOT;
            default: return Material.CLAY_BRICK;
        }
    }

    private String getCurrencyColor(ShopCurrency currency) {
        switch (currency) {
            case BRONZE: return "§6";
            case IRON: return "§7";
            case GOLD: return "§e";
            default: return "§7";
        }
    }

    private String getCurrencyName(ShopCurrency currency) {
        switch (currency) {
            case BRONZE: return "Bronze";
            case IRON: return "Eisen";
            case GOLD: return "Gold";
            default: return "???";
        }
    }

    public ShopCategory getCategory(String key) {
        return categories.get(key);
    }

    public static class ShopCategory {
        private final String name;
        private final Material icon;
        private final List<ShopItem> items;

        public ShopCategory(String name, Material icon) {
            this.name = name;
            this.icon = icon;
            this.items = new ArrayList<>();
        }

        public void addItem(ShopItem item) {
            items.add(item);
        }

        public String getName() { return name; }
        public Material getIcon() { return icon; }
        public List<ShopItem> getItems() { return items; }
    }

    public static class ShopItem {
        private final ItemStack display;
        private final int price;
        private final ShopCurrency currency;

        public ShopItem(ItemStack display, int price, ShopCurrency currency) {
            this.display = display;
            this.price = price;
            this.currency = currency;
        }

        public ItemStack getDisplay() { return display.clone(); }
        public int getPrice() { return price; }
        public ShopCurrency getCurrency() { return currency; }
    }

    public enum ShopCurrency {
        BRONZE, IRON, GOLD
    }
}