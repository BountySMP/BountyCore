package com.bountysmp.bountyCore.auction;

import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AHClaimStore {

    private final Map<UUID, List<ItemStack>> pending = new HashMap<>();

    public void addItem(UUID player, ItemStack item) {
        pending.computeIfAbsent(player, k -> new ArrayList<>()).add(item.clone());
    }

    public List<ItemStack> getClaims(UUID player) {
        return new ArrayList<>(pending.getOrDefault(player, List.of()));
    }

    public boolean removeAt(UUID player, int index) {
        List<ItemStack> list = pending.get(player);
        if (list == null || index < 0 || index >= list.size()) return false;
        list.remove(index);
        if (list.isEmpty()) pending.remove(player);
        return true;
    }

    public void clear(UUID player) {
        pending.remove(player);
    }

    public int count(UUID player) {
        return pending.getOrDefault(player, List.of()).size();
    }
}
