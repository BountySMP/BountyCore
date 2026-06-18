package com.bountysmp.bountyCore;

import com.bountysmp.bountyCore.ban.BanManager;
import com.bountysmp.bountyCore.bounty.BountyManager;
import com.bountysmp.bountyCore.commands.*;
import com.bountysmp.bountyCore.commands.admin.*;
import com.bountysmp.bountyCore.enderchest.EnderChestManager;
import com.bountysmp.bountyCore.gamemode.GameModeManager;
import com.bountysmp.bountyCore.mute.MuteManager;
import com.bountysmp.bountyCore.ranks.RankManager;
import com.bountysmp.bountyCore.vanish.VanishManager;
import com.bountysmp.bountyCore.economy.BountyCoreEconomy;
import com.bountysmp.bountyCore.economy.storage.EconomyStorage;
import com.bountysmp.bountyCore.economy.storage.FlatFileStorage;
import com.bountysmp.bountyCore.economy.storage.MySQLStorage;
import com.bountysmp.bountyCore.homes.HomeManager;
import com.bountysmp.bountyCore.homes.gui.GUIManager;
import com.bountysmp.bountyCore.listeners.BanListener;
import com.bountysmp.bountyCore.listeners.BountyListener;
import com.bountysmp.bountyCore.listeners.CombatListener;
import com.bountysmp.bountyCore.listeners.DisabledCommandListener;
import com.bountysmp.bountyCore.listeners.EnderChestListener;
import com.bountysmp.bountyCore.listeners.GUIListener;
import com.bountysmp.bountyCore.listeners.MuteListener;
import com.bountysmp.bountyCore.listeners.PlayerListener;
import com.bountysmp.bountyCore.listeners.RankListener;
import com.bountysmp.bountyCore.listeners.TabListener;
import com.bountysmp.bountyCore.listeners.TeleportWarmupListener;
import com.bountysmp.bountyCore.listeners.VanishListener;
import com.bountysmp.bountyCore.messaging.MessagingManager;
import com.bountysmp.bountyCore.tab.NametagManager;
import com.bountysmp.bountyCore.tab.TabManager;
import com.bountysmp.bountyCore.teleport.CombatTagManager;
import com.bountysmp.bountyCore.teleport.TeleportManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

public final class BountyCore extends JavaPlugin {

    private static BountyCore instance;

    private EconomyStorage economyStorage;
    private BountyCoreEconomy economy;
    private com.zaxxer.hikari.HikariDataSource sharedDataSource;
    private HomeManager homeManager;
    private GUIManager guiManager;
    private TeleportManager teleportManager;
    private CombatTagManager combatTagManager;
    private MessagingManager messagingManager;
    private BountyManager bountyManager;
    private MuteManager muteManager;
    private BanManager banManager;
    private RankManager rankManager;
    private VanishManager vanishManager;
    private EnderChestManager enderChestManager;
    private GameModeManager gameModeManager;
    private TabManager tabManager;
    private NametagManager nametagManager;
    private RandomTpCommand randomTpCommand;
    private FileConfiguration messagesConfig;
    private com.bountysmp.bountyCore.auction.AuctionManager auctionManager;
    private com.bountysmp.bountyCore.orders.OrderManager orderManager;
    private com.bountysmp.bountyCore.teams.TeamManager teamManager;
    private com.bountysmp.bountyCore.shop.ShopManager shopManager;
    private com.bountysmp.bountyCore.booster.SellBoosterManager sellBoosterManager;
    private com.bountysmp.bountyCore.sell.SellManager sellManager;
    private com.bountysmp.bountyCore.warp.WarpManager warpManager;
    private com.bountysmp.bountyCore.stats.PlayerStatsManager playerStatsManager;
    private com.bountysmp.bountyCore.clearlag.ClearLagManager clearLagManager;
    private com.bountysmp.bountyCore.keyall.KeyAllManager keyAllManager;
    private com.bountysmp.bountyCore.settings.SettingsManager settingsManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        loadMessagesConfig();

