package com.bountysmp.bountyCore.keyall;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class KeyAllManager {
    private final BountyCore plugin;
    private FileConfiguration keyallConfig;

    public KeyAllManager(BountyCore plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        File keyallFile = new File(plugin.getDataFolder(), "keyall.yml");
        if (!keyallFile.exists()) {
            plugin.saveResource("keyall.yml", false);
        }

        keyallConfig = YamlConfiguration.loadConfiguration(keyallFile);
    }

    public void giveKeyToAll(String crateName, int amount) {
        String commandTemplate = keyallConfig.getString("command-template",
            "crate givekey {player} {crate} {amount}");

        int playersGiven = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            String command = commandTemplate
                .replace("{player}", player.getName())
                .replace("{crate}", crateName)
                .replace("{amount}", String.valueOf(amount));

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            playersGiven++;
        }

        String broadcastMessage = keyallConfig.getString("broadcast-message",
            "&a&lKey All: &7Everyone received &e{amount}x {crate} &7keys!")
            .replace("{amount}", String.valueOf(amount))
            .replace("{crate}", crateName)
            .replace("&", "§");

        plugin.broadcastFiltered(broadcastMessage,
            com.bountysmp.bountyCore.settings.PlayerSettings::isKeyAllNotifications);

        plugin.getLogger().info("KeyAll: Gave " + amount + "x " + crateName + " keys to " + playersGiven + " players");
    }

    public void reload() {
        loadConfig();
    }
}
