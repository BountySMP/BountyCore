package com.bountysmp.bountyCore.booster.storage;

import com.bountysmp.bountyCore.booster.SellBooster;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLBoosterStorage implements BoosterStorage {
    private final HikariDataSource dataSource;
    private final ConcurrentHashMap<UUID, SellBooster> cache;
    private final Logger logger;

    public MySQLBoosterStorage(HikariDataSource dataSource, Logger logger) {
        this.dataSource = dataSource;
        this.cache = new ConcurrentHashMap<>();
        this.logger = logger;
        createTables();
    }

    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS sell_boosters (" +
                     "uuid VARCHAR(36) PRIMARY KEY, " +
                     "multiplier DOUBLE NOT NULL, " +
                     "expiry_time BIGINT NOT NULL" +
                     ")";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create sell_boosters table", e);
        }
    }

    @Override
    public CompletableFuture<Void> saveBooster(SellBooster booster) {
        cache.put(booster.getPlayerUuid(), booster);

        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO sell_boosters (uuid, multiplier, expiry_time) VALUES (?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE multiplier = ?, expiry_time = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, booster.getPlayerUuid().toString());
                stmt.setDouble(2, booster.getMultiplier());
                stmt.setLong(3, booster.getExpiryTime());
                stmt.setDouble(4, booster.getMultiplier());
                stmt.setLong(5, booster.getExpiryTime());
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to save sell booster", e);
            }
        });
    }

    @Override
    public CompletableFuture<SellBooster> loadBooster(UUID playerUuid) {
        SellBooster cached = cache.get(playerUuid);
        if (cached != null && !cached.isExpired()) {
            return CompletableFuture.completedFuture(cached);
        }

        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT multiplier, expiry_time FROM sell_boosters WHERE uuid = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, playerUuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    double multiplier = rs.getDouble("multiplier");
                    long expiryTime = rs.getLong("expiry_time");
                    SellBooster booster = new SellBooster(playerUuid, multiplier, expiryTime);

                    if (!booster.isExpired()) {
                        cache.put(playerUuid, booster);
                        return booster;
                    } else {
                        deleteBooster(playerUuid);
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to load sell booster", e);
            }

            return null;
        });
    }

    @Override
    public CompletableFuture<Void> deleteBooster(UUID playerUuid) {
        cache.remove(playerUuid);

        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM sell_boosters WHERE uuid = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, playerUuid.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to delete sell booster", e);
            }
        });
    }

    @Override
    public CompletableFuture<List<SellBooster>> getAllBoosters() {
        return CompletableFuture.supplyAsync(() -> {
            List<SellBooster> boosters = new ArrayList<>();
            String sql = "SELECT uuid, multiplier, expiry_time FROM sell_boosters";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    double multiplier = rs.getDouble("multiplier");
                    long expiryTime = rs.getLong("expiry_time");
                    boosters.add(new SellBooster(uuid, multiplier, expiryTime));
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to load all sell boosters", e);
            }

            return boosters;
        });
    }

    @Override
    public void close() {
        cache.clear();
    }
}
