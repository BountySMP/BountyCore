package com.bountysmp.bountyCore.ranks;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class RankManager {
    private final BountyCore plugin;
    private final File ranksFile;
    private final File playersFile;
    private FileConfiguration ranksConfig;
    private FileConfiguration playersConfig;
    private final Map<String, RankGroup> groups;
    private final Map<UUID, PermissionAttachment> attachments;

    public RankManager(BountyCore plugin) {
        this.plugin = plugin;
        this.ranksFile = new File(plugin.getDataFolder(), "ranks.yml");
        this.playersFile = new File(plugin.getDataFolder(), "players.yml");
        this.groups = new HashMap<>();
        this.attachments = new HashMap<>();
        loadRanks();
        loadPlayers();
    }

    private void loadRanks() {
        if (!ranksFile.exists()) {
            plugin.saveResource("ranks.yml", false);
        }

        ranksConfig = YamlConfiguration.loadConfiguration(ranksFile);

        groups.clear();
        if (ranksConfig.contains("groups")) {
            for (String category : ranksConfig.getConfigurationSection("groups").getKeys(false)) {
                ConfigurationSection categorySection = ranksConfig.getConfigurationSection("groups." + category);
                for (String rankName : categorySection.getKeys(false)) {
                    String fullName = "group." + category + "." + rankName;
                    ConfigurationSection rankSection = categorySection.getConfigurationSection(rankName);

                    String prefix = rankSection.getString("prefix", "§7");
                    String chatColor = rankSection.getString("chat-color", "§f");
                    int weight = rankSection.getInt("weight", 0);
                    boolean bypassAll = rankSection.getBoolean("bypass-all", false);
                    List<String> permissions = rankSection.getStringList("permissions");

                    groups.put(fullName, new RankGroup(fullName, category, prefix, chatColor, weight, bypassAll, permissions));
                }
            }
        }

        plugin.getLogger().info("Loaded " + groups.size() + " rank groups");
    }

    private void loadPlayers() {
        if (!playersFile.exists()) {
            plugin.saveResource("players.yml", false);
        }

        playersConfig = YamlConfiguration.loadConfiguration(playersFile);
    }

    public List<String> getGroups(UUID player) {
        String uuidStr = player.toString();
        return playersConfig.getStringList(uuidStr + ".groups");
    }

    public List<String> getPermissions(UUID player) {
        String uuidStr = player.toString();
        return playersConfig.getStringList(uuidStr + ".permissions");
    }

    public void addGroup(UUID player, String group) {
        List<String> groups = new ArrayList<>(getGroups(player));
        if (!groups.contains(group)) {
            groups.add(group);
            playersConfig.set(player.toString() + ".groups", groups);
            savePlayers();
            refreshPermissions(player);
        }
    }

    public void removeGroup(UUID player, String group) {
        List<String> groups = new ArrayList<>(getGroups(player));
        if (groups.remove(group)) {
            playersConfig.set(player.toString() + ".groups", groups);
            savePlayers();
            refreshPermissions(player);
        }
    }

    public void addPermission(UUID player, String permission) {
        List<String> permissions = new ArrayList<>(getPermissions(player));
        if (!permissions.contains(permission)) {
            permissions.add(permission);
            playersConfig.set(player.toString() + ".permissions", permissions);
            savePlayers();
            refreshPermissions(player);
        }
    }

    public void removePermission(UUID player, String permission) {
        List<String> permissions = new ArrayList<>(getPermissions(player));
        if (permissions.remove(permission)) {
            playersConfig.set(player.toString() + ".permissions", permissions);
            savePlayers();
            refreshPermissions(player);
        }
    }

    public RankGroup getHighestStaffGroup(UUID player) {
        List<String> playerGroups = getGroups(player);
        RankGroup highest = null;

        for (String groupName : playerGroups) {
            RankGroup group = groups.get(groupName);
            if (group != null && group.getCategory().equals("staff")) {
                if (highest == null || group.getWeight() > highest.getWeight()) {
                    highest = group;
                }
            }
        }

        return highest;
    }

    public RankGroup getHighestDonatorGroup(UUID player) {
        List<String> playerGroups = getGroups(player);
        RankGroup highest = null;

        for (String groupName : playerGroups) {
            RankGroup group = groups.get(groupName);
            if (group != null && group.getCategory().equals("donator")) {
                if (highest == null || group.getWeight() > highest.getWeight()) {
                    highest = group;
                }
            }
        }

        return highest;
    }

    public RankGroup getDefaultGroup() {
        return groups.get("group.default.member");
    }

    public boolean hasBypassAll(UUID player) {
        List<String> playerGroups = getGroups(player);
        for (String groupName : playerGroups) {
            RankGroup group = groups.get(groupName);
            if (group != null && group.isBypassAll()) {
                return true;
            }
        }
        return false;
    }

    public void injectPermissions(Player player) {
        removePermissions(player.getUniqueId());

        PermissionAttachment attachment = player.addAttachment(plugin);
        attachments.put(player.getUniqueId(), attachment);

        Set<String> allPermissions = new HashSet<>();

        // Add group permissions with inheritance
        List<String> playerGroups = getGroups(player.getUniqueId());
        for (String groupName : playerGroups) {
            RankGroup group = groups.get(groupName);
            if (group != null) {
                // Add this group's permissions
                allPermissions.addAll(group.getPermissions());

                // Inherit permissions from all lower-weight groups in the same category
                String category = group.getCategory();
                int groupWeight = group.getWeight();

                for (RankGroup lowerGroup : groups.values()) {
                    // Inherit from same category with lower weight
                    if (lowerGroup.getCategory().equals(category) && lowerGroup.getWeight() < groupWeight) {
                        allPermissions.addAll(lowerGroup.getPermissions());
                    }
                    // Also inherit from default category if not already in default
                    if (!category.equals("default") && lowerGroup.getCategory().equals("default")) {
                        allPermissions.addAll(lowerGroup.getPermissions());
                    }
                }
            }
        }

        // Add individual permissions
        allPermissions.addAll(getPermissions(player.getUniqueId()));

        // Apply permissions
        for (String permission : allPermissions) {
            if (permission.equals("*")) {
                // Grant all registered permissions
                for (Permission perm : Bukkit.getPluginManager().getPermissions()) {
                    attachment.setPermission(perm, true);
                }
            } else {
                attachment.setPermission(permission, true);
            }
        }
    }

    public void removePermissions(UUID player) {
        PermissionAttachment attachment = attachments.remove(player);
        if (attachment != null) {
            attachment.remove();
        }
    }

    public void refreshPermissions(UUID player) {
        Player onlinePlayer = Bukkit.getPlayer(player);
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            injectPermissions(onlinePlayer);
        }
    }

    private void savePlayers() {
        try {
            playersConfig.save(playersFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save players.yml", e);
        }
    }

    public RankGroup getGroup(String groupName) {
        return groups.get(groupName);
    }

    public boolean groupExists(String groupName) {
        return groups.containsKey(groupName);
    }

    public static class RankGroup {
        private final String name;
        private final String category;
        private final String prefix;
        private final String chatColor;
        private final int weight;
        private final boolean bypassAll;
        private final List<String> permissions;

        public RankGroup(String name, String category, String prefix, String chatColor, int weight, boolean bypassAll, List<String> permissions) {
            this.name = name;
            this.category = category;
            this.prefix = prefix;
            this.chatColor = chatColor;
            this.weight = weight;
            this.bypassAll = bypassAll;
            this.permissions = permissions;
        }

        public String getName() {
            return name;
        }

        public String getCategory() {
            return category;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getChatColor() {
            return chatColor;
        }

        public int getWeight() {
            return weight;
        }

        public boolean isBypassAll() {
            return bypassAll;
        }

        public List<String> getPermissions() {
            return permissions;
        }
    }
}
