package com.bountysmp.bountyCore.listeners;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.ranks.RankManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class RankListener implements Listener {
    private final BountyCore plugin;

    public RankListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getRankManager().injectPermissions(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getRankManager().removePermissions(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        // Cancel the event to bypass Paper's chat pipeline
        event.setCancelled(true);

        Player player = event.getPlayer();
        Component message = event.message();
        RankManager rankManager = plugin.getRankManager();

        // Get highest staff group first (priority)
        RankManager.RankGroup staffGroup = rankManager.getHighestStaffGroup(player.getUniqueId());
        RankManager.RankGroup donatorGroup = rankManager.getHighestDonatorGroup(player.getUniqueId());
        RankManager.RankGroup defaultGroup = rankManager.getDefaultGroup();

        String prefix;
        String chatColor;

        if (staffGroup != null) {
            // Staff prefix takes priority
            prefix = staffGroup.getPrefix();
            chatColor = staffGroup.getChatColor();
        } else if (donatorGroup != null) {
            // Donator prefix if no staff group
            prefix = donatorGroup.getPrefix();
            chatColor = donatorGroup.getChatColor();
        } else {
            // Default member prefix
            prefix = defaultGroup != null ? defaultGroup.getPrefix() : "§7";
            chatColor = defaultGroup != null ? defaultGroup.getChatColor() : "§f";
        }

        // Convert legacy color codes to Adventure Components
        Component prefixComponent = LegacyComponentSerializer.legacySection().deserialize(prefix);
        Component chatColorComponent = LegacyComponentSerializer.legacySection().deserialize(chatColor);

        // Build formatted component: <prefix><playername>: <chat-color><message>
        Component formatted = prefixComponent
            .append(Component.text(player.getName()))
            .append(Component.text(": ").color(NamedTextColor.GRAY))
            .append(chatColorComponent.append(message));

        // Manually broadcast to all online players, bypassing Paper's pipeline
        for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            p.sendMessage(formatted);
        }

        // Send to console
        org.bukkit.Bukkit.getConsoleSender().sendMessage(formatted);
    }
}

