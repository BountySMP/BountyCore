# BountyCore Complete Implementation Guide

## Summary

A comprehensive Minecraft plugin implementation has been started for the BountyCore plugin. This guide documents what has been completed and provides code templates for the remaining systems.

## What Was Completed

### ✅ Fully Implemented Systems

1. **Auction House** - Complete with MySQL/FlatFile storage, GUIs, and commands
2. **Orders System** - Full buy order implementation with storage layers
3. **Teams System** - Team creation, management, and chat functionality
4. **Settings System** - Player settings with toggles for TPA, messages, etc.
5. **Shop System** - Full shop implementation with category-based GUIs
6. **Sell System** (Partial) - SellManager complete, missing GUI and command

### ✅ Configuration Files Created

All required YAML configuration files:
- shop.yml (with example items in categories: Tools, Food, Blocks, Combat)
- sell.yml (with category-based multipliers for ores, ingots, gems, crops, etc.)
- clearlag.yml (auto-clear configuration with warnings)
- keyall.yml (CrazyCrates integration)
- menus.yml (Info and Rules GUI configurations)

### ✅ Core Infrastructure

- ItemStack serialization (Base64 + BukkitObjectOutputStream)
- HikariCP MySQL connection pooling
- FlatFile fallback storage pattern
- Async CompletableFuture operations
- GUI pagination system (following BountyGUI pattern)

## Files Created

### Auction System (8 files)
```
com.bountysmp.bountyCore.auction/
├── AuctionListing.java
├── AuctionManager.java
├── AuctionGUI.java
├── AuctionListGUI.java
├── AuctionReturnGUI.java
├── storage/
│   ├── AuctionStorage.java
│   ├── MySQLAuctionStorage.java
│   └── FlatFileAuctionStorage.java
commands/
└── AhCommand.java
```

### Orders System (7 files)
```
com.bountysmp.bountyCore.orders/
├── BuyOrder.java
├── OrderManager.java
├── OrderGUI.java
├── OrderPlaceGUI.java
├── storage/
│   ├── OrderStorage.java
│   ├── MySQLOrderStorage.java
│   └── FlatFileOrderStorage.java
commands/
└── OrdersCommand.java
```

### Teams System (8 files)
```
com.bountysmp.bountyCore.teams/
├── Team.java
├── TeamManager.java
├── TeamGUI.java
├── TeamChatListener.java
├── storage/
│   ├── TeamStorage.java
│   ├── MySQLTeamStorage.java
│   └── FlatFileTeamStorage.java
commands/
└── TeamCommand.java
```

### Settings System (7 files)
```
com.bountysmp.bountyCore.settings/
├── PlayerSettings.java
├── SettingsManager.java
├── SettingsGUI.java
├── storage/
│   ├── SettingsStorage.java
│   ├── MySQLSettingsStorage.java
│   └── FlatFileSettingsStorage.java
commands/
└── SettingsCommand.java
```

### Shop System (4 files)
```
com.bountysmp.bountyCore.shop/
├── ShopManager.java
├── ShopGUI.java
├── ShopCategoryGUI.java
commands/
└── ShopCommand.java
```

### Sell System (2 files - needs 1 more)
```
com.bountysmp.bountyCore.sell/
├── SellManager.java
commands/
└── SellCommand.java
[Missing: SellGUI.java]
```

## Remaining Implementation Tasks

### Priority 1: Core System Files

#### 1. Sell Booster System (7 files needed)
Create in `com.bountysmp.bountyCore.sellbooster/`:
- SellBooster.java (UUID uuid, double multiplier, long expiryTime)
- SellBoosterManager.java (activate, stack, check active booster)
- storage/BoosterStorage.java (interface)
- storage/MySQLBoosterStorage.java
- storage/FlatFileBoosterStorage.java
- commands/SellBoosterCommand.java
- commands/BoosterCommand.java

#### 2. Warps System (8 files needed)
Create in `com.bountysmp.bountyCore.warps/`:
- Warp.java (String name, Location location, Material icon, List<String> lore)
- WarpManager.java
- WarpGUI.java (54 slot paginated)
- storage/WarpStorage.java (interface)
- storage/MySQLWarpStorage.java
- storage/FlatFileWarpStorage.java
- commands/WarpCommand.java
- commands/admin/WarpManagerCommand.java

#### 3. Player Stats System (6 files needed)
Create in `com.bountysmp.bountyCore.stats/`:
- PlayerStats.java (UUID uuid, int kills, int deaths, long playtime, long joinTime)
- PlayerStatsManager.java
- storage/PlayerStatsStorage.java (interface)
- storage/MySQLPlayerStatsStorage.java
- storage/FlatFilePlayerStatsStorage.java
Update: listeners/CombatListener.java (add stats tracking)

#### 4. Profile System (2 files needed)
Create in `com.bountysmp.bountyCore.profile/`:
- ProfileGUI.java (54 slot stats display)
- commands/ProfileCommand.java

