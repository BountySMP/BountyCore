package com.bountysmp.bountyCore.auction;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.auction.storage.AuctionStorage;
import com.bountysmp.bountyCore.auction.storage.FlatFileAuctionStorage;
import com.bountysmp.bountyCore.auction.storage.MySQLAuctionStorage;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AuctionManager {
    private final BountyCore plugin;
    private final AuctionStorage storage;
    private final double saleFeePercent;
    private final long listingDuration;
    private HikariDataSource dataSource;

    public AuctionManager(BountyCore plugin) {
        this.plugin = plugin;
        this.saleFeePercent = plugin.getConfig().getDouble("auction.sale-fee-percent", 10.0);
        this.listingDuration = plugin.getConfig().getLong("auction.listing-duration-hours", 24) * 3600000L;

        String storageType = plugin.getConfig().getString("auction.storage-type", "FLATFILE");

        if (storageType.equalsIgnoreCase("MYSQL")) {
            String host = plugin.getConfig().getString("economy.mysql.host", "localhost");
            int port = plugin.getConfig().getInt("economy.mysql.port", 3306);
            String database = plugin.getConfig().getString("economy.mysql.database", "bountycore");
            String username = plugin.getConfig().getString("economy.mysql.username", "root");
            String password = plugin.getConfig().getString("economy.mysql.password", "");
            int poolSize = plugin.getConfig().getInt("economy.mysql.pool-size", 10);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(poolSize);
            config.setConnectionTimeout(5000);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            this.dataSource = new HikariDataSource(config);
            this.storage = new MySQLAuctionStorage(dataSource, plugin.getLogger());
            plugin.getLogger().info("Using MySQL storage for auction house");
        } else {
            this.storage = new FlatFileAuctionStorage(plugin.getDataFolder(), plugin.getLogger());
            plugin.getLogger().info("Using FlatFile storage for auction house");
        }
    }

    public CompletableFuture<Boolean> listItem(Player seller, ItemStack item, double price) {
        return CompletableFuture.supplyAsync(() -> {
            if (item == null || item.getType().isAir()) {
                return false;
            }

            int limit = getListingLimit(seller);
            int currentCount = storage.getPlayerListingCount(seller.getUniqueId()).join();

            if (currentCount >= limit) {
                seller.sendMessage(plugin.getMessage("auction.limit-reached", "limit", limit));
                return false;
            }

            UUID listingId = UUID.randomUUID();
            long expiryTime = System.currentTimeMillis() + listingDuration;

            AuctionListing listing = new AuctionListing(
                listingId,
                seller.getUniqueId(),
                seller.getName(),
                item.clone(),
                price,
                expiryTime,
                AuctionListing.ListingStatus.ACTIVE
            );

            storage.saveListing(listing).join();
            return true;
        });
    }

    public CompletableFuture<Boolean> buyItem(Player buyer, UUID listingId) {
        return CompletableFuture.supplyAsync(() -> {
            AuctionListing listing = storage.getListing(listingId).join();

            if (listing == null || listing.getStatus() != AuctionListing.ListingStatus.ACTIVE) {
                return false;
            }

            if (listing.isExpired()) {
                listing.setStatus(AuctionListing.ListingStatus.EXPIRED);
                storage.saveListing(listing).join();
                return false;
            }

            double balance = plugin.getEconomy().getBalance(buyer);
            if (balance < listing.getPrice()) {
                buyer.sendMessage(plugin.getMessage("auction.insufficient-funds"));
                return false;
            }

            plugin.getEconomy().withdrawPlayer(buyer, listing.getPrice());

            double fee = listing.getPrice() * (saleFeePercent / 100.0);
            double sellerAmount = listing.getPrice() - fee;

            plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(listing.getSellerUuid()), sellerAmount);

            if (buyer.getInventory().firstEmpty() == -1) {
                buyer.getWorld().dropItem(buyer.getLocation(), listing.getItem());
            } else {
                buyer.getInventory().addItem(listing.getItem());
            }

            listing.setStatus(AuctionListing.ListingStatus.SOLD);
            storage.saveListing(listing).join();

            Player seller = Bukkit.getPlayer(listing.getSellerUuid());
            if (seller != null && seller.isOnline()) {
                seller.sendMessage(plugin.getMessage("auction.item-sold",
                    "buyer", buyer.getName(),
                    "item", getItemName(listing.getItem()),
                    "amount", plugin.getEconomy().format(sellerAmount)));
            }

            return true;
        });
    }

    public void expireListings() {
        storage.getActiveListings().thenAccept(listings -> {
            for (AuctionListing listing : listings) {
                if (listing.isExpired()) {
                    listing.setStatus(AuctionListing.ListingStatus.EXPIRED);
                    storage.saveListing(listing);
                }
            }
        });
    }

    public CompletableFuture<Boolean> returnItem(Player player, UUID listingId) {
        return CompletableFuture.supplyAsync(() -> {
            AuctionListing listing = storage.getListing(listingId).join();

            if (listing == null || !listing.getSellerUuid().equals(player.getUniqueId())) {
                return false;
            }

            if (listing.getStatus() != AuctionListing.ListingStatus.EXPIRED) {
                return false;
            }

            if (player.getInventory().firstEmpty() == -1) {
                player.sendMessage(plugin.getMessage("auction.inventory-full"));
                return false;
            }

            player.getInventory().addItem(listing.getItem());
            storage.deleteListing(listingId).join();
            return true;
        });
    }

    public CompletableFuture<List<AuctionListing>> getActiveListings() {
        return storage.getActiveListings();
    }

    public CompletableFuture<List<AuctionListing>> getExpiredListings(UUID playerUuid) {
        return storage.getExpiredListings(playerUuid);
    }

    public CompletableFuture<List<AuctionListing>> searchListings(String query) {
        return storage.searchListings(query);
    }

    public int getListingLimit(Player player) {
        if (player.isOp()) {
            return 999;
        }

        for (int i : new int[]{30, 25, 20, 15}) {
            if (player.hasPermission("bountycore.ah." + i)) {
                return i;
            }
        }
        return 15;
    }

    private String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name().replace("_", " ").toLowerCase();
    }

    public CompletableFuture<List<AuctionListing>> getPlayerListings(UUID playerUuid) {
        return storage.getActiveListings().thenApply(listings ->
            listings.stream()
                .filter(listing -> listing.getSellerUuid().equals(playerUuid))
                .filter(listing -> listing.getStatus() == AuctionListing.ListingStatus.ACTIVE)
                .toList()
        );
    }

    public CompletableFuture<Boolean> cancelListing(Player player, UUID listingId) {
        return CompletableFuture.supplyAsync(() -> {
            AuctionListing listing = storage.getListing(listingId).join();

            if (listing == null || !listing.getSellerUuid().equals(player.getUniqueId())) {
                return false;
            }

            if (listing.getStatus() != AuctionListing.ListingStatus.ACTIVE) {
                return false;
            }

            // Return item to player
            if (player.getInventory().firstEmpty() == -1) {
                return false;
            }

            player.getInventory().addItem(listing.getItem());
            listing.setStatus(AuctionListing.ListingStatus.EXPIRED);
            storage.saveListing(listing);

            return true;
        });
    }

    public void close() {
        storage.close();
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
