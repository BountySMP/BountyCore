# BountyCore - Quick Reference Guide

## Commands Quick Reference

### Player Commands (bounty.basic)
| Command | Aliases | Description |
|---------|---------|-------------|
| `/ah` | auction, auctionhouse | Open auction house |
| `/ah return` | - | View expired listings |
| `/ah search <query>` | - | Search listings |
| `/orders` | - | Manage buy orders |
| `/team create <name>` | teams | Create a team |
| `/team invite <player>` | - | Invite to team |
| `/team accept/deny` | - | Accept/deny invite |
| `/team leave` | - | Leave team |
| `/team info [team]` | - | View team info |
| `/team list` | - | List all teams |
| `/team chat` | - | Toggle team chat |
| `/settings` | options | Open settings menu |
| `/shop` | - | Open server shop |
| `/sell` | - | Open sell GUI |
| `/sell hand` | - | Sell item in hand |
| `/sell all` | - | Sell all items |
| `/warp [name]` | - | Teleport to warp |
| `/booster` | - | View active booster |
| `/info` | - | Server info menu |
| `/rules` | - | Server rules menu |

### Admin Commands
| Command | Permission | Description |
|---------|-----------|-------------|
| `/team kick <player>` | bounty.staff.admin | Kick from team |
| `/team disband` | bounty.staff.admin | Disband team |
| `/sellbooster <player> <mult> <time>` | bounty.staff.admin | Give sell booster |
| `/warpmanager create <name>` | bounty.staff.admin | Create warp |
| `/warpmanager delete <name>` | bounty.staff.admin | Delete warp |
| `/warpmanager seticon <name> <mat>` | bounty.staff.admin | Set warp icon |
| `/profile <player>` | bounty.staff.helper | View player profile |
| `/clearlag` | bounty.staff.admin | Trigger clearlag |
| `/clearlag reload` | bounty.staff.admin | Reload clearlag config |
| `/statswipe` | bounty.staff.admin | Open stats wipe GUI |
| `/keyall <crate> <amount>` | bounty.staff.admin | Give keys to all |

---

## Permissions Quick Reference

### Tiered Permissions
```yaml
# Home Limits
bountycore.homes.1    # 1 home (default)
bountycore.homes.2    # 2 homes
bountycore.homes.3    # 3 homes
bountycore.homes.5    # 5 homes
bountycore.homes.10   # 10 homes
bountycore.homes.15   # 15 homes
bountycore.homes.20   # 20 homes
bountycore.homes.25   # 25 homes
bountycore.homes.30   # 30 homes
bountycore.homes.50   # 50 homes

# Auction Listing Limits
bountycore.ah.15      # 15 listings (default)
bountycore.ah.20      # 20 listings
bountycore.ah.25      # 25 listings
bountycore.ah.30      # 30 listings
```

### Feature Permissions
```yaml
bountycore.fastcrystal   # Fast crystal mechanics (default: true)
bounty.basic             # All basic player commands
bounty.staff.helper      # Profile viewer access
bounty.staff.admin       # All admin commands
```

---

## PlaceholderAPI Placeholders

```
%bountycore_balance%        # $1,234.56
%bountycore_rank%           # [VIP]
%bountycore_kills%          # 123
%bountycore_deaths%         # 45
%bountycore_kd%             # 2.7
%bountycore_playtime%       # 12h 34m
%bountycore_booster%        # 2.5x or None
%bountycore_booster_time%   # 1h 23m or None
%bountycore_bounty%         # $5,000.00 or None
%bountycore_online%         # 42
%bountycore_homes%          # 5
```

---

## Configuration Files

### config.yml (additions)
```yaml
fast-crystals:
  enabled: true

chat-hover-stats:
  enabled: true

economy:
  storage-type: MYSQL  # or FLATFILE
  starting-balance: 1000.0
  currency-name: Coin
  currency-plural: Coins
  mysql:
    host: localhost
    port: 3306
    database: bountycore
    username: root
    password: ""
    pool-size: 10
```

### shop.yml
```yaml
categories:
  tools:
    name: "&6Tools"
    icon: DIAMOND_PICKAXE
  food:
    name: "&aFood"
    icon: COOKED_BEEF
  # ...

items:
  DIAMOND_PICKAXE:
    buy: 1000.0
    sell: 500.0
  # ...
```

### sell.yml
```yaml
items:
  DIAMOND: 100.0
  IRON_INGOT: 10.0
  GOLD_INGOT: 15.0
  # ...

categories:
  ORES: 1.0
  INGOTS: 1.0
  GEMS: 1.5
  CROPS: 0.8
  MOB_DROPS: 1.2
  # ...
```

