package com.bountysmp.bountyCore.auction.storage;

import com.bountysmp.bountyCore.auction.AuctionListing;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AuctionStorage {

    CompletableFuture<Void> saveListing(AuctionListing listing);

    CompletableFuture<AuctionListing> getListing(UUID listingId);

    CompletableFuture<List<AuctionListing>> getActiveListings();

    CompletableFuture<List<AuctionListing>> getExpiredListings(UUID playerUuid);

    CompletableFuture<Integer> getPlayerListingCount(UUID playerUuid);

    CompletableFuture<Void> updateListingStatus(UUID listingId, AuctionListing.ListingStatus status);

    CompletableFuture<Void> deleteListing(UUID listingId);

    CompletableFuture<List<AuctionListing>> searchListings(String query);

    void wipeAll();

    void close();
}
