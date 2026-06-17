package com.bountysmp.bountyCore.randomtp;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RandomTeleportGUI {
    private final BountyCore plugin;
    private static final int MIN_DISTANCE = 1000;
    private static final int MAX_DISTANCE = 50000;
    private static final int COOLDOWN_SECONDS = 5;
    private static final int CHUNK_PRELOAD_RADIUS = 3;
    private static final int MAX_ATTEMPTS = 15;
    private static final Random random = new Random();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public RandomTeleportGUI(BountyCore plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "Random Teleport");

        // Slot 2 - Overworld
        ItemStack overworldItem = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta overworldMeta = overworldItem.getItemMeta();
        overworldMeta.setDisplayName("§aOverworld");
        overworldItem.setItemMeta(overworldMeta);
        gui.setItem(2, overworldItem);

        // Slot 4 - Nether
        ItemStack netherItem = new ItemStack(Material.NETHERITE_BLOCK);
        ItemMeta netherMeta = netherItem.getItemMeta();
        netherMeta.setDisplayName("§cNether");
        netherItem.setItemMeta(netherMeta);
        gui.setItem(4, netherItem);

        // Slot 6 - The End
        ItemStack endItem = new ItemStack(Material.END_STONE);
        ItemMeta endMeta = endItem.getItemMeta();
        endMeta.setDisplayName("§dThe End");
        endItem.setItemMeta(endMeta);
        gui.setItem(6, endItem);

        player.openInventory(gui);
    }

    public void handleClick(Player player, int slot) {
        // Check cooldown
        UUID uuid = player.getUniqueId();
        if (cooldowns.containsKey(uuid)) {
            long timeLeftMs = cooldowns.get(uuid) - System.currentTimeMillis();
            if (timeLeftMs > 0) {
                String timeFormatted = formatTime(timeLeftMs / 1000);
                player.sendMessage(ChatColor.RED + "You must wait " + ChatColor.YELLOW + timeFormatted + ChatColor.RED + " before using random teleport again.");
                return;
            }
        }

        World.Environment environment;
        switch (slot) {
            case 2:
                environment = World.Environment.NORMAL;
                break;
            case 4:
                environment = World.Environment.NETHER;
                break;
            case 6:
                environment = World.Environment.THE_END;
                break;
            default:
                return;
        }

        player.closeInventory();

        // Get target world
        World targetWorld = null;
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == environment) {
                targetWorld = world;
                break;
            }
        }

        if (targetWorld == null) {
            player.sendMessage("§cCould not find target world!");
            return;
        }

        // Set cooldown immediately (get from config)
        int cooldownSeconds = plugin.getConfig().getInt("randomtp.cooldown-seconds", 900);
        cooldowns.put(uuid, System.currentTimeMillis() + (cooldownSeconds * 1000));

        // Show message INSTANTLY with just world name
        String worldName = getWorldName(environment);
        player.sendMessage(ChatColor.GREEN + "Teleporting you to a random location in " + ChatColor.YELLOW + worldName + ChatColor.GREEN + " in " + COOLDOWN_SECONDS + " seconds...");

        World finalWorld = targetWorld;
        UUID playerUuid = uuid;

        // Start finding location async while countdown happens
        startTeleportWithAsyncLocationSearch(player, finalWorld, environment, playerUuid);
    }

    private void startTeleportWithAsyncLocationSearch(Player player, World world, World.Environment environment, UUID playerUuid) {
        final Location[] foundLocation = {null};

        // Start finding location async immediately
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Location safeLocation = findSafeLocationQuick(world, environment);
            foundLocation[0] = safeLocation;

            if (safeLocation != null) {
                // Preload chunks while waiting
                preloadChunksAsync(safeLocation);
            }
        });

        // Start countdown timer
        new BukkitRunnable() {
            int countdown = COOLDOWN_SECONDS;
            Location startLocation = player.getLocation().clone();

            @Override
            public void run() {
                // Check if player moved
                if (startLocation.getWorld() != player.getLocation().getWorld() ||
                    startLocation.distance(player.getLocation()) > 0.1) {
                    player.sendMessage(ChatColor.RED + "Teleportation cancelled - you moved!");
                    cooldowns.remove(playerUuid);
                    cancel();
                    return;
                }

                if (countdown > 0) {
                    countdown--;
                } else {
                    // Countdown finished, check if location is found
                    if (foundLocation[0] != null) {
                        player.teleport(foundLocation[0]);
                        String worldName = getWorldName(environment);
                        int x = foundLocation[0].getBlockX();
                        int y = foundLocation[0].getBlockY();
                        int z = foundLocation[0].getBlockZ();
                        player.sendMessage(ChatColor.GREEN + "Teleported to " + ChatColor.YELLOW + x + " " + y + " " + z + " " + worldName + ChatColor.GREEN + "!");
                        cancel();
                    } else {
                        // Location not found yet, wait a bit more
                        player.sendMessage(ChatColor.YELLOW + "Almost there...");
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private Location findSafeLocationQuick(World world, World.Environment environment) {
        return findSafeLocationRecursive(world, environment, 0);
    }

    private Location findSafeLocationRecursive(World world, World.Environment environment, int attempt) {
        if (attempt >= MAX_ATTEMPTS) {
            return null;
        }

        int x = generateRandomCoordinate();
        int z = generateRandomCoordinate();

        // Load chunk synchronously (we're already on async thread)
        world.getChunkAt(x >> 4, z >> 4);

        Location safe = findSafeY(world, x, z, environment);

        // If location is not safe, try again recursively
        if (safe == null) {
            return findSafeLocationRecursive(world, environment, attempt + 1);
        }

        return safe;
    }

    private Location findSafeY(World world, int x, int z, World.Environment environment) {
        if (environment == World.Environment.NETHER) {
            // Search nether Y range
            for (int y = 70; y <= 118; y++) {
                if (isSafe(world, x, y, z)) {
                    return new Location(world, x + 0.5, y, z + 0.5);
                }
            }
            for (int y = 69; y >= 32; y--) {
                if (isSafe(world, x, y, z)) {
                    return new Location(world, x + 0.5, y, z + 0.5);
                }
            }
            return null; // No safe spot found
        } else if (environment == World.Environment.THE_END) {
            // Skip central island
            if (Math.abs(x) < 100 && Math.abs(z) < 100) {
                return null;
            }

            // Search down for solid ground
            for (int y = 80; y >= 0; y--) {
                Material block = world.getBlockAt(x, y, z).getType();
                if (block.isSolid() && block != Material.BEDROCK) {
                    // Found ground, check if spot above is safe
                    if (isSafe(world, x, y + 1, z)) {
                        return new Location(world, x + 0.5, y + 1, z + 0.5);
                    }
                }
            }
            return null; // No safe spot found
        } else {
            // Overworld - simple like SimpleRTP
            int y = world.getHighestBlockYAt(x, z);
            Material groundBlock = world.getBlockAt(x, y, z).getType();

            // If ground is liquid, recursively try again
            if (groundBlock == Material.WATER || groundBlock == Material.LAVA) {
                return null;
            }

            // Return location 1 block above the highest block
            return new Location(world, x + 0.5, y + 1, z + 0.5);
        }
    }

    private boolean isSafe(World world, int x, int y, int z) {
        try {
            if (y < 1 || y > world.getMaxHeight() - 2) {
                return false;
            }

            Material ground = world.getBlockAt(x, y - 1, z).getType();
            Material feet = world.getBlockAt(x, y, z).getType();
            Material head = world.getBlockAt(x, y + 1, z).getType();

            // Ground must be solid and safe
            if (!ground.isSolid()) {
                return false;
            }

            // No lava or dangerous blocks
            if (ground == Material.LAVA || ground == Material.MAGMA_BLOCK) {
                return false;
            }

            // Feet and head must be passable (air or non-solid)
            if (feet.isSolid() && feet != Material.WATER) {
                return false;
            }
            if (head.isSolid()) {
                return false;
            }

            // No lava at feet or head
            if (feet == Material.LAVA || head == Material.LAVA) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private int generateRandomCoordinate() {
        int distance = MIN_DISTANCE + random.nextInt(MAX_DISTANCE - MIN_DISTANCE);
        return random.nextBoolean() ? distance : -distance;
    }

    private String getWorldName(World.Environment environment) {
        switch (environment) {
            case NETHER:
                return "Nether";
            case THE_END:
                return "The End";
            case NORMAL:
            default:
                return "Overworld";
        }
    }

    private void preloadChunksAsync(Location location) {
        World world = location.getWorld();
        int centerChunkX = location.getBlockX() >> 4;
        int centerChunkZ = location.getBlockZ() >> 4;

        // Preload chunks in radius - happens during the 5 second countdown
        for (int x = -CHUNK_PRELOAD_RADIUS; x <= CHUNK_PRELOAD_RADIUS; x++) {
            for (int z = -CHUNK_PRELOAD_RADIUS; z <= CHUNK_PRELOAD_RADIUS; z++) {
                world.getChunkAtAsync(centerChunkX + x, centerChunkZ + z);
            }
        }
    }

    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + " second" + (seconds == 1 ? "" : "s");
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long secs = seconds % 60;
            if (secs == 0) {
                return minutes + " minute" + (minutes == 1 ? "" : "s");
            }
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " " + secs + " second" + (secs == 1 ? "" : "s");
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            if (minutes == 0) {
                return hours + " hour" + (hours == 1 ? "" : "s");
            }
            return hours + " hour" + (hours == 1 ? "" : "s") + " " + minutes + " minute" + (minutes == 1 ? "" : "s");
        }
    }
}
