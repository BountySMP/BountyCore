package com.bountysmp.bountyCore.orders.storage;

import com.bountysmp.bountyCore.orders.BuyOrder;
import com.bountysmp.bountyCore.orders.OrderClaim;
import com.bountysmp.bountyCore.orders.OrderStatus;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLOrderStorage implements OrderStorage {
    private final HikariDataSource dataSource;
    private final Logger logger;

    public MySQLOrderStorage(HikariDataSource dataSource, Logger logger) {
        this.dataSource = dataSource;
        this.logger = logger;
        createTables();
    }

    private void createTables() {
        String orders = "CREATE TABLE IF NOT EXISTS buy_orders (" +
                "order_id VARCHAR(36) PRIMARY KEY, " +
                "buyer_uuid VARCHAR(36) NOT NULL, " +
                "buyer_name VARCHAR(16) NOT NULL, " +
                "item_template TEXT NOT NULL, " +
                "max_price DOUBLE NOT NULL, " +
                "quantity INT NOT NULL, " +
                "filled_quantity INT NOT NULL DEFAULT 0, " +
                "status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE', " +
                "created_time BIGINT NOT NULL, " +
                "INDEX idx_buyer (buyer_uuid), " +
                "INDEX idx_status (status), " +
                "INDEX idx_created (created_time))";

        String claims = "CREATE TABLE IF NOT EXISTS order_claims (" +
                "claim_id VARCHAR(36) PRIMARY KEY, " +
                "buyer_uuid VARCHAR(36) NOT NULL, " +
                "order_id VARCHAR(36) NOT NULL, " +
                "item TEXT, " +
                "refund_amount DOUBLE NOT NULL DEFAULT 0, " +
                "created_at BIGINT NOT NULL, " +
                "INDEX idx_buyer (buyer_uuid))";

        try (Connection conn = dataSource.getConnection()) {
            conn.prepareStatement(orders).executeUpdate();
            conn.prepareStatement(claims).executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create order tables", e);
        }
    }

    // ----------------------------- Orders -----------------------------

    @Override
    public CompletableFuture<Void> saveOrder(BuyOrder order) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO buy_orders (order_id, buyer_uuid, buyer_name, item_template, " +
                    "max_price, quantity, filled_quantity, status, created_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE filled_quantity = ?, status = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement s = conn.prepareStatement(sql)) {
                s.setString(1, order.getOrderId().toString());
                s.setString(2, order.getBuyerUuid().toString());
                s.setString(3, order.getBuyerName());
                s.setString(4, serializeItem(order.getItemTemplate()));
                s.setDouble(5, order.getMaxPrice());
                s.setInt(6, order.getQuantity());
                s.setInt(7, order.getFilledQuantity());
                s.setString(8, order.getStatus().name());
                s.setLong(9, order.getCreatedTime());
                s.setInt(10, order.getFilledQuantity());
                s.setString(11, order.getStatus().name());
                s.executeUpdate();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to save order", e);
            }
        });
    }

    @Override
    public CompletableFuture<BuyOrder> getOrder(UUID orderId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM buy_orders WHERE order_id = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement s = conn.prepareStatement(sql)) {
                s.setString(1, orderId.toString());
                ResultSet rs = s.executeQuery();
                if (rs.next()) return mapOrder(rs);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to get order", e);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<List<BuyOrder>> getActiveOrders() {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM buy_orders WHERE status = 'ACTIVE' AND filled_quantity < quantity ORDER BY created_time DESC";
            List<BuyOrder> list = new ArrayList<>();
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement s = conn.prepareStatement(sql)) {
                ResultSet rs = s.executeQuery();
                while (rs.next()) list.add(mapOrder(rs));
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to get active orders", e);
            }
            return list;
        });
    }

    @Override
    public CompletableFuture<List<BuyOrder>> getPlayerOrders(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM buy_orders WHERE buyer_uuid = ? AND status = 'ACTIVE' AND filled_quantity < quantity ORDER BY created_time DESC";
            List<BuyOrder> list = new ArrayList<>();
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement s = conn.prepareStatement(sql)) {
                s.setString(1, playerUuid.toString());
                ResultSet rs = s.executeQuery();
                while (rs.next()) list.add(mapOrder(rs));
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to get player orders", e);
            }
            return list;
        });
    }

    @Override
    public CompletableFuture<Void> deleteOrder(UUID orderId) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM buy_orders WHERE order_id = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement s = conn.prepareStatement(sql)) {
                s.setString(1, orderId.toString());
                s.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to delete order", e);
            }
        });
    }

    // ----------------------------- Claims -----------------------------

    @Override
    public CompletableFuture<Void> saveClaim(OrderClaim claim) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT IGNORE INTO order_claims (claim_id, buyer_uuid, order_id, item, refund_amount, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement s = conn.prepareStatement(sql)) {
                s.setString(1, claim.getClaimId().toString());
                s.setString(2, claim.getBuyerUuid().toString());
                s.setString(3, claim.getOrderId().toString());
                s.setString(4, claim.isItemClaim() ? serializeItem(claim.getItem()) : null);
                s.setDouble(5, claim.getRefundAmount());
                s.setLong(6, claim.getCreatedAt());
                s.executeUpdate();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to save claim", e);
            }
        });
    }

    @Override
    public CompletableFuture<List<OrderClaim>> getPendingClaims(UUID buyerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM order_claims WHERE buyer_uuid = ? ORDER BY created_at ASC";
            List<OrderClaim> list = new ArrayList<>();
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement s = conn.prepareStatement(sql)) {
                s.setString(1, buyerUuid.toString());
                ResultSet rs = s.executeQuery();
                while (rs.next()) list.add(mapClaim(rs));
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to get claims", e);
            }
            return list;
        });
    }

    @Override
    public CompletableFuture<Void> deleteClaim(UUID claimId) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM order_claims WHERE claim_id = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement s = conn.prepareStatement(sql)) {
                s.setString(1, claimId.toString());
                s.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to delete claim", e);
            }
        });
    }

    @Override
    public void wipeAll() {
        try (java.sql.Connection conn = dataSource.getConnection()) {
            conn.prepareStatement("DELETE FROM buy_orders").executeUpdate();
            conn.prepareStatement("DELETE FROM order_claims").executeUpdate();
        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "Failed to wipe orders", e);
        }
    }

    @Override
    public void close() {
        // shared DataSource — do not close
    }

    // ----------------------------- Mapping -----------------------------

    private BuyOrder mapOrder(ResultSet rs) throws Exception {
        OrderStatus status = OrderStatus.ACTIVE;
        try { status = OrderStatus.valueOf(rs.getString("status")); } catch (Exception ignored) {}
        return new BuyOrder(
                UUID.fromString(rs.getString("order_id")),
                UUID.fromString(rs.getString("buyer_uuid")),
                rs.getString("buyer_name"),
                deserializeItem(rs.getString("item_template")),
                rs.getDouble("max_price"),
                rs.getInt("quantity"),
                rs.getLong("created_time"),
                rs.getInt("filled_quantity"),
                status
        );
    }

    private OrderClaim mapClaim(ResultSet rs) throws Exception {
        String itemData = rs.getString("item");
        ItemStack item = (itemData != null && !itemData.isEmpty()) ? deserializeItem(itemData) : null;
        return new OrderClaim(
                UUID.fromString(rs.getString("claim_id")),
                UUID.fromString(rs.getString("buyer_uuid")),
                UUID.fromString(rs.getString("order_id")),
                item,
                rs.getDouble("refund_amount"),
                rs.getLong("created_at")
        );
    }

    private String serializeItem(ItemStack item) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BukkitObjectOutputStream dos = new BukkitObjectOutputStream(out);
        dos.writeObject(item);
        dos.close();
        return Base64.getEncoder().encodeToString(out.toByteArray());
    }

    private ItemStack deserializeItem(String data) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(data));
        BukkitObjectInputStream dis = new BukkitObjectInputStream(in);
        ItemStack item = (ItemStack) dis.readObject();
        dis.close();
        return item;
    }
}
