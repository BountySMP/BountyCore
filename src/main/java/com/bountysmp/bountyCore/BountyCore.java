package com.bountysmp.bountyCore;

import com.bountysmp.bountyCore.ban.BanManager;
import com.bountysmp.bountyCore.bounty.BountyManager;
import com.bountysmp.bountyCore.commands.*;
import com.bountysmp.bountyCore.commands.admin.*;
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
import com.bountysmp.bountyCore.listeners.GUIListener;
import com.bountysmp.bountyCore.listeners.MuteListener;
import com.bountysmp.bountyCore.listeners.PlayerListener;
import com.bountysmp.bountyCore.listeners.RankListener;
import com.bountysmp.bountyCore.listeners.TeleportWarmupListener;
import com.bountysmp.bountyCore.listeners.VanishListener;
import com.bountysmp.bountyCore.messaging.MessagingManager;
import com.bountysmp.bountyCore.teleport.CombatTagManager;
import com.bountysmp.bountyCore.teleport.TeleportManager;
import net.milkbowl.vault.economy.Economy;
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

    private EconomyStorage economyStorage;
    private BountyCoreEconomy economy;
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
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
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
        registerCommands();
        registerListeners();

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
}
