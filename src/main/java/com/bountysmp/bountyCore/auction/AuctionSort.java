package com.bountysmp.bountyCore.auction;

import java.util.Comparator;
import java.util.List;

public enum AuctionSort {
    PRICE_LOW("Price: Low → High"),
    PRICE_HIGH("Price: High → Low"),
    NEWEST("Newest First"),
    OLDEST("Oldest First");

    private final String display;

    AuctionSort(String display) {
        this.display = display;
    }

    public String displayName() {
        return display;
    }

    public AuctionSort next() {
        AuctionSort[] vals = values();
        return vals[(ordinal() + 1) % vals.length];
    }

    public List<AuctionListing> apply(List<AuctionListing> listings) {
        return switch (this) {
            case PRICE_LOW  -> listings.stream().sorted(Comparator.comparingDouble(AuctionListing::getPrice)).toList();
            case PRICE_HIGH -> listings.stream().sorted(Comparator.comparingDouble(AuctionListing::getPrice).reversed()).toList();
            case NEWEST     -> listings.stream().sorted(Comparator.comparingLong(AuctionListing::getExpiryTime).reversed()).toList();
            case OLDEST     -> listings.stream().sorted(Comparator.comparingLong(AuctionListing::getExpiryTime)).toList();
        };
    }
}
