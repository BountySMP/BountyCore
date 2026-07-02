package com.bountysmp.bountyCore.economy;

import com.bountysmp.bountyCore.economy.storage.EconomyStorage;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BountyCoreEconomy implements Economy {
    private final EconomyStorage storage;
    private final String currencyName;
    private final String currencyPlural;

    public BountyCoreEconomy(EconomyStorage storage, String currencyName, String currencyPlural) {
        this.storage = storage;
        this.currencyName = currencyName;
        this.currencyPlural = currencyPlural;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "BountyCoreEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        if (amount >= 1_000_000_000) {
            return abbrev(amount / 1_000_000_000, "b");
        } else if (amount >= 1_000_000) {
            return abbrev(amount / 1_000_000, "m");
        } else if (amount >= 1_000) {
            return abbrev(amount / 1_000, "k");
        } else {
            return amount == Math.floor(amount)
                    ? String.format("$%.0f", amount)
                    : String.format("$%.2f", amount);
        }
    }

    private static String abbrev(double value, String suffix) {
        double rounded = Math.round(value * 10.0) / 10.0;
        return rounded == Math.floor(rounded)
                ? String.format("$%.0f%s", rounded, suffix)
                : String.format("$%.1f%s", rounded, suffix);
    }

    @Override
    public String currencyNamePlural() {
        return currencyPlural;
    }

    @Override
    public String currencyNameSingular() {
        return currencyName;
    }

    @Override
    public boolean hasAccount(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return player != null && player.hasPlayedBefore();
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return player != null && player.getUniqueId() != null;
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return getBalance(player);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        if (player == null || player.getUniqueId() == null) {
            return 0;
        }
        return storage.loadPlayer(player.getUniqueId()).join().getBalance();
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (player == null || player.getUniqueId() == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player not found");
        }

        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative amount");
        }

        UUID uuid = player.getUniqueId();
        EconomyData data = storage.loadPlayer(uuid).join();

        if (data.getBalance() < amount) {
            return new EconomyResponse(0, data.getBalance(), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }

        data.removeBalance(amount);
        storage.savePlayer(data);

        return new EconomyResponse(amount, data.getBalance(), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (player == null || player.getUniqueId() == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player not found");
        }

        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative amount");
        }

        UUID uuid = player.getUniqueId();
        EconomyData data = storage.loadPlayer(uuid).join();
        data.addBalance(amount);
        storage.savePlayer(data);

        return new EconomyResponse(amount, data.getBalance(), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return true;
    }

    // Bank methods (not supported)
    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public List<String> getBanks() {
        return Collections.emptyList();
    }

    public EconomyStorage getStorage() {
        return storage;
    }
}
