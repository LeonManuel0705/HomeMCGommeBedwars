package me.leon.bedwars.shop;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.managers.TeamManager.BedWarsTeam;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.*;

public class ShopManager {

    private final BedWars plugin;
    private final Map<ShopCategory, List<ShopItem>> shopItems;

    public ShopManager(BedWars plugin) {
        this.plugin = plugin;
        this.shopItems = new HashMap<>();
        initializeShop();
    }

    private void initializeShop() {
        List<ShopItem> blocks = new ArrayList<>();
        blocks.add(new ShopItem("§eSandstein §8x2", Material.SANDSTONE, 2, Currency.BRONZE, 1,
                "§71 Bronze", null));
        blocks.add(new ShopItem("§bEis", Material.ICE, 1, Currency.BRONZE, 2,
                "§72 Bronze", null));
        blocks.add(new ShopItem("§eGlowstone §8x2", Material.GLOWSTONE, 2, Currency.BRONZE, 2,
                "§72 Bronze", null));
        blocks.add(new ShopItem("§fGlas", Material.GLASS, 1, Currency.BRONZE, 2,
                "§72 Bronze", null));
        blocks.add(new ShopItem("§eSandsteintreppe", Material.SANDSTONE_STAIRS, 1, Currency.BRONZE, 4,
                "§74 Bronze", null));
        blocks.add(new ShopItem("§eEndstein", Material.ENDER_STONE, 1, Currency.BRONZE, 7,
                "§77 Bronze", null));
        blocks.add(new ShopItem("§eBefallener Sandstein", Material.MONSTER_EGGS, (byte)1, 1, Currency.BRONZE, 8,
                "§78 Bronze", "§7Spawnt Silberfisch"));
        blocks.add(new ShopItem("§fEisenblock", Material.IRON_BLOCK, 1, Currency.IRON, 3,
                "§73 Eisen", null));
        shopItems.put(ShopCategory.BLOCKS, blocks);

        List<ShopItem> armor = new ArrayList<>();
        armor.add(new ShopItem("§eLederhelm", Material.LEATHER_HELMET, 1, Currency.BRONZE, 1,
                "§71 Bronze", "§7Schutz I, Haltbarkeit I",
                new EnchantData(Enchantment.PROTECTION_ENVIRONMENTAL, 1),
                new EnchantData(Enchantment.DURABILITY, 1)));
        armor.add(new ShopItem("§eLederhose", Material.LEATHER_LEGGINGS, 1, Currency.BRONZE, 1,
                "§71 Bronze", "§7Schutz I, Haltbarkeit I",
                new EnchantData(Enchantment.PROTECTION_ENVIRONMENTAL, 1),
                new EnchantData(Enchantment.DURABILITY, 1)));
        armor.add(new ShopItem("§eLederschuhe", Material.LEATHER_BOOTS, 1, Currency.BRONZE, 1,
                "§71 Bronze", "§7Schutz I, Haltbarkeit I",
                new EnchantData(Enchantment.PROTECTION_ENVIRONMENTAL, 1),
                new EnchantData(Enchantment.DURABILITY, 1)));

        armor.add(new ShopItem("§fKettenhemd I", Material.CHAINMAIL_CHESTPLATE, 1, Currency.IRON, 1,
                "§71 Eisen", "§7Schutz I, Haltbarkeit I",
                new EnchantData(Enchantment.PROTECTION_ENVIRONMENTAL, 1),
                new EnchantData(Enchantment.DURABILITY, 1)));
        armor.add(new ShopItem("§fKettenhemd II", Material.CHAINMAIL_CHESTPLATE, 1, Currency.IRON, 3,
                "§73 Eisen", "§7Schutz II, Haltbarkeit I",
                new EnchantData(Enchantment.PROTECTION_ENVIRONMENTAL, 2),
                new EnchantData(Enchantment.DURABILITY, 1)));
        armor.add(new ShopItem("§fKettenhemd III", Material.CHAINMAIL_CHESTPLATE, 1, Currency.IRON, 7,
                "§77 Eisen", "§7Schutz III, Haltbarkeit I",
                new EnchantData(Enchantment.PROTECTION_ENVIRONMENTAL, 3),
                new EnchantData(Enchantment.DURABILITY, 1)));

        armor.add(new ShopItem("§eLederbrustpanzer", Material.LEATHER_CHESTPLATE, 1, Currency.IRON, 3,
                "§73 Eisen", "§7Schutz III, Haltbarkeit I, Dornen III",
                new EnchantData(Enchantment.PROTECTION_ENVIRONMENTAL, 3),
                new EnchantData(Enchantment.DURABILITY, 1),
                new EnchantData(Enchantment.THORNS, 3)));
        armor.add(new ShopItem("§6Goldstiefel", Material.GOLD_BOOTS, 1, Currency.IRON, 3,
                "§73 Eisen", "§7Federfall III",
                new EnchantData(Enchantment.PROTECTION_FALL, 3)));
        shopItems.put(ShopCategory.ARMOR, armor);

        List<ShopItem> tools = new ArrayList<>();
        tools.add(new ShopItem("§eHolzspitzhacke", Material.WOOD_PICKAXE, 1, Currency.BRONZE, 4,
                "§74 Bronze", "§7Effizienz I",
                new EnchantData(Enchantment.DIG_SPEED, 1)));
        tools.add(new ShopItem("§fSteinspitzhacke", Material.STONE_PICKAXE, 1, Currency.IRON, 2,
                "§72 Eisen", "§7Effizienz I, Haltbarkeit I",
                new EnchantData(Enchantment.DIG_SPEED, 1),
                new EnchantData(Enchantment.DURABILITY, 1)));
        tools.add(new ShopItem("§bEisenspitzhacke", Material.IRON_PICKAXE, 1, Currency.GOLD, 1,
                "§71 Gold", "§7Effizienz III, Haltbarkeit I",
                new EnchantData(Enchantment.DIG_SPEED, 3),
                new EnchantData(Enchantment.DURABILITY, 1)));
        shopItems.put(ShopCategory.TOOLS, tools);

        List<ShopItem> weapons = new ArrayList<>();
        weapons.add(new ShopItem("§eStock", Material.STICK, 1, Currency.BRONZE, 5,
                "§75 Bronze", "§7Rückstoß I",
                new EnchantData(Enchantment.KNOCKBACK, 1)));
        weapons.add(new ShopItem("§6Goldschwert I", Material.GOLD_SWORD, 1, Currency.IRON, 1,
                "§71 Eisen", "§7Schärfe I, Haltbarkeit I",
                new EnchantData(Enchantment.DAMAGE_ALL, 1),
                new EnchantData(Enchantment.DURABILITY, 1)));
        weapons.add(new ShopItem("§6Goldschwert II", Material.GOLD_SWORD, 1, Currency.IRON, 3,
                "§73 Eisen", "§7Schärfe II, Haltbarkeit I",
                new EnchantData(Enchantment.DAMAGE_ALL, 2),
                new EnchantData(Enchantment.DURABILITY, 1)));
        weapons.add(new ShopItem("§fEisenschwert", Material.IRON_SWORD, 1, Currency.GOLD, 5,
                "§75 Gold", "§7Schärfe II, Haltbarkeit I, Rückstoß I",
                new EnchantData(Enchantment.DAMAGE_ALL, 2),
                new EnchantData(Enchantment.DURABILITY, 1),
                new EnchantData(Enchantment.KNOCKBACK, 1)));
        shopItems.put(ShopCategory.WEAPONS, weapons);

        List<ShopItem> potions = new ArrayList<>();
        potions.add(new ShopItem("§dUnsichtbarkeit §730s", Material.POTION, (byte)8206, 1, Currency.IRON, 32,
                "§732 Eisen", "§7Unsichtbarkeit für 30 Sekunden"));
        potions.add(new ShopItem("§aSpeed §7(20%, 3min)", Material.POTION, (byte)8194, 1, Currency.IRON, 7,
                "§77 Eisen", "§7Speed I für 3 Minuten"));
        potions.add(new ShopItem("§bSprungkraft §7(1:30min)", Material.POTION, (byte)8203, 1, Currency.IRON, 7,
                "§77 Eisen", "§7Sprungkraft I für 1:30 Minuten"));
        potions.add(new ShopItem("§cHeilung §7(4 Herzen)", Material.POTION, (byte)8261, 1, Currency.IRON, 5,
                "§75 Eisen", "§7Stellt 4 Herzen wieder her"));
        potions.add(new ShopItem("§cHeilung §7(2 Herzen)", Material.POTION, (byte)8197, 1, Currency.IRON, 3,
                "§73 Eisen", "§7Stellt 2 Herzen wieder her"));
        potions.add(new ShopItem("§4Stärke §7(130%, 3min)", Material.POTION, (byte)8201, 1, Currency.GOLD, 7,
                "§77 Gold", "§7Stärke I für 3 Minuten"));
        shopItems.put(ShopCategory.POTIONS, potions);

        List<ShopItem> bows = new ArrayList<>();
        bows.add(new ShopItem("§eBogen I", Material.BOW, 1, Currency.GOLD, 3,
                "§73 Gold", "§7Unendlichkeit",
                new EnchantData(Enchantment.ARROW_INFINITE, 1)));
        bows.add(new ShopItem("§eBogen II", Material.BOW, 1, Currency.GOLD, 7,
                "§77 Gold", "§7Stärke I, Unendlichkeit",
                new EnchantData(Enchantment.ARROW_DAMAGE, 1),
                new EnchantData(Enchantment.ARROW_INFINITE, 1)));
        bows.add(new ShopItem("§eBogen III", Material.BOW, 1, Currency.GOLD, 13,
                "§713 Gold", "§7Stärke I, Schlag I, Unendlichkeit",
                new EnchantData(Enchantment.ARROW_DAMAGE, 1),
                new EnchantData(Enchantment.ARROW_KNOCKBACK, 1),
                new EnchantData(Enchantment.ARROW_INFINITE, 1)));
        bows.add(new ShopItem("§ePfeil", Material.ARROW, 1, Currency.GOLD, 1,
                "§71 Gold", null));
        shopItems.put(ShopCategory.BOWS, bows);

        List<ShopItem> storage = new ArrayList<>();
        storage.add(new ShopItem("§eTruhe", Material.CHEST, 1, Currency.IRON, 1,
                "§71 Eisen", null));
        storage.add(new ShopItem("§dEndertruhe", Material.ENDER_CHEST, 1, Currency.GOLD, 1,
                "§71 Gold", "§7Persönlicher Speicher"));
        shopItems.put(ShopCategory.STORAGE, storage);

        List<ShopItem> special = new ArrayList<>();
        special.add(new ShopItem("§cTNT", Material.TNT, 1, Currency.GOLD, 2,
                "§72 Gold", null));
        special.add(new ShopItem("§eLeiter §8x3", Material.LADDER, 3, Currency.BRONZE, 1,
                "§71 Bronze", null));
        special.add(new ShopItem("§fSpinnennetz", Material.WEB, 1, Currency.BRONZE, 16,
                "§716 Bronze", null));
        special.add(new ShopItem("§aSlimeblock", Material.SLIME_BLOCK, 1, Currency.IRON, 1,
                "§71 Eisen", null));
        special.add(new ShopItem("§2Ranken §8x3", Material.VINE, 3, Currency.IRON, 3,
                "§73 Eisen", null));
        special.add(new ShopItem("§bRettungsplattform", Material.BLAZE_ROD, 1, Currency.GOLD, 3,
                "§73 Gold", "§7Spawnt 3x3 Plattform unter dir"));
        special.add(new ShopItem("§dEnderperle", Material.ENDER_PEARL, 1, Currency.GOLD, 11,
                "§711 Gold", null));
        shopItems.put(ShopCategory.SPECIAL, special);

        List<ShopItem> food = new ArrayList<>();
        food.add(new ShopItem("§cApfel", Material.APPLE, 1, Currency.BRONZE, 1,
                "§71 Bronze", null));
        food.add(new ShopItem("§dSchweinefleisch", Material.GRILLED_PORK, 1, Currency.BRONZE, 2,
                "§72 Bronze", null));
        food.add(new ShopItem("§eKuchen", Material.CAKE, 1, Currency.IRON, 1,
                "§71 Eisen", null));
        food.add(new ShopItem("§6Goldapfel", Material.GOLDEN_APPLE, 1, Currency.GOLD, 2,
                "§72 Gold", "§7Regeneration II"));
        shopItems.put(ShopCategory.FOOD, food);
    }

