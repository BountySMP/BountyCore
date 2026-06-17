package com.bountysmp.bountyCore.economy;

import java.util.UUID;

public class EconomyData {
    private final UUID uuid;
    private double balance;

    public EconomyData(UUID uuid, double balance) {
        this.uuid = uuid;
        this.balance = balance;
    }

    public UUID getUuid() {
        return uuid;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = Math.max(0, balance);
    }

    public void addBalance(double amount) {
        this.balance += amount;
    }

    public boolean removeBalance(double amount) {
        if (this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }
}