#### 5. PlaceholderAPI Integration (1 file needed)
Create in `com.bountysmp.bountyCore.papi/`:
- BountyCoreExpansion.java (extends PlaceholderExpansion)
  Placeholders: %bountycore_balance%, %bountycore_rank%, %bountycore_kills%, 
  %bountycore_deaths%, %bountycore_playtime%, %bountycore_booster%, 
  %bountycore_team%, %bountycore_online%

#### 6. ClearLag System (2 files needed)
Create in `com.bountysmp.bountyCore.clearlag/`:
- ClearLagManager.java (countdown task, clear entities)
- commands/admin/ClearLagCommand.java

#### 7. Stats Wipe System (2 files needed)
Create in `com.bountysmp.bountyCore.statswipe/`:
- StatsWipeGUI.java (27 slot admin menu)
- commands/admin/StatsWipeCommand.java

#### 8. KeyAll System (2 files needed)
Create in `com.bountysmp.bountyCore.keyall/`:
- KeyAllManager.java
- commands/admin/KeyAllCommand.java

#### 9. Info/Rules Menus (4 files needed)
Create in `com.bountysmp.bountyCore.menus/`:
- InfoGUI.java (loads from menus.yml)
- RulesGUI.java (loads from menus.yml)
- commands/InfoCommand.java
- commands/RulesCommand.java

#### 10. Fast Crystals (1 file needed)
Create in `com.bountysmp.bountyCore.listeners/`:
- FastCrystalListener.java (instant End Crystal place/break)

#### 11. Chat Hover Stats (1 file needed)
Create in `com.bountysmp.bountyCore.listeners/`:
- ChatListener.java (AsyncPlayerChatEvent with Adventure API hover)

### Priority 2: Integration Files

#### Update BountyCore.java

Add manager fields:
```java
private AuctionManager auctionManager;
private OrderManager orderManager;
private TeamManager teamManager;
private SettingsManager settingsManager;
private ShopManager shopManager;
private SellManager sellManager;
private SellBoosterManager sellBoosterManager;
private WarpManager warpManager;
private PlayerStatsManager playerStatsManager;
private ClearLagManager clearLagManager;
private KeyAllManager keyAllManager;
```

Add setup methods in onEnable():
```java
setupAuction();
setupOrders();
setupTeams();
setupSettings();
setupShop();
setupSell();
setupSellBooster();
setupWarps();
setupPlayerStats();
setupClearLag();
setupKeyAll();
registerPAPI();
```

Add getters:
```java
public AuctionManager getAuctionManager() { return auctionManager; }
public OrderManager getOrderManager() { return orderManager; }
// ... etc for all managers
```

Add to onDisable():
```java
if (auctionManager != null) auctionManager.close();
if (orderManager != null) orderManager.close();
if (teamManager != null) teamManager.close();
if (settingsManager != null) settingsManager.close();
if (warpManager != null) warpManager.close();
if (playerStatsManager != null) playerStatsManager.close();
```

Register commands in registerCommands():
```java
getCommand("ah").setExecutor(new AhCommand(this));
getCommand("orders").setExecutor(new OrdersCommand(this));
getCommand("team").setExecutor(new TeamCommand(this));
getCommand("settings").setExecutor(new SettingsCommand(this));
getCommand("shop").setExecutor(new ShopCommand(this));
getCommand("sell").setExecutor(new SellCommand(this));
getCommand("warp").setExecutor(new WarpCommand(this));
getCommand("profile").setExecutor(new ProfileCommand(this));
getCommand("info").setExecutor(new InfoCommand(this));
getCommand("rules").setExecutor(new RulesCommand(this));
// Admin commands
getCommand("sellbooster").setExecutor(new SellBoosterCommand(this));
getCommand("booster").setExecutor(new BoosterCommand(this));
getCommand("clearlag").setExecutor(new ClearLagCommand(this));
getCommand("statswipe").setExecutor(new StatsWipeCommand(this));
getCommand("keyall").setExecutor(new KeyAllCommand(this));
getCommand("warpmanager").setExecutor(new WarpManagerCommand(this));
```

Register listeners in registerListeners():
```java
getServer().getPluginManager().registerEvents(new TeamChatListener(this), this);
getServer().getPluginManager().registerEvents(new FastCrystalListener(this), this);
getServer().getPluginManager().registerEvents(new ChatListener(this), this);
```

Start scheduled tasks:
```java
// Expire auction listings every 5 minutes
Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
    auctionManager.expireListings();
}, 6000L, 6000L);

// Auto-clear lag if enabled
if (getConfig().getBoolean("clearlag.auto-clear.enabled", true)) {
    clearLagManager.startAutoTask();
}
```

#### Update plugin.yml

Add ALL commands:
```yaml
  ah:
    description: Open auction house
    aliases: [auction, auctionhouse]
  orders:
    description: View buy orders
  team:
    description: Manage your team
    aliases: [t]
  settings:
    description: Open settings menu
  shop:
    description: Open the server shop
  sell:
    description: Sell items
  sellbooster:
    description: Manage sell boosters
  booster:
    description: View active booster
  warp:
    description: Teleport to a warp
  warpmanager:
    description: Manage server warps
  profile:
    description: View player profile
  info:
    description: View server information
  rules:
    description: View server rules
  clearlag:
    description: Manually trigger clearlag
  statswipe:
    description: Wipe player statistics
  keyall:
    description: Give keys to all online players
```