    public void openShop(Player player, Arena arena) {
        BedWarsTeam team = getPlayerTeam(arena, player);
        if (team == null) return;

        Inventory inv = plugin.getServer().createInventory(null, 54, "§8Shop");

        inv.setItem(10, createCategoryItem(ShopCategory.BLOCKS));
        inv.setItem(11, createCategoryItem(ShopCategory.ARMOR));
        inv.setItem(12, createCategoryItem(ShopCategory.TOOLS));
        inv.setItem(13, createCategoryItem(ShopCategory.WEAPONS));
        inv.setItem(14, createCategoryItem(ShopCategory.POTIONS));
        inv.setItem(15, createCategoryItem(ShopCategory.BOWS));
        inv.setItem(16, createCategoryItem(ShopCategory.STORAGE));
        inv.setItem(19, createCategoryItem(ShopCategory.SPECIAL));
        inv.setItem(20, createCategoryItem(ShopCategory.FOOD));

        ItemStack filler = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte)7);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }

        player.openInventory(inv);
    }

    public void openCategory(Player player, Arena arena, ShopCategory category) {
        BedWarsTeam team = getPlayerTeam(arena, player);
        if (team == null) return;

        List<ShopItem> items = shopItems.get(category);
        Inventory inv = plugin.getServer().createInventory(null, 54, "§8Shop §7» " + category.getDisplayName());

        int slot = 10;
        int itemIndex = 0;
        for (ShopItem shopItem : items) {
            ItemStack item = shopItem.createDisplayItem(player, team);

            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(meta.getDisplayName() + "§r§8§l[" + itemIndex + "]");
            item.setItemMeta(meta);

            inv.setItem(slot++, item);
            itemIndex++;

            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
            if (slot >= 35) break;
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§cZurück");
        back.setItemMeta(backMeta);
        inv.setItem(49, back);

        ItemStack filler = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte)7);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }

        player.openInventory(inv);
    }

    public void handlePurchase(Player player, Arena arena, ShopCategory category, int slot, boolean shiftClick) {
        BedWarsTeam team = getPlayerTeam(arena, player);
        if (team == null) return;

        List<ShopItem> items = shopItems.get(category);
        int itemIndex = convertSlotToIndex(slot);

        if (itemIndex < 0 || itemIndex >= items.size()) {
            return;
        }

        ShopItem item = items.get(itemIndex);

        if (shiftClick) {
            int affordable = calculateAffordableAmount(player, item);

            if (affordable == 0) {
                player.sendMessage("§cDu hast nicht genug " + item.currency.getDisplayName() + "!");
                player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_BREAK, 1.0f, 1.0f);
            } else {
                int toBuy = Math.min(affordable, 64);

                for (int i = 0; i < toBuy; i++) {
                    removeCurrency(player, item.currency, item.price);
                }

                ItemStack toGive = item.createItem(team);
                toGive.setAmount(toBuy * item.amount);

                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(toGive);
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItem(player.getLocation(), drop);
                }

                player.sendMessage("§aDu hast §e" + toBuy + "x " + item.name + " §agekauft!");
                player.playSound(player.getLocation(), org.bukkit.Sound.ORB_PICKUP, 1.0f, 1.0f);
            }
        } else {
            if (!hasEnoughCurrency(player, item.currency, item.price)) {
                player.sendMessage("§cDu hast nicht genug " + item.currency.getDisplayName() + "!");
                player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_BREAK, 1.0f, 1.0f);
            } else {
                removeCurrency(player, item.currency, item.price);

                ItemStack toGive = item.createItem(team);

                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItem(player.getLocation(), toGive);
                } else {
                    player.getInventory().addItem(toGive);
                }

                player.sendMessage("§aDu hast §e" + item.name + " §agekauft!");
                player.playSound(player.getLocation(), org.bukkit.Sound.ORB_PICKUP, 1.0f, 1.0f);
            }
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                openCategory(player, arena, category);
            }
        }, 1L);
    }

    private int calculateAffordableAmount(Player player, ShopItem item) {
        int totalCurrency = 0;

        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem != null && invItem.getType() == item.currency.getMaterial()) {
                totalCurrency += invItem.getAmount();
            }
        }

        return totalCurrency / item.price;
    }

    private boolean hasEnoughCurrency(Player player, Currency currency, int amount) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == currency.getMaterial()) {
                count += item.getAmount();
            }
        }
        return count >= amount;
    }

    private void removeCurrency(Player player, Currency currency, int amount) {
        int remaining = amount;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);

            if (item != null && item.getType() == currency.getMaterial()) {
                if (item.getAmount() > remaining) {
                    item.setAmount(item.getAmount() - remaining);
                    return;
                } else {
                    remaining -= item.getAmount();
                    player.getInventory().setItem(i, null);

                    if (remaining <= 0) {
                        return;
                    }
                }
            }
        }
    }

    private int convertSlotToIndex(int slot) {
        if (slot >= 10 && slot <= 16) {
            return slot - 10;
        } else if (slot >= 19 && slot <= 25) {
            return slot - 19 + 7;
        } else if (slot >= 28 && slot <= 34) {
            return slot - 28 + 14;
        }

        return -1;
    }

    public boolean buyItem(Player player, Arena arena, ShopItem item) {
        if (!hasEnoughCurrency(player, item.currency, item.price)) {
            player.sendMessage("§cDu hast nicht genug " + item.currency.getDisplayName() + "§c!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_BREAK, 1.0f, 1.0f);
            return false;
        }

        removeCurrency(player, item.currency, item.price);

        BedWarsTeam team = getPlayerTeam(arena, player);
        ItemStack toGive = item.createItem(team);

        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItem(player.getLocation(), toGive);
        } else {
            player.getInventory().addItem(toGive);
        }

        player.sendMessage("§aDu hast §e" + item.name + " §agekauft!");
        player.playSound(player.getLocation(), org.bukkit.Sound.ORB_PICKUP, 1.0f, 1.0f);
        return true;
    }

    private ItemStack createCategoryItem(ShopCategory category) {
        ItemStack item = new ItemStack(category.getIcon());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(category.getDisplayName());
        List<String> lore = new ArrayList<>();
        lore.add("§7Klicke zum Öffnen");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private BedWarsTeam getPlayerTeam(Arena arena, Player player) {
        for (BedWarsTeam team : arena.getTeams().values()) {
            if (team.getPlayers().contains(player.getUniqueId())) {
                return team;
            }
        }
        return null;
    }

    public enum ShopCategory {
        BLOCKS("§eBlöcke", Material.SANDSTONE),
        ARMOR("§aRüstung", Material.CHAINMAIL_CHESTPLATE),
        TOOLS("§bWerkzeuge", Material.STONE_PICKAXE),
        WEAPONS("§cWaffen", Material.GOLD_SWORD),
        POTIONS("§dTränke", Material.POTION),
        BOWS("§6Bögen", Material.BOW),
        STORAGE("§eLagerung", Material.CHEST),
        SPECIAL("§5Spezial", Material.TNT),
        FOOD("§2Essen", Material.APPLE);

        private final String displayName;
        private final Material icon;

        ShopCategory(String displayName, Material icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() { return displayName; }
        public Material getIcon() { return icon; }
    }

    public enum Currency {
        BRONZE("Bronze", Material.CLAY_BRICK),
        IRON("Eisen", Material.IRON_INGOT),
        GOLD("Gold", Material.GOLD_INGOT);

        private final String displayName;
        private final Material material;

        Currency(String displayName, Material material) {
            this.displayName = displayName;
            this.material = material;
        }

        public String getDisplayName() { return displayName; }
        public Material getMaterial() { return material; }
    }

    public static class ShopItem {
        private final String name;
        private final Material material;
        private final byte data;
        private final int amount;
        private final Currency currency;
        private final int price;
        private final String description;
        private final EnchantData[] enchantments;

        public ShopItem(String name, Material material, int amount, Currency currency, int price,
                        String priceDisplay, String description, EnchantData... enchantments) {
            this(name, material, (byte)0, amount, currency, price, priceDisplay, description, enchantments);
        }

        public ShopItem(String name, Material material, byte data, int amount, Currency currency, int price,
                        String priceDisplay, String description, EnchantData... enchantments) {
            this.name = name;
            this.material = material;
            this.data = data;
            this.amount = amount;
            this.currency = currency;
            this.price = price;
            this.description = description;
            this.enchantments = enchantments;
        }

        public ItemStack createItem(BedWarsTeam team) {
            ItemStack item = new ItemStack(material, amount, data);
            ItemMeta meta = item.getItemMeta();

            String cleanName = name.replaceAll(" §8x\\d+$", "");
            meta.setDisplayName(cleanName);

            if (enchantments != null) {
                for (EnchantData enchant : enchantments) {
                    meta.addEnchant(enchant.enchantment, enchant.level, true);
                }
            }

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

            if (team != null && meta instanceof LeatherArmorMeta) {
                ((LeatherArmorMeta) meta).setColor(team.getColor().getColor());
            }

            item.setItemMeta(meta);
            return item;
        }

        public ItemStack createDisplayItem(Player player, BedWarsTeam team) {
            ItemStack item = createItem(team);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(name);

            List<String> lore = new ArrayList<>();
            if (description != null) {
                lore.add(description);
                lore.add("");
            }
            lore.add("§7Preis: §e" + currency.getDisplayName() + " §8x§e" + price);
            lore.add("");

            boolean canAfford = hasEnoughCurrency(player);
            if (canAfford) {
                lore.add("§aKlicke zum Kaufen!");
            } else {
                lore.add("§cNicht genug " + currency.getDisplayName() + "!");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        }

        private boolean hasEnoughCurrency(Player player) {
            int count = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == currency.getMaterial()) {
                    count += item.getAmount();
                }
            }
            return count >= price;
        }
    }

    public static class EnchantData {
        private final Enchantment enchantment;
        private final int level;

        public EnchantData(Enchantment enchantment, int level) {
            this.enchantment = enchantment;
            this.level = level;
        }
    }
}