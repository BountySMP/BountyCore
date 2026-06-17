package com.bountysmp.bountyCore.stats.storage;

import com.bountysmp.bountyCore.stats.PlayerStats;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLPlayerStatsStorage implements PlayerStatsStorage {
    private final HikariDataSource dataSource;
    private final Logger logger;

    public MySQLPlayerStatsStorage(HikariDataSource dataSource, Logger logger) {
        this.dataSource = dataSource;
        this.logger = logger;
        createTables();
    }

    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS player_stats (" +
                     "uuid VARCHAR(36) PRIMARY KEY, " +
                     "kills INT NOT NULL DEFAULT 0, " +
                     "deaths INT NOT NULL DEFAULT 0, " +
                     "playtime_millis BIGINT NOT NULL DEFAULT 0" +
                     ")";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create player_stats table", e);
        }
    }

    @Override
    public CompletableFuture<PlayerStats> loadStats(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM player_stats WHERE uuid = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, playerUuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int kills = rs.getInt("kills");
                    int deaths = rs.getInt("deaths");
                    long playtime = rs.getLong("playtime_millis");
                    return new PlayerStats(playerUuid, kills, deaths, playtime);
                } else {
                    return new PlayerStats(playerUuid, 0, 0, 0);
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to load player stats", e);
                return new PlayerStats(playerUuid, 0, 0, 0);
            }
        });
    }

    @Override
    public CompletableFuture<Void> saveStats(PlayerStats stats) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO player_stats (uuid, kills, deaths, playtime_millis) VALUES (?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE kills = ?, deaths = ?, playtime_millis = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, stats.getPlayerUuid().toString());
                stmt.setInt(2, stats.getKills());
                stmt.setInt(3, stats.getDeaths());
                stmt.setLong(4, stats.getPlaytimeMillis());
                stmt.setInt(5, stats.getKills());
                stmt.setInt(6, stats.getDeaths());
                stmt.setLong(7, stats.getPlaytimeMillis());

                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to save player stats", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteStats(UUID playerUuid) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM player_stats WHERE uuid = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, playerUuid.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to delete player stats", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteAllStats() {
        return CompletableFuture.runAsync(() -> {
            String sql = "TRUNCATE TABLE player_stats";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to delete all player stats", e);
            }
        });
    }

    @Override
    public void close() {
    }
}
