package com.bountysmp.bountyCore.teams;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class TeamChatListener implements Listener {
    private final BountyCore plugin;

    public TeamChatListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        if (!message.startsWith("#")) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();

        plugin.getTeamManager().getPlayerTeam(player.getUniqueId()).thenAccept(team -> {
            if (team == null) {
                player.sendMessage(ChatColor.RED + "You are not in a team!");
                return;
            }

            String teamMessage = ChatColor.LIGHT_PURPLE + "[Team] " + ChatColor.WHITE +
                               player.getName() + ChatColor.GRAY + ": " + message.substring(1);

            team.getMembers().forEach(uuid -> {
                Player member = Bukkit.getPlayer(uuid);
                if (member != null && member.isOnline()) {
                    member.sendMessage(teamMessage);
                }
            });
        });
    }
}
