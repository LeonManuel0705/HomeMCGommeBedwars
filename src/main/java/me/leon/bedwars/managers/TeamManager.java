package me.leon.bedwars.managers;

import me.leon.bedwars.BedWars;
import me.leon.bedwars.game.Arena;
import me.leon.bedwars.npc.ShopNPC;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
        int teamCount = arena.getTeams().size();
        int rows = teamCount <= 4 ? 3 : 5;
        org.bukkit.inventory.Inventory inv = plugin.getServer().createInventory(null, rows * 9, "§8Team wählen");

        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        List<BedWarsTeam> teamList = new ArrayList<>(arena.getTeams().values());
        int[] slots;

        if (teamCount <= 4) {
            switch (teamCount) {
                case 2:
                    slots = new int[]{11, 15};
                    break;
                case 3:
                    slots = new int[]{10, 13, 16};
                    break;
                case 4:
                default:
                    slots = new int[]{10, 12, 14, 16};
                    break;
            }
        } else {
            switch (teamCount) {
                case 5:
                    slots = new int[]{11, 15, 19, 22, 25};
                    break;
                case 6:
                    slots = new int[]{10, 13, 16, 19, 22, 25};
                    break;
                case 7:
                    slots = new int[]{10, 13, 16, 19, 21, 23, 25};
                    break;
                case 8:
                default:
                    slots = new int[]{10, 12, 14, 16, 28, 30, 32, 34};
                    break;
            }
        }

        for (int i = 0; i < teamList.size() && i < slots.length; i++) {
            BedWarsTeam team = teamList.get(i);
            ItemStack item = new ItemStack(Material.WOOL, 1, team.getColor().getData());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(team.getColoredName());

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Spieler: §e" + team.getPlayers().size() + "§8/§e" + team.getMaxPlayers());
            lore.add("");

            for (UUID uuid : team.getPlayers()) {
                Player p = plugin.getServer().getPlayer(uuid);
                if (p != null) {
                    lore.add("§8- §7" + plugin.getDisplayName(p));
                }
            }

            if (!team.getPlayers().isEmpty()) {
                lore.add("");
            }

            if (team.getPlayers().size() >= team.getMaxPlayers()) {
                lore.add("§cTeam ist voll!");
            } else if (team.getPlayers().contains(player.getUniqueId())) {
                lore.add("§eDu bist in diesem Team!");
            } else {
                lore.add("§aKlicke zum Beitreten!");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);

            inv.setItem(slots[i], item);
        }

        player.openInventory(inv);
    }

    public ItemStack getGoldVoteItem() {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Gold Abstimmung §7(Rechtsklick)");
        List<String> lore = new ArrayList<>();
        lore.add("§7Stimme für Gold ab!");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public void openGoldVoteGUI(Player player, Arena arena) {
        org.bukkit.inventory.Inventory inv = plugin.getServer().createInventory(null, 27, "§8Gold Abstimmung");

        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        ItemStack goldOn = new ItemStack(Material.GOLD_INGOT);
        ItemMeta goldOnMeta = goldOn.getItemMeta();
        goldOnMeta.setDisplayName("§a§lGold aktivieren");
        List<String> onLore = new ArrayList<>();
        onLore.add("");
        onLore.add("§7Stimmen dafür: §a" + arena.getGoldVotesYes());
        onLore.add("");
        onLore.add("§eKlicke um dafür zu stimmen!");
        goldOnMeta.setLore(onLore);
        goldOn.setItemMeta(goldOnMeta);
        inv.setItem(11, goldOn);

        ItemStack goldOff = new ItemStack(Material.BARRIER);
        ItemMeta goldOffMeta = goldOff.getItemMeta();
        goldOffMeta.setDisplayName("§c§lGold deaktivieren");
        List<String> offLore = new ArrayList<>();
        offLore.add("");
        offLore.add("§7Stimmen dagegen: §c" + arena.getGoldVotesNo());
        offLore.add("");
        offLore.add("§eKlicke um dagegen zu stimmen!");
        goldOffMeta.setLore(offLore);
        goldOff.setItemMeta(goldOffMeta);
        inv.setItem(15, goldOff);

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
        private ShopNPC shopNPC;
        private boolean bedAlive;
        private boolean hasHadPlayers;

        private final Set<Location> teamGenerators;

        public BedWarsTeam(TeamColor color, int maxPlayers) {
            this.color = color;
            this.players = new HashSet<>();
            this.maxPlayers = maxPlayers;
            this.bedAlive = true;
            this.hasHadPlayers = false;
            this.teamGenerators = new HashSet<>();
        }

        public void addPlayer(UUID uuid) {
            players.add(uuid);
            hasHadPlayers = true;
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
            String destroyerColor;
            if (BedWars.getInstance().getCore().getNickManager().isNicked(destroyer.getUniqueId())) {
                destroyerName = BedWars.getInstance().getCore().getNickManager()
                        .getDisplayName(destroyer.getUniqueId());
                destroyerColor = "§7";
            } else {
                destroyerName = destroyer.getName();
                destroyerColor = BedWars.getInstance().getCore().getRankManager()
                        .getRankColor(destroyer);
            }

            broadcastToTeam("§f§lBETT ZERSTÖRT! §7Du kannst nicht mehr respawnen!");

            String bedDestroyMessage = BedWars.getInstance().getCosmeticsManager()
                    .formatBedDestroyMessage(getColoredName(), destroyerName, destroyerColor, destroyer.getUniqueId());

            for (UUID uuid : BedWars.getInstance().getGameManager().getArenaByPlayer(destroyer.getUniqueId())
                    .getPlayers()) {
                if (!players.contains(uuid)) {
                    Player p = BedWars.getInstance().getServer().getPlayer(uuid);
                    if (p != null) {
                        p.sendMessage(bedDestroyMessage);
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
            hasHadPlayers = false;
            shopNPC = null;
        }

        public boolean hasHadPlayers() {
            return hasHadPlayers;
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
        public ShopNPC getShopNPC() { return shopNPC; }
        public void setShopNPC(ShopNPC shopNPC) { this.shopNPC = shopNPC; }
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