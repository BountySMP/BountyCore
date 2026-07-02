package com.bountysmp.bountyCore.profile;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.booster.SellBooster;
import com.bountysmp.bountyCore.stats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ProfileGUI implements InventoryHolder {

    // Row 1: head at 4
    // Row 2: balance=11, rank=13, playtime=15
    // Row 3: homes=19, combat=21, booster=23, bounty=25
    // Row 4: close=31
    private static final int SLOT_HEAD     = 4;
    private static final int SLOT_BALANCE  = 11;
    private static final int SLOT_RANK     = 13;
    private static final int SLOT_PLAYTIME = 15;
    private static final int SLOT_HOMES    = 19;
    private static final int SLOT_COMBAT   = 21;
    private static final int SLOT_BOOSTER  = 23;
    private static final int SLOT_BOUNTY   = 25;
    private static final int SLOT_CLOSE    = 31;

    private static final DecimalFormat MONEY_FMT = new DecimalFormat("#,###.##");
    private static final DecimalFormat KD_FMT    = new DecimalFormat("0.00");

    private final BountyCore plugin;
    private final OfflinePlayer target;
    private final Player viewer;
    private Inventory inventory;

    public ProfileGUI(BountyCore plugin, OfflinePlayer target, Player viewer) {
        this.plugin  = plugin;
        this.target  = target;
        this.viewer  = viewer;
    }

    @Override
    public Inventory getInventory() { return inventory; }

    public void open() {
        inventory = Bukkit.createInventory(this, 36, "§8§lProfile §7» §e" + target.getName());

        // Head
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwningPlayer(target);
        headMeta.setDisplayName("§e§l" + target.getName());
        headMeta.setLore(List.of(
            "",
            "§7Status: " + (target.isOnline() ? "§aOnline" : "§cOffline")
        ));
        head.setItemMeta(headMeta);
        inventory.setItem(SLOT_HEAD, head);

        // Balance
        double balance = plugin.getEconomy().getBalance(target);
        inventory.setItem(SLOT_BALANCE, make(Material.GOLD_INGOT, "§6§lBalance",
            "§f$" + MONEY_FMT.format(balance)));

        // Rank
        var rankObj = plugin.getRankManager().getRank(target.getUniqueId());
        String rankName = rankObj != null ? rankObj.getDisplayName() : "§7None";
        inventory.setItem(SLOT_RANK, make(Material.NAME_TAG, "§b§lRank", rankName));

        // Playtime
        PlayerStats stats = plugin.getPlayerStatsManager().getStats(target.getUniqueId());
        inventory.setItem(SLOT_PLAYTIME, make(Material.CLOCK, "§a§lPlaytime",
            "§f" + (stats != null ? stats.getFormattedPlaytime() : "0m")));

        // Homes
        int homes = plugin.getHomeManager().getHomes(target.getUniqueId()).size();
        inventory.setItem(SLOT_HOMES, make(Material.RED_BED, "§d§lHomes",
            "§f" + homes + " §7homes set"));

        // Combat stats
        if (stats != null) {
            inventory.setItem(SLOT_COMBAT, make(Material.DIAMOND_SWORD, "§c§lCombat Stats",
                "§7Kills   §f" + stats.getKills(),
                "§7Deaths  §f" + stats.getDeaths(),
                "§7K/D     §f" + KD_FMT.format(stats.getKDRatio())));
        }

        // Sell booster
        SellBooster booster = plugin.getSellBoosterManager().getBooster(target.getUniqueId());
        if (booster != null) {
            inventory.setItem(SLOT_BOOSTER, make(Material.NETHER_STAR, "§e§lSell Booster",
                "§7Multiplier §f" + booster.getMultiplier() + "x",
                "§7Time Left  §f" + booster.getFormattedTimeRemaining()));
        } else {
            inventory.setItem(SLOT_BOOSTER, make(Material.GRAY_DYE, "§7§lSell Booster",
                "§7No active booster"));
        }

        // Bounty
        double bounty = plugin.getBountyManager().getBounty(target.getUniqueId());
        if (bounty > 0) {
            inventory.setItem(SLOT_BOUNTY, make(Material.REDSTONE, "§c§lBounty",
                "§f$" + MONEY_FMT.format(bounty)));
        } else {
            inventory.setItem(SLOT_BOUNTY, make(Material.GRAY_DYE, "§7§lBounty",
                "§7No active bounty"));
        }

        // Close
        inventory.setItem(SLOT_CLOSE, make(Material.BARRIER, "§c§lClose", "§7Click to close"));

        viewer.openInventory(inventory);
    }

    public void handleClick(int slot, Player player) {
        if (slot == SLOT_CLOSE) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            player.closeInventory();
        }
    }

    private ItemStack make(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> loreList = new ArrayList<>();
        loreList.add("");
        for (String line : lore) loreList.add(line);
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }
}
