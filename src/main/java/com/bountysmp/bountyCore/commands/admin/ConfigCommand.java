package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.sync.ConfigRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class ConfigCommand implements CommandExecutor, TabCompleter {

    private final BountyCore plugin;

    public ConfigCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.admin.config")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        ConfigRegistry registry = plugin.getConfigRegistry();

        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            sender.sendMessage("§6Registered configs:");
            for (ConfigRegistry.Entry entry : registry.getEntries()) {
                String status = entry.hasUnappliedChanges()
                    ? "§e(unapplied changes on disk)"
                    : "§7(up to date)";
                sender.sendMessage("§7 - §f" + entry.getName() + " " + status);
            }
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("reload")) {
            if (args[1].equalsIgnoreCase("all")) {
                List<String> failed = registry.reloadAll();
                if (failed.isEmpty()) {
                    sender.sendMessage("§aReloaded all " + registry.getEntries().size() + " configs.");
                } else {
                    sender.sendMessage("§eReloaded with errors in: §c" + String.join(", ", failed)
                        + " §7(see console)");
                }
                return true;
            }

            switch (registry.reload(args[1])) {
                case RELOADED -> sender.sendMessage("§aReloaded config '" + args[1].toLowerCase() + "'.");
                case FAILED -> sender.sendMessage("§cReload of '" + args[1].toLowerCase() + "' failed — see console.");
                case UNKNOWN -> sender.sendMessage("§cUnknown config '" + args[1] + "'. Use /config list.");
            }
            return true;
        }

        sender.sendMessage("§cUsage: /config <reload <name|all>|list>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("bounty.admin.config")) {
            return completions;
        }

        if (args.length == 1) {
            for (String option : List.of("reload", "list")) {
                if (option.startsWith(args[0].toLowerCase())) {
                    completions.add(option);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reload")) {
            if ("all".startsWith(args[1].toLowerCase())) {
                completions.add("all");
            }
            for (String name : plugin.getConfigRegistry().getNames()) {
                if (name.startsWith(args[1].toLowerCase())) {
                    completions.add(name);
                }
            }
        }
        return completions;
    }
}
