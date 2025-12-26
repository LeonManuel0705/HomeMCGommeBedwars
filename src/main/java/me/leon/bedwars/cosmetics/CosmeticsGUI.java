package me.leon.bedwars.cosmetics;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.cosmetics.CosmeticsManager.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class CosmeticsGUI implements Listener {

    private final BedWars plugin;
    private static final String MAIN_TITLE = "§8Kosmetik";
    private static final String KILL_TITLE = "§8Kill-Nachrichten";
    private static final String DEATH_TITLE = "§8Todes-Nachrichten";
    private static final String SOUND_TITLE = "§8Bett-Sounds";
    private static final String BEDMSG_TITLE = "§8Bett-Nachrichten";
    private static final String DANCE_TITLE = "§8Sieges-Tänze";

    public CosmeticsGUI(BedWars plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, MAIN_TITLE);

        PlayerCosmetics cosmetics = plugin.getCosmeticsManager().getCosmetics(player.getUniqueId());
        int coins = plugin.getCore().getCoinManager().getCoins(player.getUniqueId());

        ItemStack coinItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta coinMeta = coinItem.getItemMeta();
        coinMeta.setDisplayName("§6§lDeine Coins");
        coinMeta.setLore(Arrays.asList(
                "§7Du hast: §e" + coins + " Coins",
                "§f",
                "§7Kaufe Kosmetiken mit Coins!"
        ));
        coinItem.setItemMeta(coinMeta);
        inv.setItem(4, coinItem);

        ItemStack killItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta killMeta = killItem.getItemMeta();
        killMeta.setDisplayName("§c§lKill-Nachrichten");
        killMeta.setLore(Arrays.asList(
                "§7Wähle deine Kill-Nachricht!",
                "§f",
                "§7Aktuell: " + cosmetics.getKillMessage().getDisplayName(),
                "§f",
                "§eKlicken zum Öffnen"
        ));
        killItem.setItemMeta(killMeta);
        inv.setItem(10, killItem);

        ItemStack deathItem = new ItemStack(Material.SKULL_ITEM);
        ItemMeta deathMeta = deathItem.getItemMeta();
        deathMeta.setDisplayName("§6§lTodes-Nachrichten");
        deathMeta.setLore(Arrays.asList(
                "§7Wähle deine Todes-Nachricht!",
                "§f",
                "§7Aktuell: " + cosmetics.getDeathMessage().getDisplayName(),
                "§f",
                "§eKlicken zum Öffnen"
        ));
        deathItem.setItemMeta(deathMeta);
        inv.setItem(12, deathItem);

        ItemStack soundItem = new ItemStack(Material.NOTE_BLOCK);
        ItemMeta soundMeta = soundItem.getItemMeta();
        soundMeta.setDisplayName("§b§lBett-Sounds");
        soundMeta.setLore(Arrays.asList(
                "§7Wähle deinen Bett-Sound!",
                "§f",
                "§7Aktuell: " + cosmetics.getBedDestroySound().getDisplayName(),
                "§f",
                "§eKlicken zum Öffnen"
        ));
        soundItem.setItemMeta(soundMeta);
        inv.setItem(14, soundItem);

        ItemStack bedMsgItem = new ItemStack(Material.BED);
        ItemMeta bedMsgMeta = bedMsgItem.getItemMeta();
        bedMsgMeta.setDisplayName("§e§lBett-Nachrichten");
        bedMsgMeta.setLore(Arrays.asList(
                "§7Wähle deine Bett-Nachricht!",
                "§f",
                "§7Aktuell: " + cosmetics.getBedDestroyMessage().getDisplayName(),
                "§f",
                "§eKlicken zum Öffnen"
        ));
        bedMsgItem.setItemMeta(bedMsgMeta);
        inv.setItem(16, bedMsgItem);

        ItemStack danceItem = new ItemStack(Material.FIREWORK);
        ItemMeta danceMeta = danceItem.getItemMeta();
        danceMeta.setDisplayName("§d§lSieges-Tänze");
        danceMeta.setLore(Arrays.asList(
                "§7Wähle deinen Sieges-Tanz!",
                "§f",
                "§7Aktuell: " + cosmetics.getVictoryDance().getDisplayName(),
                "§f",
                "§eKlicken zum Öffnen"
        ));
        danceItem.setItemMeta(danceMeta);
        inv.setItem(22, danceItem);

        ItemStack filler = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }

        player.openInventory(inv);
    }

    public void openKillMessages(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, KILL_TITLE);
        PlayerCosmetics cosmetics = plugin.getCosmeticsManager().getCosmetics(player.getUniqueId());
        KillMessage current = cosmetics.getKillMessage();

        int slot = 10;
        for (KillMessage msg : KillMessage.values()) {
            String cosmeticId = "KILL_" + msg.name();
            boolean owned = cosmetics.ownsCosmetic(cosmeticId);
            boolean selected = current == msg;

            ItemStack item = new ItemStack(selected ? Material.ENCHANTED_BOOK : (owned ? Material.BOOK : Material.PAPER));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(msg.getDisplayName());

            List<String> lore = new ArrayList<>();
            lore.add("§7Vorschau:");
            lore.add(msg.format("Spieler", "§c", "Opfer", "§a"));
            lore.add("§f");

            if (selected) {
                lore.add("§a✓ Ausgewählt");
            } else if (owned) {
                lore.add("§eKlicken zum Auswählen");
            } else {
                lore.add("§c✗ Nicht freigeschaltet");
                lore.add("§7Preis: §e" + msg.getPrice() + " Coins");
                lore.add("§aRechtsklick zum Kaufen");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot, item);

            slot++;
            if (slot == 17) slot = 19;
            if (slot == 26) break;
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§c§lZurück");
        back.setItemMeta(backMeta);
        inv.setItem(31, back);

        fillEmpty(inv);
        player.openInventory(inv);
    }

    public void openDeathMessages(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, DEATH_TITLE);
        PlayerCosmetics cosmetics = plugin.getCosmeticsManager().getCosmetics(player.getUniqueId());
        DeathMessage current = cosmetics.getDeathMessage();

        int slot = 10;
        for (DeathMessage msg : DeathMessage.values()) {
            String cosmeticId = "DEATH_" + msg.name();
            boolean owned = cosmetics.ownsCosmetic(cosmeticId);
            boolean selected = current == msg;

            ItemStack item = new ItemStack(selected ? Material.ENCHANTED_BOOK : (owned ? Material.BOOK : Material.PAPER));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(msg.getDisplayName());

            List<String> lore = new ArrayList<>();
            lore.add("§7Vorschau:");
            lore.add(msg.format("Spieler", "§c"));
            lore.add("§f");

            if (selected) {
                lore.add("§a✓ Ausgewählt");
            } else if (owned) {
                lore.add("§eKlicken zum Auswählen");
            } else {
                lore.add("§c✗ Nicht freigeschaltet");
                lore.add("§7Preis: §e" + msg.getPrice() + " Coins");
                lore.add("§aRechtsklick zum Kaufen");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot, item);

            slot++;
            if (slot == 17) slot = 19;
            if (slot == 26) break;
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§c§lZurück");
        back.setItemMeta(backMeta);
        inv.setItem(31, back);

        fillEmpty(inv);
        player.openInventory(inv);
    }

    public void openBedSounds(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, SOUND_TITLE);
        PlayerCosmetics cosmetics = plugin.getCosmeticsManager().getCosmetics(player.getUniqueId());
        BedDestroySound current = cosmetics.getBedDestroySound();

        int slot = 10;
        for (BedDestroySound sound : BedDestroySound.values()) {
            String cosmeticId = "SOUND_" + sound.name();
            boolean owned = cosmetics.ownsCosmetic(cosmeticId);
            boolean selected = current == sound;

            ItemStack item = new ItemStack(selected ? Material.RECORD_4 : (owned ? Material.NOTE_BLOCK : Material.COAL));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(sound.getDisplayName());

            List<String> lore = new ArrayList<>();
            lore.add("§7Sound beim Bett zerstören");
            lore.add("§f");

            if (selected) {
                lore.add("§a✓ Ausgewählt");
            } else if (owned) {
                lore.add("§eKlicken zum Auswählen");
                lore.add("§7Rechtsklick für Vorschau");
            } else {
                lore.add("§c✗ Nicht freigeschaltet");
                lore.add("§7Preis: §e" + sound.getPrice() + " Coins");
                lore.add("§aRechtsklick zum Kaufen");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot, item);

            slot++;
            if (slot == 17) slot = 19;
            if (slot == 26) break;
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§c§lZurück");
        back.setItemMeta(backMeta);
        inv.setItem(31, back);

        fillEmpty(inv);
        player.openInventory(inv);
    }

    public void openBedMessages(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, BEDMSG_TITLE);
        PlayerCosmetics cosmetics = plugin.getCosmeticsManager().getCosmetics(player.getUniqueId());
        BedDestroyMessage current = cosmetics.getBedDestroyMessage();

        int slot = 10;
        for (BedDestroyMessage msg : BedDestroyMessage.values()) {
            String cosmeticId = "BEDMSG_" + msg.name();
            boolean owned = cosmetics.ownsCosmetic(cosmeticId);
            boolean selected = current == msg;

            ItemStack item = new ItemStack(selected ? Material.ENCHANTED_BOOK : (owned ? Material.BED : Material.PAPER));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(msg.getDisplayName());

            List<String> lore = new ArrayList<>();
            lore.add("§7Vorschau:");
            lore.add(msg.format("§cRot", "Spieler", "§a"));
            lore.add("§f");

            if (selected) {
                lore.add("§a✓ Ausgewählt");
            } else if (owned) {
                lore.add("§eKlicken zum Auswählen");
            } else {
                lore.add("§c✗ Nicht freigeschaltet");
                lore.add("§7Preis: §e" + msg.getPrice() + " Coins");
                lore.add("§aRechtsklick zum Kaufen");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot, item);

            slot++;
            if (slot == 17) slot = 19;
            if (slot == 26) break;
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§c§lZurück");
        back.setItemMeta(backMeta);
        inv.setItem(31, back);

        fillEmpty(inv);
        player.openInventory(inv);
    }

    public void openVictoryDances(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, DANCE_TITLE);
        PlayerCosmetics cosmetics = plugin.getCosmeticsManager().getCosmetics(player.getUniqueId());
        VictoryDance current = cosmetics.getVictoryDance();

        int slot = 11;
        for (VictoryDance dance : VictoryDance.values()) {
            String cosmeticId = "DANCE_" + dance.name();
            boolean owned = cosmetics.ownsCosmetic(cosmeticId);
            boolean selected = current == dance;

            Material mat;
            switch (dance) {
                case FIREWORKS: mat = Material.FIREWORK; break;
                case LIGHTNING: mat = Material.BLAZE_ROD; break;
                case PARTICLES: mat = Material.NETHER_STAR; break;
                default: mat = Material.BARRIER; break;
            }

            ItemStack item = new ItemStack(selected ? Material.ENCHANTED_BOOK : (owned ? mat : Material.COAL));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(dance.getDisplayName());

            List<String> lore = new ArrayList<>();
            lore.add("§7Animation nach einem Sieg");
            lore.add("§f");

            if (selected) {
                lore.add("§a✓ Ausgewählt");
            } else if (owned) {
                lore.add("§eKlicken zum Auswählen");
            } else {
                lore.add("§c✗ Nicht freigeschaltet");
                lore.add("§7Preis: §e" + dance.getPrice() + " Coins");
                lore.add("§aRechtsklick zum Kaufen");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot, item);

            slot++;
            if (slot == 16) slot = 20;
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§c§lZurück");
        back.setItemMeta(backMeta);
        inv.setItem(31, back);

        fillEmpty(inv);
        player.openInventory(inv);
    }

    private void fillEmpty(Inventory inv) {
        ItemStack filler = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getInventory().getTitle();

        if (!title.equals(MAIN_TITLE) && !title.equals(KILL_TITLE) &&
            !title.equals(DEATH_TITLE) && !title.equals(SOUND_TITLE) &&
            !title.equals(BEDMSG_TITLE) && !title.equals(DANCE_TITLE)) {
            return;
        }

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR ||
            clicked.getType() == Material.STAINED_GLASS_PANE) {
            return;
        }

        PlayerCosmetics cosmetics = plugin.getCosmeticsManager().getCosmetics(player.getUniqueId());

        if (title.equals(MAIN_TITLE)) {
            handleMainMenu(player, event.getSlot());
        } else if (title.equals(KILL_TITLE)) {
            handleKillMessages(player, event.getSlot(), cosmetics, event.isRightClick());
        } else if (title.equals(DEATH_TITLE)) {
            handleDeathMessages(player, event.getSlot(), cosmetics, event.isRightClick());
        } else if (title.equals(SOUND_TITLE)) {
            handleBedSounds(player, event.getSlot(), cosmetics, event.isRightClick());
        } else if (title.equals(BEDMSG_TITLE)) {
            handleBedMessages(player, event.getSlot(), cosmetics, event.isRightClick());
        } else if (title.equals(DANCE_TITLE)) {
            handleVictoryDances(player, event.getSlot(), cosmetics, event.isRightClick());
        }
    }

    private void handleMainMenu(Player player, int slot) {
        switch (slot) {
            case 10:
                openKillMessages(player);
                break;
            case 12:
                openDeathMessages(player);
                break;
            case 14:
                openBedSounds(player);
                break;
            case 16:
                openBedMessages(player);
                break;
            case 22:
                openVictoryDances(player);
                break;
        }
    }

    private void handleKillMessages(Player player, int slot, PlayerCosmetics cosmetics, boolean purchase) {
        if (slot == 31) {
            openMainMenu(player);
            return;
        }

        int index = slotToIndex(slot);
        if (index < 0 || index >= KillMessage.values().length) return;

        KillMessage msg = KillMessage.values()[index];
        String cosmeticId = "KILL_" + msg.name();

        if (!cosmetics.ownsCosmetic(cosmeticId)) {
            if (purchase) {
                if (plugin.getCosmeticsManager().purchaseCosmetic(player, cosmeticId, msg.getPrice())) {
                    openKillMessages(player);
                }
            } else {
                player.sendMessage(BedWars.PREFIX + "§cDu besitzt diese Kosmetik nicht! §7Rechtsklick zum Kaufen.");
            }
            return;
        }

        cosmetics.setKillMessage(msg);
        plugin.getCosmeticsManager().savePlayerCosmetics(player.getUniqueId());
        player.sendMessage(BedWars.PREFIX + "§aKill-Nachricht geändert zu: " + msg.getDisplayName());
        openKillMessages(player);
    }

    private void handleDeathMessages(Player player, int slot, PlayerCosmetics cosmetics, boolean purchase) {
        if (slot == 31) {
            openMainMenu(player);
            return;
        }

        int index = slotToIndex(slot);
        if (index < 0 || index >= DeathMessage.values().length) return;

        DeathMessage msg = DeathMessage.values()[index];
        String cosmeticId = "DEATH_" + msg.name();

        if (!cosmetics.ownsCosmetic(cosmeticId)) {
            if (purchase) {
                if (plugin.getCosmeticsManager().purchaseCosmetic(player, cosmeticId, msg.getPrice())) {
                    openDeathMessages(player);
                }
            } else {
                player.sendMessage(BedWars.PREFIX + "§cDu besitzt diese Kosmetik nicht! §7Rechtsklick zum Kaufen.");
            }
            return;
        }

        cosmetics.setDeathMessage(msg);
        plugin.getCosmeticsManager().savePlayerCosmetics(player.getUniqueId());
        player.sendMessage(BedWars.PREFIX + "§aTodes-Nachricht geändert zu: " + msg.getDisplayName());
        openDeathMessages(player);
    }

    private void handleBedSounds(Player player, int slot, PlayerCosmetics cosmetics, boolean rightClick) {
        if (slot == 31) {
            openMainMenu(player);
            return;
        }

        int index = slotToIndex(slot);
        if (index < 0 || index >= BedDestroySound.values().length) return;

        BedDestroySound sound = BedDestroySound.values()[index];
        String cosmeticId = "SOUND_" + sound.name();

        if (!cosmetics.ownsCosmetic(cosmeticId)) {
            if (rightClick) {
                if (plugin.getCosmeticsManager().purchaseCosmetic(player, cosmeticId, sound.getPrice())) {
                    openBedSounds(player);
                }
            } else {
                player.sendMessage(BedWars.PREFIX + "§cDu besitzt diese Kosmetik nicht! §7Rechtsklick zum Kaufen.");
            }
            return;
        }

        if (rightClick) {
            sound.play(player.getLocation());
            player.sendMessage(BedWars.PREFIX + "§7Vorschau: " + sound.getDisplayName());
        } else {
            cosmetics.setBedDestroySound(sound);
            plugin.getCosmeticsManager().savePlayerCosmetics(player.getUniqueId());
            player.sendMessage(BedWars.PREFIX + "§aBett-Sound geändert zu: " + sound.getDisplayName());
            openBedSounds(player);
        }
    }

    private void handleBedMessages(Player player, int slot, PlayerCosmetics cosmetics, boolean purchase) {
        if (slot == 31) {
            openMainMenu(player);
            return;
        }

        int index = slotToIndex(slot);
        if (index < 0 || index >= BedDestroyMessage.values().length) return;

        BedDestroyMessage msg = BedDestroyMessage.values()[index];
        String cosmeticId = "BEDMSG_" + msg.name();

        if (!cosmetics.ownsCosmetic(cosmeticId)) {
            if (purchase) {
                if (plugin.getCosmeticsManager().purchaseCosmetic(player, cosmeticId, msg.getPrice())) {
                    openBedMessages(player);
                }
            } else {
                player.sendMessage(BedWars.PREFIX + "§cDu besitzt diese Kosmetik nicht! §7Rechtsklick zum Kaufen.");
            }
            return;
        }

        cosmetics.setBedDestroyMessage(msg);
        plugin.getCosmeticsManager().savePlayerCosmetics(player.getUniqueId());
        player.sendMessage(BedWars.PREFIX + "§aBett-Nachricht geändert zu: " + msg.getDisplayName());
        openBedMessages(player);
    }

    private void handleVictoryDances(Player player, int slot, PlayerCosmetics cosmetics, boolean purchase) {
        if (slot == 31) {
            openMainMenu(player);
            return;
        }

        int index = slotToVictoryIndex(slot);
        if (index < 0 || index >= VictoryDance.values().length) return;

        VictoryDance dance = VictoryDance.values()[index];
        String cosmeticId = "DANCE_" + dance.name();

        if (!cosmetics.ownsCosmetic(cosmeticId)) {
            if (purchase) {
                if (plugin.getCosmeticsManager().purchaseCosmetic(player, cosmeticId, dance.getPrice())) {
                    openVictoryDances(player);
                }
            } else {
                player.sendMessage(BedWars.PREFIX + "§cDu besitzt diese Kosmetik nicht! §7Rechtsklick zum Kaufen.");
            }
            return;
        }

        cosmetics.setVictoryDance(dance);
        plugin.getCosmeticsManager().savePlayerCosmetics(player.getUniqueId());
        player.sendMessage(BedWars.PREFIX + "§aSieges-Tanz geändert zu: " + dance.getDisplayName());
        openVictoryDances(player);
    }

    private int slotToIndex(int slot) {
        if (slot >= 10 && slot <= 16) return slot - 10;
        if (slot >= 19 && slot <= 25) return slot - 12;
        return -1;
    }

    private int slotToVictoryIndex(int slot) {
        if (slot >= 11 && slot <= 15) return slot - 11;
        if (slot >= 20 && slot <= 24) return slot - 16;
        return -1;
    }
}
