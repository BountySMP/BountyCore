package com.bountysmp.bountyCore.orders;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class BuyOrder {
    private final UUID orderId;
    private final UUID buyerUuid;
    private final String buyerName;
    private final ItemStack itemTemplate;
    private final double maxPrice;
    private final int quantity;
    private final long createdTime;
    private int filledQuantity;
    private OrderStatus status;

    public BuyOrder(UUID orderId, UUID buyerUuid, String buyerName, ItemStack itemTemplate,
                    double maxPrice, int quantity, long createdTime) {
        this.orderId = orderId;
        this.buyerUuid = buyerUuid;
        this.buyerName = buyerName;
        this.itemTemplate = itemTemplate;
        this.maxPrice = maxPrice;
        this.quantity = quantity;
        this.createdTime = createdTime;
        this.filledQuantity = 0;
        this.status = OrderStatus.ACTIVE;
    }

    public BuyOrder(UUID orderId, UUID buyerUuid, String buyerName, ItemStack itemTemplate,
                    double maxPrice, int quantity, long createdTime, int filledQuantity) {
        this(orderId, buyerUuid, buyerName, itemTemplate, maxPrice, quantity, createdTime);
        this.filledQuantity = filledQuantity;
    }

    public BuyOrder(UUID orderId, UUID buyerUuid, String buyerName, ItemStack itemTemplate,
                    double maxPrice, int quantity, long createdTime, int filledQuantity, OrderStatus status) {
        this(orderId, buyerUuid, buyerName, itemTemplate, maxPrice, quantity, createdTime, filledQuantity);
        this.status = status;
    }

    public UUID getOrderId() { return orderId; }
    public UUID getBuyerUuid() { return buyerUuid; }
    public String getBuyerName() { return buyerName; }
    public ItemStack getItemTemplate() { return itemTemplate.clone(); }
    public double getMaxPrice() { return maxPrice; }
    public int getQuantity() { return quantity; }
    public long getCreatedTime() { return createdTime; }
    public int getFilledQuantity() { return filledQuantity; }
    public OrderStatus getStatus() { return status; }

    public void setFilledQuantity(int filledQuantity) { this.filledQuantity = filledQuantity; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public int getRemainingQuantity() { return quantity - filledQuantity; }
    public boolean isComplete() { return filledQuantity >= quantity; }
    public boolean isActive() { return status == OrderStatus.ACTIVE && !isComplete(); }

    public boolean matches(ItemStack item) {
        if (item == null || itemTemplate == null) return false;
        return item.isSimilar(itemTemplate);
    }
}
