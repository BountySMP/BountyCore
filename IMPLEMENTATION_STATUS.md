# BountyCore Implementation Status

## Completed Systems

### 1. Auction House (/ah) - COMPLETE
- ✅ AuctionListing.java (model)
- ✅ AuctionStorage.java (interface)
- ✅ MySQLAuctionStorage.java (full MySQL implementation with ItemStack serialization)
- ✅ FlatFileAuctionStorage.java (full FlatFile implementation)
- ✅ AuctionManager.java (listItem, buyItem, expireListings, returnItem)
- ✅ AuctionGUI.java (54 slot paginated)
- ✅ AuctionListGUI.java (27 slot listing interface)
- ✅ AuctionReturnGUI.java (expired items return interface)
- ✅ AhCommand.java (/ah, /ah return, /ah search)

### 2. Orders (/orders) - COMPLETE
- ✅ BuyOrder.java (model)
- ✅ OrderStorage.java (interface)
- ✅ MySQLOrderStorage.java
- ✅ FlatFileOrderStorage.java
- ✅ OrderManager.java
- ✅ OrderGUI.java
- ✅ OrderPlaceGUI.java
- ✅ OrdersCommand.java

### 3. Teams (/team) - COMPLETE
- ✅ Team.java (model)
- ✅ TeamStorage.java (interface)
- ✅ MySQLTeamStorage.java
- ✅ FlatFileTeamStorage.java
- ✅ TeamManager.java
- ✅ TeamGUI.java
- ✅ TeamCommand.java
- ✅ TeamChatListener.java

### 4. Settings (/settings) - COMPLETE
- ✅ PlayerSettings.java
- ✅ SettingsStorage.java (interface) 
- ✅ MySQLSettingsStorage.java
- ✅ FlatFileSettingsStorage.java
- ✅ SettingsManager.java
- ✅ SettingsGUI.java
- ✅ SettingsCommand.java

### 5. Shop (/shop) - COMPLETE
- ✅ ShopManager.java
- ✅ ShopGUI.java
- ✅ ShopCategoryGUI.java
- ✅ ShopCommand.java
- ✅ shop.yml config file

### 6. Sell (/sell) - PARTIAL
- ✅ SellManager.java
- ✅ sell.yml config file
- ⚠️ Missing: SellGUI.java, SellCommand.java

### 7. Configuration Files - COMPLETE
- ✅ shop.yml
- ✅ sell.yml
- ✅ clearlag.yml
- ✅ keyall.yml
- ✅ menus.yml

## Files Still Needed

### High Priority (Core Functionality)
1. **Sell System**
   - SellGUI.java
   - SellCommand.java

2. **Sell Booster System**
   - SellBooster.java
   - BoosterStorage.java (interface)
   - MySQLBoosterStorage.java
   - FlatFileBoosterStorage.java
   - SellBoosterManager.java
   - SellBoosterCommand.java
   - BoosterCommand.java

3. **Warps System**
   - Warp.java
   - WarpStorage.java (interface)
   - MySQLWarpStorage.java
   - FlatFileWarpStorage.java
   - WarpManager.java
   - WarpGUI.java
   - WarpCommand.java
   - WarpManagerCommand.java

4. **Player Stats System**
   - PlayerStats.java
   - PlayerStatsStorage.java (interface)
   - MySQLPlayerStatsStorage.java
   - FlatFilePlayerStatsStorage.java
   - PlayerStatsManager.java
   - Update CombatListener.java

5. **Profile System**
   - ProfileGUI.java
   - ProfileCommand.java

6. **PlaceholderAPI Integration**
   - papi/BountyCoreExpansion.java

7. **ClearLag System**
   - ClearLagManager.java
   - ClearLagCommand.java

8. **Stats Wipe**
   - StatsWipeGUI.java
   - StatsWipeCommand.java

9. **KeyAll**
   - KeyAllManager.java
   - KeyAllCommand.java

10. **Info/Rules Menus**
    - menus/InfoGUI.java
    - menus/RulesGUI.java
    - InfoCommand.java
    - RulesCommand.java

11. **Fast Crystals**
    - listeners/FastCrystalListener.java

12. **Chat Hover Stats**
    - listeners/ChatListener.java

### Critical Integration Tasks

1. **Update BountyCore.java**
   - Add all manager fields
   - Create setup methods
   - Initialize in onEnable()
   - Register all commands
   - Register all listeners
   - Start scheduled tasks
   - Add close() calls in onDisable()

2. **Update plugin.yml**
   - Add all new commands
   - Add all permissions (including bountycore.ah.15/20/25/30, bounty.staff.helper, bountycore.fastcrystal)
   - Add softdepend: [PlaceholderAPI, Vault, CrazyCrates]

3. **Update GUIListener.java**
   - Add click handlers for all new GUIs

4. **Update MsgCommand.java and TpaCommand.java**
   - Integrate SettingsManager checks before sending messages/requests

## Implementation Notes

### Storage Pattern
All systems use the established HikariCP MySQL + FlatFile fallback pattern:
```java
// MySQL: Shared HikariDataSource from economy
// FlatFile: JSON with Gson, per-system files
// ItemStack serialization: BukkitObjectOutputStream/InputStream + Base64
```

### GUI Pattern
All GUIs follow the BountyGUI.java pattern:
- 54 slots for main interfaces
- Slots 0-44 for content
- Slots 48-50 for navigation (previous, page indicator, next)
- 27 slots for smaller interfaces
- Inventory click handling in GUIListener

### Manager Pattern
All managers follow HomeManager.java pattern:
- Constructor initializes storage
- CompletableFuture for async operations
- close() method for cleanup
- Integration with BountyCore instance

## Next Steps

1. Create remaining system files (use existing files as templates)
2. Update BountyCore.java with all manager integrations
3. Update plugin.yml with complete command and permission lists
4. Update GUIListener.java with new GUI handlers
5. Test compilation
6. Test in-game functionality

## Commands Summary

### Player Commands
- /ah, /ah return, /ah search
- /orders
- /team (create, disband, invite, kick, leave, list, info)
- /settings
- /shop
- /sell, /sell hand, /sell all
- /warp
- /profile <player>
- /info
- /rules

### Admin Commands
- /sellbooster, /booster
- /clearlag
- /statswipe
- /keyall
- /warpmanager

## Permissions Summary

### Player Permissions
- bountycore.ah.15/20/25/30 (auction house listing limits)
- bountycore.fastcrystal (fast crystal placing/breaking)

### Staff Permissions  
- bounty.staff.helper (helper rank)
- All existing staff permissions

## Database Schema

### auction_listings table
```sql
listing_id VARCHAR(36) PRIMARY KEY
seller_uuid VARCHAR(36) NOT NULL
seller_name VARCHAR(16) NOT NULL
item TEXT NOT NULL
price DOUBLE NOT NULL
expiry_time BIGINT NOT NULL
status VARCHAR(16) NOT NULL
```

### Similar tables for: orders, teams, settings, sell_boosters, warps, player_stats
