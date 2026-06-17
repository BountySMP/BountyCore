package com.bountysmp.bountyCore.economy.storage;

import com.bountysmp.bountyCore.economy.EconomyData;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLStorage implements EconomyStorage {
    private final HikariDataSource dataSource;
    private final ConcurrentHashMap<UUID, EconomyData> cache;
    private final double startingBalance;
    private final Logger logger;

    public MySQLStorage(String host, int port, String database, String username, String password, int poolSize, double startingBalance, Logger logger) {
        this.cache = new ConcurrentHashMap<>();
        this.startingBalance = startingBalance;
        this.logger = logger;

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
        createTables();
    }

    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS economy (" +
                     "uuid VARCHAR(36) PRIMARY KEY, " +
                     "balance DOUBLE NOT NULL DEFAULT 0" +
                     ")";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create economy table", e);
        }
    }

    @Override
    public CompletableFuture<EconomyData> loadPlayer(UUID uuid) {
        EconomyData cached = cache.get(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT balance FROM economy WHERE uuid = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    double balance = rs.getDouble("balance");
                    EconomyData data = new EconomyData(uuid, balance);
                    cache.put(uuid, data);
                    return data;
                } else {
                    EconomyData data = new EconomyData(uuid, startingBalance);
                    cache.put(uuid, data);
                    savePlayer(data);
                    return data;
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to load player economy data", e);
                EconomyData data = new EconomyData(uuid, startingBalance);
                cache.put(uuid, data);
                return data;
            }
        });
    }

    @Override
    public CompletableFuture<Void> savePlayer(EconomyData data) {
        cache.put(data.getUuid(), data);

        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO economy (uuid, balance) VALUES (?, ?) " +
                         "ON DUPLICATE KEY UPDATE balance = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, data.getUuid().toString());
                stmt.setDouble(2, data.getBalance());
                stmt.setDouble(3, data.getBalance());
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to save player economy data", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> saveAll() {
        return CompletableFuture.allOf(
            cache.values().stream()
                .map(this::savePlayer)
                .toArray(CompletableFuture[]::new)
        );
    }

    @Override
    public void close() {
        saveAll().join();
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }
}
