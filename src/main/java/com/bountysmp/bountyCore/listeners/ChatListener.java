package com.bountysmp.bountyCore.listeners;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.ranks.RankManager;
import com.bountysmp.bountyCore.stats.PlayerStats;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.text.DecimalFormat;

public class ChatListener implements Listener {
    private final BountyCore plugin;
    private final DecimalFormat df = new DecimalFormat("#,###.##");

    public ChatListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    private String trailingColor(String text) {
        String last = "§f";
        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == '§') {
                char code = text.charAt(i + 1);
                if ("0123456789abcdef".indexOf(code) >= 0) {
                    last = "§" + code;
                }
                i++;
            }
        }
        return last;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        if (!plugin.getConfig().getBoolean("chat-hover-stats.enabled", true)) {
            return;
        }

        Player player = event.getPlayer();
        String playerName = player.getName();

        PlayerStats stats = plugin.getPlayerStatsManager().getStats(player.getUniqueId());
        double balance = plugin.getEconomy().getBalance(player);

        Component hoverText = Component.text()
            .append(Component.text("Balance: ", NamedTextColor.GRAY))
            .append(Component.text(df.format(balance) + " " + plugin.getEconomy().currencyNamePlural(), NamedTextColor.GOLD))
            .append(Component.newline())
            .append(Component.text("Kills: ", NamedTextColor.GRAY))
            .append(Component.text(String.valueOf(stats.getKills()), NamedTextColor.GREEN))
            .append(Component.newline())
            .append(Component.text("Deaths: ", NamedTextColor.GRAY))
            .append(Component.text(String.valueOf(stats.getDeaths()), NamedTextColor.RED))
            .append(Component.newline())
            .append(Component.text("Playtime: ", NamedTextColor.GRAY))
            .append(Component.text(stats.getFormattedPlaytime(), NamedTextColor.YELLOW))
            .build();

        RankManager.RankGroup rank = plugin.getRankManager().getRank(player.getUniqueId());
        String rankPrefix = "";
        String nameColor = "§f";
        String chatColor = "§f";
        if (rank != null && !rank.getCategory().equals("default")) {
            rankPrefix = rank.getPrefix();
            nameColor = trailingColor(rankPrefix);
            chatColor = rank.getChatColor();
        }

        final String finalRankPrefix = rankPrefix;
        final String finalNameColor = nameColor;
        final String finalChatColor = chatColor;

        Component nameComponent = LegacyComponentSerializer.legacySection()
            .deserialize(finalNameColor + playerName)
            .hoverEvent(HoverEvent.showText(hoverText));

        event.renderer((source, sourceDisplayName, message, viewer) ->
            Component.text()
                .append(LegacyComponentSerializer.legacySection().deserialize(finalRankPrefix))
                .append(nameComponent)
                .append(Component.text(": "))
                .append(LegacyComponentSerializer.legacySection().deserialize(finalChatColor))
                .append(message)
                .build()
        );
    }
}
