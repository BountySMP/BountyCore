package com.bountysmp.bountyCore.profile;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.booster.SellBooster;
import com.bountysmp.bountyCore.stats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ProfileGUI {
    private final BountyCore plugin;
    private final OfflinePlayer target;

    public ProfileGUI(BountyCore plugin, OfflinePlayer target) {
        this.plugin = plugin;
        this.target = target;
    }

    public void open(Player viewer) {
        Inventory gui = Bukkit.createInventory(null, 54, "§8§lProfile - " + target.getName());

        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) playerHead.getItemMeta();
        headMeta.setOwningPlayer(target);
        headMeta.setDisplayName("§e§l" + target.getName());
        List<String> headLore = new ArrayList<>();
        headLore.add("");
        headLore.add("§7Status: " + (target.isOnline() ? "§aOnline" : "§cOffline"));
        headMeta.setLore(headLore);
        playerHead.setItemMeta(headMeta);
        gui.setItem(13, playerHead);

        double balance = plugin.getEconomy().getBalance(target);
        gui.setItem(20, createInfoItem(Material.GOLD_INGOT, "§e§lBalance",
            "§7$" + new DecimalFormat("#,###.##").format(balance)));

        String rank = plugin.getRankManager().getRank(target.getUniqueId()).getDisplayName();
        gui.setItem(21, createInfoItem(Material.NAME_TAG, "§e§lRank", rank));

        int homes = 0;
        if (target.isOnline()) {
            homes = plugin.getHomeManager().getHomes(target.getUniqueId()).size();
        }
        gui.setItem(22, createInfoItem(Material.RED_BED, "§e§lHomes", "§7" + homes + " homes set"));

        PlayerStats stats = plugin.getPlayerStatsManager().getStats(target.getUniqueId());
        List<String> statsLore = new ArrayList<>();
        statsLore.add("§7Kills: §f" + stats.getKills());
        statsLore.add("§7Deaths: §f" + stats.getDeaths());
        statsLore.add("§7K/D Ratio: §f" + new DecimalFormat("0.00").format(stats.getKDRatio()));
        gui.setItem(23, createInfoItem(Material.DIAMOND_SWORD, "§e§lCombat Stats", statsLore));

        gui.setItem(24, createInfoItem(Material.CLOCK, "§e§lPlaytime",
            "§7" + stats.getFormattedPlaytime()));

        SellBooster booster = plugin.getSellBoosterManager().getBooster(target.getUniqueId());
        if (booster != null) {
            List<String> boosterLore = new ArrayList<>();
            boosterLore.add("§7Multiplier: §e" + booster.getMultiplier() + "x");
            boosterLore.add("§7Time Left: §e" + booster.getFormattedTimeRemaining());
            gui.setItem(29, createInfoItem(Material.NETHER_STAR, "§e§lSell Booster", boosterLore));
        } else {
            gui.setItem(29, createInfoItem(Material.BARRIER, "§c§lNo Sell Booster", "§7No active booster"));
        }

        double bounty = plugin.getBountyManager().getBounty(target.getUniqueId());
        if (bounty > 0) {
            gui.setItem(33, createInfoItem(Material.REDSTONE, "§e§lBounty",
                "§c$" + new DecimalFormat("#,###.##").format(bounty)));
        } else {
            gui.setItem(33, createInfoItem(Material.BARRIER, "§a§lNo Bounty", "§7No active bounty"));
        }

        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);

        for (int i = 0; i < 54; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, glassPane);
            }
        }

        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.setDisplayName("§c§lClose");
        closeButton.setItemMeta(closeMeta);
        gui.setItem(49, closeButton);

        viewer.openInventory(gui);
    }

    private ItemStack createInfoItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            List<String> loreList = new ArrayList<>();
            loreList.add("");
            for (String line : lore) {
                loreList.add(line);
            }
            meta.setLore(loreList);
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInfoItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> fullLore = new ArrayList<>();
        fullLore.add("");
        fullLore.addAll(lore);
        meta.setLore(fullLore);
        item.setItemMeta(meta);
        return item;
    }
}
