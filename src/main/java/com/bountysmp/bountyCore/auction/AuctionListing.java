package com.bountysmp.bountyCore.auction;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class AuctionListing {
    private final UUID listingId;
    private final UUID sellerUuid;
    private final String sellerName;
    private final ItemStack item;
    private final double price;
    private final long expiryTime;
    private ListingStatus status;

    public AuctionListing(UUID listingId, UUID sellerUuid, String sellerName, ItemStack item,
                         double price, long expiryTime, ListingStatus status) {
        this.listingId = listingId;
        this.sellerUuid = sellerUuid;
        this.sellerName = sellerName;
        this.item = item;
        this.price = price;
        this.expiryTime = expiryTime;
        this.status = status;
    }

    public UUID getListingId() {
        return listingId;
    }

    public UUID getSellerUuid() {
        return sellerUuid;
    }

    public String getSellerName() {
        return sellerName;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getPrice() {
        return price;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }

    public enum ListingStatus {
        ACTIVE,
        EXPIRED,
        SOLD
    }
}
