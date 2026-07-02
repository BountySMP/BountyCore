package com.bountysmp.bountyCore.statswipe;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class StatsWipeGUI {
    private final BountyCore plugin;
    private final Player viewer;

    public StatsWipeGUI(BountyCore plugin) {
        this.plugin = plugin;
        this.viewer = null;
    }

    public StatsWipeGUI(BountyCore plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
    }

    public void open() {
        if (viewer != null) open(viewer);
    }

    /**
     * 27-slot layout:
     * Row 0: [BG][Eco:1][Stats:2][Homes:3][EC:4][Bounty:5][AH:6][Teams:7][BG]
     * Row 1: [BG][Inventories:10][HH:11][BG][BG][BG][WipeAll:15][BG][BG]
     * Row 2: [BG][BG][BG][BG][Close:22][BG][BG][BG][BG]
     */
    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§8§lStats Wipe");

        ItemStack bg = borderItem();
        for (int i = 0; i < 27; i++) gui.setItem(i, bg);

        // Row 0 — 7 data categories
        gui.setItem(1,  btn(Material.GOLD_INGOT,        "§e§lEconomy",       "§7Wipe all player balances."));
        gui.setItem(2,  btn(Material.DIAMOND_SWORD,     "§b§lStats",          "§7Wipe all kills, deaths & playtime."));
        gui.setItem(3,  btn(Material.RED_BED,           "§c§lHomes",          "§7Delete all player homes."));
        gui.setItem(4,  btn(Material.ENDER_CHEST,       "§5§lEnder Chests",   "§7Clear all ender chest contents."));
        gui.setItem(5,  btn(Material.ARROW,             "§6§lBounties",       "§7Remove all active bounties."));
        gui.setItem(6,  btn(Material.GOLD_BLOCK,        "§6§lAuction House",  "§7Delete all AH listings."));
        gui.setItem(7,  btn(Material.SHIELD,            "§a§lTeams",          "§7Disband all teams."));

        // Row 1 — 2 more + wipe all
        gui.setItem(10, btn(Material.CHEST,             "§6§lInventories",    "§7Clear all player inventories.", "§7Offline players are cleared on join."));
        gui.setItem(11, btn(Material.EXPERIENCE_BOTTLE, "§2§lHH Levels/XP",   "§7Reset all HeadHunter levels & XP."));
        gui.setItem(15, btn(Material.BARRIER,           "§c§l⚠ Wipe ALL",     "§7Wipes every category above.", "§4§lTHIS CANNOT BE UNDONE!"));

        // Close button
        ItemStack close = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName("§c§lClose");
        close.setItemMeta(closeMeta);
        gui.setItem(22, close);

        player.openInventory(gui);
    }

    public void openConfirm(String wipeType) {
        if (viewer != null) openConfirm(viewer, wipeType);
    }

    /**
     * 27-slot confirm layout:
     * Row 0: all BG
     * Row 1: [BG][BG][CONFIRM:11][BG][Info:13][BG][CANCEL:15][BG][BG]
     * Row 2: all BG
     */
    public void openConfirm(Player player, String wipeType) {
        Inventory gui = Bukkit.createInventory(null, 27, "§8§lConfirm Wipe - " + wipeType);

        ItemStack bg = borderItem();
        for (int i = 0; i < 27; i++) gui.setItem(i, bg);

        ItemStack confirm = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName("§a§lCONFIRM");
        confirmMeta.setLore(List.of("§7Click to wipe §e" + wipeType + "§7.", "§c§lThis cannot be undone!"));
        confirm.setItemMeta(confirmMeta);

        ItemStack cancel = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName("§c§lCANCEL");
        cancelMeta.setLore(List.of("§7Click to go back."));
        cancel.setItemMeta(cancelMeta);

        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§e§lWiping: §f" + wipeType);
        infoMeta.setLore(List.of("§7Are you sure?"));
        info.setItemMeta(infoMeta);

        gui.setItem(11, confirm);
        gui.setItem(13, info);
        gui.setItem(15, cancel);

        player.openInventory(gui);
    }

    private ItemStack borderItem() {
        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(" ");
        pane.setItemMeta(meta);
        return pane;
    }

    private ItemStack btn(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of(lore));
        item.setItemMeta(meta);
        return item;
    }
}