### clearlag.yml
```yaml
enabled: true
interval: 300  # seconds (5 minutes)
warnings: [60, 30, 15, 10, 5, 4, 3, 2, 1]  # seconds before clear
remove-items: true
remove-monsters: true
remove-animals: false
remove-villagers: false
exclude-named-entities: true
```

### keyall.yml
```yaml
command-template: "crazycrates give %player% %crate% %amount%"
```

### menus.yml
```yaml
info:
  title: "&aServer Info"
  items:
    0:
      material: BOOK
      name: "&6Welcome!"
      lore:
        - "&7Welcome to our server!"
        - "&7Have fun!"

rules:
  title: "&cServer Rules"
  items:
    0:
      material: WRITTEN_BOOK
      name: "&cRule 1: No Griefing"
      lore:
        - "&7Breaking this rule will result in a ban"
```

---

## Database Tables (MySQL)

Auto-created on first run:
- `economy` - Player balances
- `ah_listings` - Auction listings
- `orders` - Buy orders
- `teams` - Team data
- `team_members` - Team membership
- `player_settings` - Settings toggles
- `sell_boosters` - Active sell boosters
- `warps` - Server warps
- `player_stats` - Kills, deaths, playtime

---

## Common Tasks

### Give a Player a Sell Booster
```
/sellbooster <player> <multiplier> <duration>
Example: /sellbooster Notch 2.0 24h
Duration formats: 1h, 24h, 7d
```

### Create a Warp
```
1. Stand at desired location
2. /warpmanager create <name>
3. /warpmanager seticon <name> <material>
```

### Configure Shop Categories
```yaml
# In shop.yml
categories:
  custom:
    name: "&dCustom Category"
    icon: NETHER_STAR

items:
  NETHER_STAR:
    buy: 10000.0
    sell: 5000.0
```

### Wipe Player Stats (Admin)
```
1. /statswipe
2. Click desired wipe option
3. Confirm in dialog
```

### Team Management
```
# As player:
/team create MyTeam
/team invite PlayerName
/team chat  # Toggle team chat

# As admin:
/team kick PlayerName  # Kick from their team
/team disband  # While looking at team info
```

---

## Troubleshooting

### Plugin Won't Start
- Check MySQL connection in config.yml
- Ensure Vault is installed
- Check server log for errors

### Auction Items Not Saving
- Verify MySQL connection
- Check file permissions for FlatFile fallback
- Ensure `dataFolder/auction/` exists

### PlaceholderAPI Not Working
- Install PlaceholderAPI plugin
- Restart server
- Check `/papi list` for BountyCore

### Sell Boosters Not Applying
- Verify booster is active: `/booster`
- Check expiry time
- Ensure SellBoosterManager initialized (check logs)

---

## API Usage (For Developers)

```java
// Get plugin instance
BountyCore plugin = BountyCore.getInstance();

// Access managers
AuctionManager auctionManager = plugin.getAuctionManager();
OrderManager orderManager = plugin.getOrderManager();
TeamManager teamManager = plugin.getTeamManager();
ShopManager shopManager = plugin.getShopManager();
SellBoosterManager boosterManager = plugin.getSellBoosterManager();
SellManager sellManager = plugin.getSellManager();
WarpManager warpManager = plugin.getWarpManager();
PlayerStatsManager statsManager = plugin.getPlayerStatsManager();
SettingsManager settingsManager = plugin.getSettingsManager();

// Example: Give a sell booster
UUID playerUuid = player.getUniqueId();
double multiplier = 2.0;
long duration = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
boosterManager.giveBooster(playerUuid, multiplier, duration);

// Example: Create a warp
Location location = player.getLocation();
String warpName = "spawn";
Material icon = Material.BEACON;
warpManager.createWarp(warpName, location, player.getUniqueId(), icon);

// Example: Check player settings
boolean allowsTpa = settingsManager.getSetting(playerUuid, "tpa-requests");
if (allowsTpa) {
    // Send TPA request
}
```

---

## Performance Tips

1. **Use MySQL for production** - Much faster than FlatFile for large player bases
2. **Adjust HikariCP pool size** - Default 10, increase for high player count
3. **Monitor scheduled tasks** - Check timings for auction expiry and booster checks
4. **Clear old auction listings** - Expired listings auto-delete but check database size
5. **Optimize shop.yml** - Don't add thousands of items, keep it reasonable

---

## Support

For issues or questions:
1. Check server logs in `logs/latest.log`
2. Verify config syntax (YAML is indent-sensitive!)
3. Test with FlatFile storage to isolate MySQL issues
4. Check permissions are set correctly
5. Restart server after config changes

---

*BountyCore v1.0.0 - Paper 1.21*