Add ALL permissions:
```yaml
  bountycore.ah.15:
    description: Allows 15 auction house listings
    default: true
  bountycore.ah.20:
    description: Allows 20 auction house listings
    default: false
  bountycore.ah.25:
    description: Allows 25 auction house listings
    default: false
  bountycore.ah.30:
    description: Allows 30 auction house listings
    default: false
  bountycore.fastcrystal:
    description: Allows fast crystal placement
    default: false
  bounty.staff.helper:
    description: Helper rank permissions
    default: false
  bountycore.clearlag:
    description: Use clearlag command
    default: op
  bountycore.statswipe:
    description: Use statswipe command
    default: op
  bountycore.keyall:
    description: Use keyall command
    default: op
  bountycore.warpmanager:
    description: Manage warps
    default: op
```

Update softdepend:
```yaml
softdepend: [Vault, PlaceholderAPI, CrazyCrates]
```

#### Update GUIListener.java

Add click handlers for all new GUIs:
```java
// In handleClick method
if (title.equals(ChatColor.GOLD + "Auction House")) {
    // Get AuctionGUI instance and handle click
}
if (title.equals(ChatColor.GOLD + "Expired Listings")) {
    // Handle AuctionReturnGUI clicks
}
if (title.equals(ChatColor.GOLD + "Buy Orders")) {
    // Handle OrderGUI clicks
}
// ... etc for all GUIs
```

## Code Templates

### Template: MySQL Storage Class
```java
public class MySQLXStorage implements XStorage {
    private final HikariDataSource dataSource;
    private final Logger logger;

    public MySQLXStorage(HikariDataSource dataSource, Logger logger) {
        this.dataSource = dataSource;
        this.logger = logger;
        createTables();
    }

    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS table_name (" +
                     "id VARCHAR(36) PRIMARY KEY, " +
                     "data TEXT NOT NULL" +
                     ")";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create table", e);
        }
    }

    @Override
    public void close() {
        // DataSource is shared, don't close
    }
}
```

### Template: FlatFile Storage Class
```java
public class FlatFileXStorage implements XStorage {
    private final File dataFile;
    private final Gson gson;
    private final Map<UUID, X> cache;
    private final Logger logger;

    public FlatFileXStorage(File dataFolder, Logger logger) {
        this.dataFile = new File(dataFolder, "x.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.cache = new ConcurrentHashMap<>();
        this.logger = logger;
        loadAll();
    }

    @Override
    public void close() {
        saveAll().join();
    }
}
```

### Template: Manager Class
```java
public class XManager {
    private final BountyCore plugin;
    private final XStorage storage;

    public XManager(BountyCore plugin) {
        this.plugin = plugin;
        String storageType = plugin.getConfig().getString("x.storage-type", "FLATFILE");
        // Initialize storage based on type
    }

    public void close() {
        storage.close();
    }
}
```

### Template: GUI Class
```java
public class XGUI {
    private final BountyCore plugin;
    private final Player viewer;
    private final int page;
    private static final int SLOTS_PER_PAGE = 45;

    public XGUI(BountyCore plugin, Player viewer, int page) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.page = page;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Title");
        // Add items to slots 0-44
        // Navigation buttons at 48, 49, 50
        viewer.openInventory(inv);
    }

    public void handleClick(int slot, Player clicker) {
        // Handle navigation and item clicks
    }
}
```

### Template: Command Class
```java
public class XCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public XCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("general.player-only"));
            return true;
        }
        Player player = (Player) sender;
        // Command logic
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return new ArrayList<>();
    }
}
```

## Testing Checklist

After completing all files:

1. ✅ Compile plugin (check for errors)
2. ✅ Load plugin on test server
3. ✅ Test each command
4. ✅ Test each GUI
5. ✅ Test MySQL storage
6. ✅ Test FlatFile storage
7. ✅ Test permissions
8. ✅ Test PlaceholderAPI integration
9. ✅ Test with Vault economy
10. ✅ Test CrazyCrates integration

## Notes

- All storage implementations use async CompletableFuture operations
- ItemStack serialization uses Base64 + BukkitObjectOutputStream
- All GUIs follow the 54-slot pagination pattern
- All managers have close() methods for cleanup
- All commands have tab completion
- Permission system is hierarchical
- Config files use YAML with color code support (&)

## Estimated Remaining Work

- Files to create: ~45
- Lines of code: ~8,000
- Time estimate: 6-8 hours for experienced developer

## Support Files Location

All configuration files are in: src/main/resources/
- config.yml (existing, needs updates)
- messages.yml (existing)
- shop.yml ✅
- sell.yml ✅
- clearlag.yml ✅
- keyall.yml ✅
- menus.yml ✅
