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
        if (staffGroup != null) {
            prefix = ChatColor.translateAlternateColorCodes('&', staffGroup.getPrefix());
        } else if (donatorGroup != null) {
            prefix = ChatColor.translateAlternateColorCodes('&', donatorGroup.getPrefix());
        } else {
            RankManager.RankGroup defaultGroup = rankManager.getDefaultGroup();
            if (defaultGroup != null) {
                prefix = ChatColor.translateAlternateColorCodes('&', defaultGroup.getPrefix());
            }
        }

        // Add AFK prefix if player is AFK
        if (plugin.getAfkManager() != null && plugin.getAfkManager().isAfk(player.getUniqueId())) {
            prefix = ChatColor.GRAY + "[AFK] " + prefix;
        }

        player.setPlayerListName(prefix + player.getName());
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

        // Build header
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < headerLines.size(); i++) {
            String line = ChatColor.translateAlternateColorCodes('&', headerLines.get(i));
            header.append(line);
            if (i < headerLines.size() - 1) {
                header.append("\n");
            }
        }

        // Build footer with staff count replacement
        StringBuilder footer = new StringBuilder();
        for (int i = 0; i < footerLines.size(); i++) {
            String line = ChatColor.translateAlternateColorCodes('&', footerLines.get(i));
            line = line.replace("{staff_count}", String.valueOf(staffCount));
            footer.append(line);
            if (i < footerLines.size() - 1) {
                footer.append("\n");
            }
        }

        // Apply to all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setPlayerListHeader(header.toString());
            player.setPlayerListFooter(footer.toString());
        }
    }

    public void sortTabList() {
        // Get all online players
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

        // Sort by rank weight (highest first)
        players.sort((p1, p2) -> {
            int weight1 = getHighestWeight(p1);
            int weight2 = getHighestWeight(p2);
            return Integer.compare(weight2, weight1); // Descending order
        });

        // Note: Tab list sorting requires ProtocolLib or paper-specific API
        // This is a placeholder - actual sorting would need additional implementation
        // For now, just update display names which affects some clients
        for (Player player : players) {
            updatePlayer(player);
        }
    }

    private int getHighestWeight(Player player) {
        RankManager rankManager = plugin.getRankManager();
        List<String> playerGroups = rankManager.getGroups(player.getUniqueId());

        int highestWeight = 0;
        for (String groupName : playerGroups) {
            RankManager.RankGroup group = rankManager.getGroup(groupName);
            if (group != null && group.getWeight() > highestWeight) {
                highestWeight = group.getWeight();
            }
        }

        return highestWeight;
    }

    public void reload() {
        loadConfig();
        updateAll();
    }
}
