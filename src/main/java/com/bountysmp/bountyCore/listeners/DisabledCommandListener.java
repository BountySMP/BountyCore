package com.bountysmp.bountyCore.listeners;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class DisabledCommandListener implements Listener {
    private final BountyCore plugin;

    public DisabledCommandListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Bypass-all players can run any command
        if (plugin.getRankManager().hasBypassAll(player.getUniqueId())) {
            return;
        }

        // Remove leading slash and convert to lowercase
        String command = message.substring(1).toLowerCase();

        // Get disabled commands list from config
        List<String> disabledCommands = plugin.getConfig().getStringList("disabled-commands");

        // Check if command matches any disabled command
        for (String disabled : disabledCommands) {
            String disabledLower = disabled.toLowerCase();

            // Check exact match or if command starts with disabled command
            if (command.equals(disabledLower) || command.startsWith(disabledLower + " ")) {
                event.setCancelled(true);

                // Send configurable blocked message
                String blockedMessage = plugin.getMessagesConfig().getString("disabled-command", "§cThis command is disabled.");
                blockedMessage = ChatColor.translateAlternateColorCodes('&', blockedMessage);
                player.sendMessage(blockedMessage);
                return;
            }
        }
    }
}
