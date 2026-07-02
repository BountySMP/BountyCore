package com.bountysmp.bountyCore.tab;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.ranks.RankManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class TabManager {
    private final BountyCore plugin;
    private final File tabFile;
    private FileConfiguration tabConfig;
    private String staffPermissionPrefix;

    public TabManager(BountyCore plugin) {
        this.plugin = plugin;
        this.tabFile = new File(plugin.getDataFolder(), "tab.yml");
        loadConfig();
    }

    private void loadConfig() {
        if (!tabFile.exists()) {
            plugin.saveResource("tab.yml", false);
        }
        tabConfig = YamlConfiguration.loadConfiguration(tabFile);
        staffPermissionPrefix = tabConfig.getString("staff-permission", "group.staff");
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayer(player);
        }
        updateHeaderFooter();
    }

    public void updatePlayer(Player player) {
        RankManager rankManager = plugin.getRankManager();

        // Get highest staff group for prefix
        RankManager.RankGroup staffGroup = rankManager.getHighestStaffGroup(player.getUniqueId());
        RankManager.RankGroup donatorGroup = rankManager.getHighestDonatorGroup(player.getUniqueId());

        String prefix = "";
        int order; // higher sorts first in the tab list (1.21.2+ listOrder)
        if (staffGroup != null) {
            prefix = ChatColor.translateAlternateColorCodes('&', staffGroup.getPrefix());
            order = 20_000 + staffGroup.getWeight();
        } else if (donatorGroup != null) {
            prefix = ChatColor.translateAlternateColorCodes('&', donatorGroup.getPrefix());
            order = 10_000 + donatorGroup.getWeight();
        } else {
            RankManager.RankGroup defaultGroup = rankManager.getDefaultGroup();
            if (defaultGroup != null) {
                prefix = ChatColor.translateAlternateColorCodes('&', defaultGroup.getPrefix());
            }
            order = 0;
        }

        player.setPlayerListName(prefix + player.getName());
        player.setPlayerListOrder(order);
    }

    public void updateHeaderFooter() {
        List<String> headerLines = tabConfig.getStringList("header");
        List<String> footerLines = tabConfig.getStringList("footer");

        // Count online staff
        int staffCount = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            List<String> groups = plugin.getRankManager().getGroups(player.getUniqueId());
            for (String group : groups) {
                if (group.startsWith(staffPermissionPrefix)) {
                    staffCount++;
                    break;
                }
            }
        }

        int onlineCount = Bukkit.getOnlinePlayers().size();
        String header = buildSection(headerLines, onlineCount, staffCount);
        String footer = buildSection(footerLines, onlineCount, staffCount);

        // Apply to all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setPlayerListHeader(header);
            player.setPlayerListFooter(footer);
        }
    }

    private String buildSection(List<String> lines, int onlineCount, int staffCount) {
        StringBuilder section = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i)
                .replace("{online}", String.valueOf(onlineCount))
                .replace("{staff_count}", String.valueOf(staffCount));
            section.append(ChatColor.translateAlternateColorCodes('&', line));
            if (i < lines.size() - 1) {
                section.append("\n");
            }
        }
        return section.toString();
    }

    public void reload() {
        loadConfig();
        updateAll();
    }
}
