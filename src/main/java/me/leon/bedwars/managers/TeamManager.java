package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.game.Arena.GeneratorData;
import me.leon.bedwars.game.Arena.GeneratorType;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class TeamManager {

    private final BedWars plugin;

    public TeamManager(BedWars plugin) {
        this.plugin = plugin;
    }

    public ItemStack getTeamSelectorItem() {
        ItemStack item = new ItemStack(Material.BED);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eTeam wählen §7(Rechtsklick)");
        List<String> lore = new ArrayList<>();
        lore.add("§7Wähle dein Team aus!");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public void openTeamSelector(Player player, Arena arena) {
        org.bukkit.inventory.Inventory inv = plugin.getServer().createInventory(null, 45, "§8Team wählen");

        int slot = 10;
        for (BedWarsTeam team : arena.getTeams().values()) {
            ItemStack item = new ItemStack(Material.WOOL, 1, team.getColor().getData());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(team.getColoredName());

            List<String> lore = new ArrayList<>();
            lore.add("§7Spieler: §e" + team.getPlayers().size() + "§8/§e" + team.getMaxPlayers());
            lore.add("");
            if (team.getPlayers().size() >= team.getMaxPlayers()) {
                lore.add("§cTeam ist voll!");
            } else {
                lore.add("§aKlicke zum Beitreten!");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);

            inv.setItem(slot++, item);
            if (slot == 14) slot = 16;
        }

        ItemStack goldOn = new ItemStack(Material.GOLD_INGOT);
        ItemMeta goldOnMeta = goldOn.getItemMeta();
        goldOnMeta.setDisplayName("§aGold aktivieren");
        goldOnMeta.setLore(Arrays.asList("§7Klicke um für Gold zu stimmen"));
        goldOn.setItemMeta(goldOnMeta);
        inv.setItem(37, goldOn);

        ItemStack goldOff = new ItemStack(Material.BARRIER);
        ItemMeta goldOffMeta = goldOff.getItemMeta();
        goldOffMeta.setDisplayName("§cGold deaktivieren");
        goldOffMeta.setLore(Arrays.asList("§7Klicke um gegen Gold zu stimmen"));
        goldOff.setItemMeta(goldOffMeta);
        inv.setItem(39, goldOff);

        player.openInventory(inv);
    }

    public void joinTeam(Player player, Arena arena, TeamColor color) {
        for (BedWarsTeam team : arena.getTeams().values()) {
            team.removePlayer(player.getUniqueId());
        }

        BedWarsTeam newTeam = arena.getTeams().get(color.name());
        if (newTeam == null || newTeam.getPlayers().size() >= newTeam.getMaxPlayers()) {
            player.sendMessage("§cDieses Team ist bereits voll!");
            return;
        }

        newTeam.addPlayer(player.getUniqueId());
        player.sendMessage("§aDu bist jetzt in Team " + newTeam.getColoredName());

        plugin.getNametagManager().setGameNametag(player, newTeam);
    }

    private BedWarsTeam getPlayerTeamInArena(Arena arena, Player player) {
        for (BedWarsTeam team : arena.getTeams().values()) {
            if (team.getPlayers().contains(player.getUniqueId())) {
                return team;
            }
        }
        return null;
    }

    public static class BedWarsTeam {
        private final TeamColor color;
        private final Set<UUID> players;
        private final int maxPlayers;

        private Location spawn;
        private Location bedLocation;
        private Location shopVillager;
        private Villager shopVillagerEntity;
        private boolean bedAlive;

        private final Set<Location> teamGenerators;

        public BedWarsTeam(TeamColor color, int maxPlayers) {
            this.color = color;
            this.players = new HashSet<>();
            this.maxPlayers = maxPlayers;
            this.bedAlive = true;
            this.teamGenerators = new HashSet<>();
        }

        public void addPlayer(UUID uuid) {
            players.add(uuid);
        }

        public void removePlayer(UUID uuid) {
            players.remove(uuid);
        }

        public boolean isAlive() {
            if (bedAlive) return true;

            for (UUID uuid : players) {
                Player p = BedWars.getInstance().getServer().getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    return true;
                }
            }
            return false;
        }

        public void destroyBed(Player destroyer) {
            bedAlive = false;

            String destroyerName;
            if (BedWars.getInstance().getCore().getNickManager().isNicked(destroyer.getUniqueId())) {
                destroyerName = "§7" + BedWars.getInstance().getCore().getNickManager()
                        .getDisplayName(destroyer.getUniqueId());
            } else {
                destroyerName = BedWars.getInstance().getCore().getRankManager()
                        .getRankColor(destroyer) + destroyer.getName();
            }

            broadcastToTeam("§f§lBETT ZERSTÖRT! §7Du kannst nicht mehr respawnen!");

            for (UUID uuid : BedWars.getInstance().getGameManager().getArenaByPlayer(destroyer.getUniqueId())
                    .getPlayers()) {
                if (!players.contains(uuid)) {
                    Player p = BedWars.getInstance().getServer().getPlayer(uuid);
                    if (p != null) {
                        p.sendMessage("§f§lBETT ZERSTÖRT > " + getColoredName() + " §7von " +
                                destroyerName);
                    }
                }
            }
        }

        public void broadcastToTeam(String message) {
            for (UUID uuid : players) {
                Player p = BedWars.getInstance().getServer().getPlayer(uuid);
                if (p != null) {
                    p.sendMessage(message);
                }
            }
        }

        public void reset() {
            players.clear();
            bedAlive = true;
            shopVillagerEntity = null;
        }

        public String getColoredName() {
            return color.getColorCode() + color.getDisplayName();
        }

        public TeamColor getColor() { return color; }
        public Set<UUID> getPlayers() { return players; }
        public int getMaxPlayers() { return maxPlayers; }
        public Location getSpawn() { return spawn; }
        public void setSpawn(Location spawn) { this.spawn = spawn; }
        public Location getBedLocation() { return bedLocation; }
        public void setBedLocation(Location bedLocation) { this.bedLocation = bedLocation; }
        public Location getShopVillager() { return shopVillager; }
        public void setShopVillager(Location shopVillager) { this.shopVillager = shopVillager; }
        public Villager getShopVillagerEntity() { return shopVillagerEntity; }
        public void setShopVillagerEntity(Villager shopVillagerEntity) { this.shopVillagerEntity = shopVillagerEntity; }
        public boolean isBedAlive() { return bedAlive; }
        public Set<Location> getTeamGenerators() { return teamGenerators; }
    }

    public enum TeamColor {
        RED("§c", "Rot", (short)14, Color.RED),
        BLUE("§9", "Blau", (short)11, Color.BLUE),
        GREEN("§a", "Grün", (short)13, Color.GREEN),
        YELLOW("§e", "Gelb", (short)4, Color.YELLOW),
        AQUA("§b", "Cyan", (short)9, Color.AQUA),
        PINK("§d", "Pink", (short)6, Color.FUCHSIA),
        GRAY("§7", "Grau", (short)7, Color.GRAY),
        WHITE("§f", "Weiß", (short)0, Color.WHITE);

        private final String colorCode;
        private final String displayName;
        private final short data;
        private final Color color;

        TeamColor(String colorCode, String displayName, short data, Color color) {
            this.colorCode = colorCode;
            this.displayName = displayName;
            this.data = data;
            this.color = color;
        }

        public String getColorCode() { return colorCode; }
        public String getDisplayName() { return displayName; }
        public short getData() { return data; }
        public Color getColor() { return color; }

        public String getColoredName() {
            return colorCode + displayName;
        }
    }
}