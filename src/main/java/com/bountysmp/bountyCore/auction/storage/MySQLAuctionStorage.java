package com.bountysmp.bountyCore.auction.storage;

import com.bountysmp.bountyCore.auction.AuctionListing;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLAuctionStorage implements AuctionStorage {
    private final HikariDataSource dataSource;
    private final Logger logger;

    public MySQLAuctionStorage(HikariDataSource dataSource, Logger logger) {
        this.dataSource = dataSource;
        this.logger = logger;
        createTables();
    }

    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS auction_listings (" +
                     "listing_id VARCHAR(36) PRIMARY KEY, " +
                     "seller_uuid VARCHAR(36) NOT NULL, " +
                     "seller_name VARCHAR(16) NOT NULL, " +
                     "item TEXT NOT NULL, " +
                     "price DOUBLE NOT NULL, " +
                     "expiry_time BIGINT NOT NULL, " +
                     "status VARCHAR(16) NOT NULL, " +
                     "INDEX idx_status (status), " +
                     "INDEX idx_seller (seller_uuid), " +
                     "INDEX idx_expiry (expiry_time)" +
                     ")";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create auction_listings table", e);
        }
    }

    @Override
    public CompletableFuture<Void> saveListing(AuctionListing listing) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO auction_listings (listing_id, seller_uuid, seller_name, item, price, expiry_time, status) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE status = ?, price = ?, expiry_time = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                String itemData = serializeItem(listing.getItem());
                stmt.setString(1, listing.getListingId().toString());
                stmt.setString(2, listing.getSellerUuid().toString());
                stmt.setString(3, listing.getSellerName());
                stmt.setString(4, itemData);
                stmt.setDouble(5, listing.getPrice());
                stmt.setLong(6, listing.getExpiryTime());
                stmt.setString(7, listing.getStatus().name());
                stmt.setString(8, listing.getStatus().name());
                stmt.setDouble(9, listing.getPrice());
                stmt.setLong(10, listing.getExpiryTime());

                stmt.executeUpdate();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to save auction listing", e);
            }
        });
    }

    @Override
    public CompletableFuture<AuctionListing> getListing(UUID listingId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM auction_listings WHERE listing_id = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, listingId.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return extractListing(rs);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to load auction listing", e);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<List<AuctionListing>> getActiveListings() {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM auction_listings WHERE status = 'ACTIVE' ORDER BY expiry_time ASC";
            List<AuctionListing> listings = new ArrayList<>();

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    listings.add(extractListing(rs));
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to load active listings", e);
            }
            return listings;
        });
    }

    @Override
    public CompletableFuture<List<AuctionListing>> getExpiredListings(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM auction_listings WHERE seller_uuid = ? AND status = 'EXPIRED'";
            List<AuctionListing> listings = new ArrayList<>();

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, playerUuid.toString());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    listings.add(extractListing(rs));
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to load expired listings", e);
            }
            return listings;
        });
    }

    @Override
    public CompletableFuture<Integer> getPlayerListingCount(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT COUNT(*) FROM auction_listings WHERE seller_uuid = ? AND status = 'ACTIVE'";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, playerUuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to get player listing count", e);
            }
            return 0;
        });
    }

    @Override
    public CompletableFuture<Void> updateListingStatus(UUID listingId, AuctionListing.ListingStatus status) {
        return CompletableFuture.runAsync(() -> {
            String sql = "UPDATE auction_listings SET status = ? WHERE listing_id = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, status.name());
                stmt.setString(2, listingId.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to update listing status", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteListing(UUID listingId) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM auction_listings WHERE listing_id = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, listingId.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to delete listing", e);
            }
        });
    }

    @Override
    public CompletableFuture<List<AuctionListing>> searchListings(String query) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM auction_listings WHERE status = 'ACTIVE' AND " +
                         "(seller_name LIKE ? OR item LIKE ?) ORDER BY expiry_time ASC LIMIT 100";
            List<AuctionListing> listings = new ArrayList<>();

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                String searchPattern = "%" + query + "%";
                stmt.setString(1, searchPattern);
                stmt.setString(2, searchPattern);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    listings.add(extractListing(rs));
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to search listings", e);
            }
            return listings;
        });
    }

    @Override
    public void wipeAll() {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement("DELETE FROM auction_listings")) {
            stmt.executeUpdate();
        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "Failed to wipe auction listings", e);
        }
    }

    @Override
    public void close() {
        // DataSource is shared, don't close it here
    }

    private AuctionListing extractListing(ResultSet rs) throws Exception {
        UUID listingId = UUID.fromString(rs.getString("listing_id"));
        UUID sellerUuid = UUID.fromString(rs.getString("seller_uuid"));
        String sellerName = rs.getString("seller_name");
        ItemStack item = deserializeItem(rs.getString("item"));
        double price = rs.getDouble("price");
        long expiryTime = rs.getLong("expiry_time");
        AuctionListing.ListingStatus status = AuctionListing.ListingStatus.valueOf(rs.getString("status"));

        return new AuctionListing(listingId, sellerUuid, sellerName, item, price, expiryTime, status);
    }

    private String serializeItem(ItemStack item) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
        dataOutput.writeObject(item);
        dataOutput.close();
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    private ItemStack deserializeItem(String data) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
        ItemStack item = (ItemStack) dataInput.readObject();
        dataInput.close();
        return item;
    }
}