        if (!setupEconomy()) {
            getLogger().severe("Failed to setup economy! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        setupHomes();
        setupTeleport();
        setupMessaging();
        setupBounty();
        setupMute();
        setupBan();
        setupRanks();
        setupVanish();
        setupEnderChest();
        setupGameMode();
        setupTabAndNametags();
        setupAuction();
        setupOrders();
        setupTeams();
        setupShop();
        setupSell();
        setupSettings();
        setupWarps();
        setupPlayerStats();
        setupClearLag();
        setupKeyAll();
        registerCommands();
        registerListeners();
        startScheduledTasks();
        registerPlaceholderAPI();

        getLogger().info("BountyCore has been enabled!");
    }

    @Override
    public void onDisable() {
        if (economyStorage != null) {
            economyStorage.close();
        }

        if (homeManager != null) {
            homeManager.close();
        }

        if (vanishManager != null) {
            vanishManager.shutdown();
        }

        if (enderChestManager != null) {
            enderChestManager.close();
        }

        if (gameModeManager != null) {
            gameModeManager.close();
        }

        if (nametagManager != null) {
            nametagManager.shutdown();
        }

        if (auctionManager != null) {
            auctionManager.close();
        }

        if (orderManager != null) {
            orderManager.close();
        }

        if (teamManager != null) {
            teamManager.close();
        }

        if (settingsManager != null) {
            settingsManager.close();
        }

        if (sellBoosterManager != null) {
            sellBoosterManager.close();
        }

        if (warpManager != null) {
            warpManager.close();
        }

        if (playerStatsManager != null) {
            playerStatsManager.close();
        }

        if (clearLagManager != null) {
            clearLagManager.shutdown();
        }

        getLogger().info("BountyCore has been disabled!");
    }

    private boolean setupEconomy() {
        String storageType = getConfig().getString("economy.storage-type", "FLATFILE");
        double startingBalance = getConfig().getDouble("economy.starting-balance", 1000.0);
        String currencyName = getConfig().getString("economy.currency-name", "Coin");
        String currencyPlural = getConfig().getString("economy.currency-plural", "Coins");

        try {
            if (storageType.equalsIgnoreCase("MYSQL")) {
                String host = getConfig().getString("economy.mysql.host", "localhost");
                int port = getConfig().getInt("economy.mysql.port", 3306);
                String database = getConfig().getString("economy.mysql.database", "bountycore");
                String username = getConfig().getString("economy.mysql.username", "root");
                String password = getConfig().getString("economy.mysql.password", "");
                int poolSize = getConfig().getInt("economy.mysql.pool-size", 10);

                economyStorage = new MySQLStorage(host, port, database, username, password, poolSize, startingBalance, getLogger());
                sharedDataSource = ((MySQLStorage) economyStorage).getDataSource();
                getLogger().info("Using MySQL storage for economy");
            } else {
                economyStorage = new FlatFileStorage(getDataFolder(), startingBalance, getLogger());
                getLogger().info("Using FlatFile storage for economy");
            }

            economy = new BountyCoreEconomy(economyStorage, currencyName, currencyPlural);

            getServer().getServicesManager().register(
                Economy.class,
                economy,
                this,
                ServicePriority.Highest
            );

            getLogger().info("Economy provider registered with Vault!");
            return true;

        } catch (Exception e) {
            getLogger().severe("Failed to initialize economy storage: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void setupHomes() {
        homeManager = new HomeManager(this);
        guiManager = new GUIManager();
        getLogger().info("Homes system initialized!");
    }

    private void setupTeleport() {
        teleportManager = new TeleportManager(this);
        combatTagManager = new CombatTagManager(this);
        getLogger().info("Teleportation system initialized!");
    }

    private void setupMessaging() {
        messagingManager = new MessagingManager();
        getLogger().info("Messaging system initialized!");
    }

    private void setupBounty() {
        bountyManager = new BountyManager(this);
        getLogger().info("Bounty system initialized!");
    }

    private void setupMute() {
        muteManager = new MuteManager(this);
        getLogger().info("Mute system initialized!");
    }

    private void setupBan() {
        banManager = new BanManager(this);
        getLogger().info("Ban system initialized!");
    }

    private void setupRanks() {
        rankManager = new RankManager(this);
        getLogger().info("Rank system initialized!");
    }

    private void setupVanish() {
        vanishManager = new VanishManager(this);
        getLogger().info("Vanish system initialized!");
    }

    private void setupEnderChest() {
        enderChestManager = new EnderChestManager(this);
        getLogger().info("EnderChest system initialized!");
    }

    private void setupGameMode() {
        gameModeManager = new GameModeManager(this);
        getLogger().info("GameMode manager initialized!");
    }

    private void setupTabAndNametags() {
        tabManager = new TabManager(this);
        nametagManager = new NametagManager(this);

        // Update all online players on enable
        tabManager.updateAll();
        nametagManager.updateAll();

        getLogger().info("Tab and nametag systems initialized!");
    }

    private void setupAuction() {
        auctionManager = new com.bountysmp.bountyCore.auction.AuctionManager(this);
        getLogger().info("Auction system initialized!");
    }

    private void setupOrders() {
        orderManager = new com.bountysmp.bountyCore.orders.OrderManager(this);
        getLogger().info("Orders system initialized!");
    }

    private void setupTeams() {
        teamManager = new com.bountysmp.bountyCore.teams.TeamManager(this);
        getLogger().info("Team system initialized!");
    }

    private void setupShop() {
        shopManager = new com.bountysmp.bountyCore.shop.ShopManager(this);
        getLogger().info("Shop system initialized!");
    }

    private void setupSell() {
        sellManager = new com.bountysmp.bountyCore.sell.SellManager(this);
        sellBoosterManager = new com.bountysmp.bountyCore.booster.SellBoosterManager(this);
        getLogger().info("Sell system initialized!");
    }

    private void setupSettings() {
        settingsManager = new com.bountysmp.bountyCore.settings.SettingsManager(this);
        getLogger().info("Settings system initialized!");
    }

    private void setupWarps() {
        warpManager = new com.bountysmp.bountyCore.warp.WarpManager(this);
        getLogger().info("Warp system initialized!");
    }

    private void setupPlayerStats() {
        playerStatsManager = new com.bountysmp.bountyCore.stats.PlayerStatsManager(this);
        getLogger().info("Player stats system initialized!");
    }

    private void setupClearLag() {
        clearLagManager = new com.bountysmp.bountyCore.clearlag.ClearLagManager(this);
        getLogger().info("ClearLag system initialized!");
    }

    private void setupKeyAll() {
        keyAllManager = new com.bountysmp.bountyCore.keyall.KeyAllManager(this);
        getLogger().info("KeyAll system initialized!");
    }

    private void startScheduledTasks() {
        // Sell booster expiry check every minute
        getServer().getScheduler().runTaskTimer(this, () -> {
            if (sellBoosterManager != null) {
                sellBoosterManager.checkExpiredBoosters();
            }
        }, 20 * 60, 20 * 60);

        // Auction listing expiry check every 5 minutes
        getServer().getScheduler().runTaskTimer(this, () -> {
            if (auctionManager != null) {
                auctionManager.expireListings();
            }
        }, 20 * 60 * 5, 20 * 60 * 5);

        getLogger().info("Scheduled tasks started!");
    }

    private void registerPlaceholderAPI() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                new com.bountysmp.bountyCore.papi.BountyCoreExpansion(this).register();
                getLogger().info("PlaceholderAPI expansion registered!");
            } catch (Exception e) {
                getLogger().warning("Failed to register PlaceholderAPI expansion: " + e.getMessage());
            }
        }
    }

