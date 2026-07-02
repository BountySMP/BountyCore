package com.bountysmp.bountyCore.scoreboard;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class ColorUtils {

    private static boolean hasPAPI = false;

    private ColorUtils() {}

    public static void init() {
        hasPAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public static Component toComponent(String text) {
        if (text == null || text.isEmpty()) return Component.empty();
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    public static Component toComponent(String text, Player player) {
        if (text == null) return Component.empty();
        String resolved = text;
        if (hasPAPI && player != null) {
            try {
                resolved = PlaceholderAPI.setPlaceholders(player, resolved);
            } catch (Exception ignored) {}
        }
        return toComponent(resolved);
    }
}
