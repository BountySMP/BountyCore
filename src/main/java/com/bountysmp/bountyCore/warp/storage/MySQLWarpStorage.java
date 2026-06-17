package com.bountysmp.bountyCore.warp.storage;

import com.bountysmp.bountyCore.warp.Warp;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLWarpStorage implements WarpStorage {
    private final HikariDataSource dataSource;
    private final Logger logger;

    public MySQLWarpStorage(HikariDataSource dataSource, Logger logger) {
        this.dataSource = dataSource;
        this.logger = logger;
        createTables();
    }

    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS warps (" +
                     "name VARCHAR(32) PRIMARY KEY, " +
                     "world VARCHAR(64) NOT NULL, " +
                     "x DOUBLE NOT NULL, " +
                     "y DOUBLE NOT NULL, " +
                     "z DOUBLE NOT NULL, " +
                     "yaw FLOAT NOT NULL, " +
                     "pitch FLOAT NOT NULL, " +
                     "creator_uuid VARCHAR(36) NOT NULL, " +
                     "icon_material VARCHAR(64) NOT NULL, " +
                     "creation_time BIGINT NOT NULL" +
                     ")";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create warps table", e);
        }
    }

    @Override
    public CompletableFuture<Void> saveWarp(Warp warp) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO warps (name, world, x, y, z, yaw, pitch, creator_uuid, icon_material, creation_time) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ?, icon_material = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                Location loc = warp.getLocation();
                stmt.setString(1, warp.getName());
                stmt.setString(2, loc.getWorld().getName());
                stmt.setDouble(3, loc.getX());
                stmt.setDouble(4, loc.getY());
                stmt.setDouble(5, loc.getZ());
                stmt.setFloat(6, loc.getYaw());
                stmt.setFloat(7, loc.getPitch());
                stmt.setString(8, warp.getCreatorUuid().toString());
                stmt.setString(9, warp.getIconMaterial().name());
                stmt.setLong(10, warp.getCreationTime());
                stmt.setString(11, loc.getWorld().getName());
                stmt.setDouble(12, loc.getX());
                stmt.setDouble(13, loc.getY());
                stmt.setDouble(14, loc.getZ());
                stmt.setFloat(15, loc.getYaw());
                stmt.setFloat(16, loc.getPitch());
                stmt.setString(17, warp.getIconMaterial().name());

                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to save warp", e);
            }
        });
    }

    @Override
    public CompletableFuture<Warp> getWarp(String name) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM warps WHERE name = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, name);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return extractWarp(rs);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to load warp", e);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<List<Warp>> getAllWarps() {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM warps ORDER BY name ASC";
            List<Warp> warps = new ArrayList<>();

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    warps.add(extractWarp(rs));
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to load warps", e);
            }
            return warps;
        });
    }

    @Override
    public CompletableFuture<Void> deleteWarp(String name) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM warps WHERE name = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, name);
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to delete warp", e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> warpExists(String name) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT 1 FROM warps WHERE name = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, name);
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to check warp existence", e);
                return false;
            }
        });
    }

    @Override
    public void close() {
    }

    private Warp extractWarp(ResultSet rs) throws SQLException {
        String name = rs.getString("name");
        String worldName = rs.getString("world");
        double x = rs.getDouble("x");
        double y = rs.getDouble("y");
        double z = rs.getDouble("z");
        float yaw = rs.getFloat("yaw");
        float pitch = rs.getFloat("pitch");
        UUID creatorUuid = UUID.fromString(rs.getString("creator_uuid"));
        Material icon = Material.valueOf(rs.getString("icon_material"));
        long creationTime = rs.getLong("creation_time");

        Location location = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        return new Warp(name, location, creatorUuid, icon, creationTime);
    }
}