    private void loadMessagesConfig() {
        File messagesFile = new File(getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void registerCommands() {
        BalanceCommand balanceCmd = new BalanceCommand(this);
        getCommand("balance").setExecutor(balanceCmd);
        getCommand("balance").setTabCompleter(balanceCmd);

        PayCommand payCmd = new PayCommand(this);
        getCommand("pay").setExecutor(payCmd);
        getCommand("pay").setTabCompleter(payCmd);

        getCommand("home").setExecutor(new HomeCommand(this));
        getCommand("sethome").setExecutor(new SetHomeCommand(this));

        DelHomeCommand delHomeCmd = new DelHomeCommand(this);
        getCommand("delhome").setExecutor(delHomeCmd);
        getCommand("delhome").setTabCompleter(delHomeCmd);

        TpaCommand tpaCmd = new TpaCommand(this);
        getCommand("tpa").setExecutor(tpaCmd);
        getCommand("tpa").setTabCompleter(tpaCmd);

        TpaHereCommand tpaHereCmd = new TpaHereCommand(this);
        getCommand("tpahere").setExecutor(tpaHereCmd);
        getCommand("tpahere").setTabCompleter(tpaHereCmd);

        getCommand("tpaccept").setExecutor(new TpAcceptCommand(this));
        getCommand("tpdeny").setExecutor(new TpDenyCommand(this));
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
        getCommand("back").setExecutor(new BackCommand(this));

        MsgCommand msgCmd = new MsgCommand(this);
        getCommand("msg").setExecutor(msgCmd);
        getCommand("msg").setTabCompleter(msgCmd);

        getCommand("r").setExecutor(new ReplyCommand(this));

        BountyCommand bountyCmd = new BountyCommand(this);
        getCommand("bounty").setExecutor(bountyCmd);
        getCommand("bounty").setTabCompleter(bountyCmd);

        EcoCommand ecoCmd = new EcoCommand(this);
        getCommand("eco").setExecutor(ecoCmd);
        getCommand("eco").setTabCompleter(ecoCmd);

        // Admin commands
        TpHereStaffCommand tphereCmd = new TpHereStaffCommand(this);
        getCommand("tphere").setExecutor(tphereCmd);
        getCommand("tphere").setTabCompleter(tphereCmd);

        TpStaffCommand tpStaffCmd = new TpStaffCommand(this);
        getCommand("tp").setExecutor(tpStaffCmd);
        getCommand("tp").setTabCompleter(tpStaffCmd);

        KickCommand kickCmd = new KickCommand(this);
        getCommand("kick").setExecutor(kickCmd);
        getCommand("kick").setTabCompleter(kickCmd);

        BanCommand banCmd = new BanCommand(this);
        getCommand("ban").setExecutor(banCmd);
        getCommand("ban").setTabCompleter(banCmd);

        TempBanCommand tempbanCmd = new TempBanCommand(this);
        getCommand("tempban").setExecutor(tempbanCmd);
        getCommand("tempban").setTabCompleter(tempbanCmd);

        UnbanCommand unbanCmd = new UnbanCommand(this);
        getCommand("unban").setExecutor(unbanCmd);
        getCommand("unban").setTabCompleter(unbanCmd);

        SummonCommand summonCmd = new SummonCommand(this);
        getCommand("s").setExecutor(summonCmd);
        getCommand("s").setTabCompleter(summonCmd);

        MuteCommand muteCmd = new MuteCommand(this);
        getCommand("mute").setExecutor(muteCmd);
        getCommand("mute").setTabCompleter(muteCmd);

        TempMuteCommand tempmuteCmd = new TempMuteCommand(this);
        getCommand("tempmute").setExecutor(tempmuteCmd);
        getCommand("tempmute").setTabCompleter(tempmuteCmd);

        UnmuteCommand unmuteCmd = new UnmuteCommand(this);
        getCommand("unmute").setExecutor(unmuteCmd);
        getCommand("unmute").setTabCompleter(unmuteCmd);

        ClearBountyCommand clearBountyCmd = new ClearBountyCommand(this);
        getCommand("clearbounty").setExecutor(clearBountyCmd);
        getCommand("clearbounty").setTabCompleter(clearBountyCmd);

        SetBountyCommand setBountyCmd = new SetBountyCommand(this);
        getCommand("setbounty").setExecutor(setBountyCmd);
        getCommand("setbounty").setTabCompleter(setBountyCmd);

        PaddCommand paddCmd = new PaddCommand(this);
        getCommand("padd").setExecutor(paddCmd);
        getCommand("padd").setTabCompleter(paddCmd);

        PremCommand premCmd = new PremCommand(this);
        getCommand("prem").setExecutor(premCmd);
        getCommand("prem").setTabCompleter(premCmd);

        PinfoCommand pinfoCmd = new PinfoCommand(this);
        getCommand("pinfo").setExecutor(pinfoCmd);
        getCommand("pinfo").setTabCompleter(pinfoCmd);

        PsearchCommand psearchCmd = new PsearchCommand(this);
        getCommand("psearch").setExecutor(psearchCmd);
        getCommand("psearch").setTabCompleter(psearchCmd);

        getCommand("vanish").setExecutor(new VanishCommand(this));
        getCommand("god").setExecutor(new GodCommand(this));

        FlyCommand flyCmd = new FlyCommand(this);
        getCommand("fly").setExecutor(flyCmd);
        getCommand("fly").setTabCompleter(flyCmd);

        GamemodeSurvivalCommand gmsCmd = new GamemodeSurvivalCommand(this);
        getCommand("gms").setExecutor(gmsCmd);
        getCommand("gms").setTabCompleter(gmsCmd);

        GamemodeCreativeCommand gmcCmd = new GamemodeCreativeCommand(this);
        getCommand("gmc").setExecutor(gmcCmd);
        getCommand("gmc").setTabCompleter(gmcCmd);

        GamemodeSpectatorCommand gmspCmd = new GamemodeSpectatorCommand(this);
        getCommand("gmsp").setExecutor(gmspCmd);
        getCommand("gmsp").setTabCompleter(gmspCmd);

        randomTpCommand = new RandomTpCommand(this);
        getCommand("randomtp").setExecutor(randomTpCommand);
        getCommand("rtp").setExecutor(randomTpCommand);

        // Auction House
        getCommand("ah").setExecutor(new com.bountysmp.bountyCore.commands.AhCommand(this));

        // Orders
        getCommand("orders").setExecutor(new com.bountysmp.bountyCore.commands.OrdersCommand(this));

        // Team
        com.bountysmp.bountyCore.commands.TeamCommand teamCmd = new com.bountysmp.bountyCore.commands.TeamCommand(this);
        getCommand("team").setExecutor(teamCmd);
        getCommand("team").setTabCompleter(teamCmd);

        // Settings
        getCommand("settings").setExecutor(new com.bountysmp.bountyCore.commands.SettingsCommand(this));

        // Shop
        getCommand("shop").setExecutor(new com.bountysmp.bountyCore.commands.ShopCommand(this));

        // Sell
        com.bountysmp.bountyCore.commands.SellCommand sellCmd = new com.bountysmp.bountyCore.commands.SellCommand(this);
        getCommand("sell").setExecutor(sellCmd);
        getCommand("sell").setTabCompleter(sellCmd);

        // New commands
        getCommand("booster").setExecutor(new com.bountysmp.bountyCore.commands.BoosterCommand(this));

        com.bountysmp.bountyCore.commands.admin.SellBoosterCommand sellBoosterCmd = new com.bountysmp.bountyCore.commands.admin.SellBoosterCommand(this);
        getCommand("sellbooster").setExecutor(sellBoosterCmd);
        getCommand("sellbooster").setTabCompleter(sellBoosterCmd);

        com.bountysmp.bountyCore.commands.WarpCommand warpCmd = new com.bountysmp.bountyCore.commands.WarpCommand(this);
        getCommand("warp").setExecutor(warpCmd);
        getCommand("warp").setTabCompleter(warpCmd);

        com.bountysmp.bountyCore.commands.admin.WarpManagerCommand warpMgrCmd = new com.bountysmp.bountyCore.commands.admin.WarpManagerCommand(this);
        getCommand("warpmanager").setExecutor(warpMgrCmd);
        getCommand("warpmanager").setTabCompleter(warpMgrCmd);

        com.bountysmp.bountyCore.commands.admin.ProfileCommand profileCmd = new com.bountysmp.bountyCore.commands.admin.ProfileCommand(this);
        getCommand("profile").setExecutor(profileCmd);
        getCommand("profile").setTabCompleter(profileCmd);

        com.bountysmp.bountyCore.commands.admin.ClearLagCommand clearLagCmd = new com.bountysmp.bountyCore.commands.admin.ClearLagCommand(this);
        getCommand("clearlag").setExecutor(clearLagCmd);
        getCommand("clearlag").setTabCompleter(clearLagCmd);

        getCommand("statswipe").setExecutor(new com.bountysmp.bountyCore.commands.admin.StatsWipeCommand(this));

        com.bountysmp.bountyCore.commands.admin.KeyAllCommand keyAllCmd = new com.bountysmp.bountyCore.commands.admin.KeyAllCommand(this);
        getCommand("keyall").setExecutor(keyAllCmd);
        getCommand("keyall").setTabCompleter(keyAllCmd);

        getCommand("serverinfo").setExecutor(new com.bountysmp.bountyCore.commands.InfoCommand(this));
        getCommand("rules").setExecutor(new com.bountysmp.bountyCore.commands.RulesCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new TeleportWarmupListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new BountyListener(this), this);
        getServer().getPluginManager().registerEvents(new DisabledCommandListener(this), this);
        getServer().getPluginManager().registerEvents(new MuteListener(this), this);
        getServer().getPluginManager().registerEvents(new BanListener(this), this);
        getServer().getPluginManager().registerEvents(new RankListener(this), this);
        getServer().getPluginManager().registerEvents(new VanishListener(this), this);
        getServer().getPluginManager().registerEvents(new EnderChestListener(this, enderChestManager), this);
        getServer().getPluginManager().registerEvents(new TabListener(this), this);
        getServer().getPluginManager().registerEvents(new com.bountysmp.bountyCore.listeners.FastCrystalListener(this), this);
        getServer().getPluginManager().registerEvents(new com.bountysmp.bountyCore.listeners.ChatListener(this), this);
    }

    public static BountyCore getInstance() {
        return instance;
    }

    public BountyCoreEconomy getEconomy() {
        return economy;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public CombatTagManager getCombatTagManager() {
        return combatTagManager;
    }

    public MessagingManager getMessagingManager() {
        return messagingManager;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public String getMessage(String path, Object... replacements) {
        String message = messagesConfig.getString(path);
        if (message == null) {
            return "§cMessage not found: " + path;
        }

        // Replace color codes
        message = ChatColor.translateAlternateColorCodes('&', message);

        // Replace placeholders in pairs (key, value)
        for (int i = 0; i < replacements.length - 1; i += 2) {
            String placeholder = "{" + replacements[i] + "}";
            String value = String.valueOf(replacements[i + 1]);
            message = message.replace(placeholder, value);
        }

        return message;
    }

    public BountyManager getBountyManager() {
        return bountyManager;
    }

    public MuteManager getMuteManager() {
        return muteManager;
    }

    public BanManager getBanManager() {
        return banManager;
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    public VanishManager getVanishManager() {
        return vanishManager;
    }

    public EnderChestManager getEnderChestManager() {
        return enderChestManager;
    }

    public GameModeManager getGameModeManager() {
        return gameModeManager;
    }

    public RandomTpCommand getRandomTpCommand() {
        return randomTpCommand;
    }

    public TabManager getTabManager() {
        return tabManager;
    }

    public NametagManager getNametagManager() {
        return nametagManager;
    }

    public com.zaxxer.hikari.HikariDataSource getSharedDataSource() {
        return sharedDataSource;
    }

    public com.bountysmp.bountyCore.auction.AuctionManager getAuctionManager() {
        return auctionManager;
    }

    public com.bountysmp.bountyCore.orders.OrderManager getOrderManager() {
        return orderManager;
    }

    public com.bountysmp.bountyCore.teams.TeamManager getTeamManager() {
        return teamManager;
    }

    public com.bountysmp.bountyCore.shop.ShopManager getShopManager() {
        return shopManager;
    }

    public com.bountysmp.bountyCore.booster.SellBoosterManager getSellBoosterManager() {
        return sellBoosterManager;
    }

    public com.bountysmp.bountyCore.sell.SellManager getSellManager() {
        return sellManager;
    }

    public com.bountysmp.bountyCore.warp.WarpManager getWarpManager() {
        return warpManager;
    }

    public com.bountysmp.bountyCore.stats.PlayerStatsManager getPlayerStatsManager() {
        return playerStatsManager;
    }

    public com.bountysmp.bountyCore.clearlag.ClearLagManager getClearLagManager() {
        return clearLagManager;
    }

    public com.bountysmp.bountyCore.keyall.KeyAllManager getKeyAllManager() {
        return keyAllManager;
    }

    public com.bountysmp.bountyCore.settings.SettingsManager getSettingsManager() {
        return settingsManager;
    }
}
