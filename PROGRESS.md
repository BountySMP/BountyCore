# BountyCore Development Progress

## ✅ Completed: Economy System

### Features Implemented
- **Vault Integration**: Full Vault Economy provider implementation
- **Storage Options**: 
  - FlatFile (JSON) storage (default)
  - MySQL storage with HikariCP connection pooling
- **Commands**:
  - `/balance [player]` - Check balance (aliases: /bal, /money)
  - `/pay <player> <amount>` - Pay another player
- **Configuration**: 
  - Customizable currency name/plural/symbol
  - Starting balance setting
  - Storage type selection (FLATFILE/MYSQL)
  - MySQL connection settings

### Technical Details
- Java 21, Paper 1.21.x
- Async storage operations using CompletableFuture
- In-memory caching for performance
- Proper dependency shading (HikariCP, Gson)
- Auto-save on plugin disable

### Files Created
```
src/main/java/com/antony125/bountyCore/
├── BountyCore.java (main plugin class)
├── economy/
│   ├── EconomyData.java
│   ├── BountyCoreEconomy.java (Vault provider)
│   └── storage/
│       ├── EconomyStorage.java (interface)
│       ├── FlatFileStorage.java
│       └── MySQLStorage.java
├── commands/
│   ├── BalanceCommand.java
│   └── PayCommand.java
└── listeners/
    └── PlayerListener.java

src/main/resources/
├── config.yml
└── plugin.yml
```

### Build Output
- Plugin JAR: `build/libs/BountyCore-1.0.0.jar`
- Build status: ✅ SUCCESS

---

## ✅ Completed: Homes System

### Features Implemented
- **GUI Interface**: 54-slot double chest GUI with pagination
  - Slots 0-44: Home slots (colored beds for set homes, gray panes for empty, barriers for locked)
  - Bottom row: Navigation buttons (previous/next page)
  - Click bed to teleport (left click) or delete (right click)
- **Commands**:
  - `/home` - Opens homes GUI
  - `/sethome <name>` - Set a home (alphanumeric + underscore, max 16 chars)
  - `/delhome <name>` - Delete a home via command
- **Deletion Confirmation**: 9-slot GUI with cancel/confirm options
- **Warmup System**: 
  - 5-second warmup (configurable)
  - Cancelled if player moves or takes damage
  - Visual countdown messages
- **Home Limits**: Permission-based limits (bountycore.homes.1 through bountycore.homes.50)
- **Storage**: Async JSON file storage per player (homes/<uuid>.json)
- **Random Bed Colors**: Each home displays with a random colored bed based on name hash

### Technical Details
- Pagination support for players with many homes
- In-memory caching with async save/load
- GUI state management via GUIManager
- Movement and damage detection during warmup
- Prepared for combat tag integration (placeholder for CombatTagManager)

### Files Created
```
src/main/java/com/antony125/bountyCore/
├── homes/
│   ├── Home.java
│   ├── HomeManager.java
│   ├── TeleportWarmup.java
│   └── gui/
│       ├── GUIManager.java
│       ├── HomeGUI.java
│       └── HomeDeleteConfirmGUI.java
├── commands/
│   ├── HomeCommand.java
│   ├── SetHomeCommand.java
│   └── DelHomeCommand.java
└── listeners/
    ├── GUIListener.java
    └── TeleportWarmupListener.java
```

---

## ✅ Completed: Teleportation System

### Features Implemented
- **TPA System**:
  - `/tpa <player>` - Send teleport request (60 second expiry, one pending at a time)
  - `/tpaccept` (alias: `/tpyes`) - Accept incoming request with 5 second warmup
  - `/tpdeny` (alias: `/tpno`) - Deny incoming request
  - Auto-expiration notifications
  - Cannot request to yourself
- **Spawn System**:
  - `/spawn` - Teleport to server spawn with 3 second warmup
  - `/setspawn` - Set spawn location (permission: bountycore.setspawn)
  - Persistent spawn storage in config.yml (world, x, y, z, yaw, pitch)
- **Back Command**:
  - `/back` - Return to last location before death or teleport
  - One location saved per player
- **Combat Tag System**:
  - 15 second combat tag on PvP damage (configurable)
  - Prevents using /tpa, /tpaccept, /spawn, /home, /back while tagged
  - Shows remaining seconds when blocked
  - Combat tag notifications (on tag/untag)
  - Combat log kill on logout (configurable)
- **Warmup System**:
  - Cancelled on movement or damage
  - Countdown messages
  - Integrated with homes system

### Technical Details
- Request management with auto-cleanup
- Location persistence for /back
- Combat tag tracking with auto-expire task
- Warmup cancellation on move/damage
- Config-driven timers and settings

### Files Created
```
src/main/java/com/antony125/bountyCore/
├── teleport/
│   ├── CombatTagManager.java
│   ├── TeleportManager.java
│   ├── TeleportRequest.java
│   └── TeleportWarmup.java
├── commands/
│   ├── TpaCommand.java
│   ├── TpAcceptCommand.java
│   ├── TpDenyCommand.java
│   ├── SpawnCommand.java
│   ├── SetSpawnCommand.java
│   └── BackCommand.java
└── listeners/
    └── CombatListener.java
```

### Config Options
```yaml
teleport:
  tpa-warmup-seconds: 5
  spawn-warmup-seconds: 3
  tpa-expire-seconds: 60
  combat-tag-seconds: 15
  combat-log-kill: true
```

---

## ✅ Completed: Private Messaging System

### Features Implemented
- **Commands**:
  - `/msg <player> <message>` (aliases: /tell, /whisper, /w, /m) - Send private message
  - `/r <message>` (alias: /reply) - Reply to last player who messaged you
- **Message Format**:
  - Sender sees: `§7[§fYou §7→ §f<player>§7] §f<message>`
  - Receiver sees: `§7[§f<sender> §7→ §fYou§7] §f<message>`
- **Behavior**:
  - Cannot message yourself
  - Cannot message offline players
  - Reply targets update for both players automatically
  - Conversation flows naturally with /r command
  - Tab completion for player names in /msg
- **Configuration**: messages.yml with customizable formats and error messages

### Technical Details
- In-memory reply target tracking (no persistence needed)
- Color code support in messages.yml
- Placeholder system for player names and messages
- Bidirectional reply target updates

### Files Created
```
src/main/java/com/antony125/bountyCore/
├── messaging/
│   └── MessagingManager.java
└── commands/
    ├── MsgCommand.java
    └── ReplyCommand.java

src/main/resources/
└── messages.yml
```

---

## ✅ Completed: Bounty/Wanted System

### Features Implemented
- **Commands**:
  - `/bounty` - Opens GUI showing all active bounties (54-slot inventory)
  - `/bounty set <player> <amount>` - Place a bounty (minimum $10,000)
  - `/bounty <player>` - View specific player's bounty in chat
- **Bounty GUI**:
  - Player heads with skulls showing bounty amounts
  - Sorted by bounty amount (highest first)
  - Pagination with page indicator
  - Bottom row navigation (previous/next page)
  - Gray glass pane fillers for empty slots
- **Auto Payout**:
  - Automatic bounty claim on PvP kill
  - Full bounty paid to killer via Vault
  - Server-wide broadcast message
  - Bounty reset to $0 after claim
- **Bounty Management**:
  - Bounties stack when multiple placed on same target
  - Cannot bounty yourself
  - Cannot bounty offline players
  - Money withdrawn from placer's balance immediately
  - Persistent storage in bounties.yml

### Technical Details
- BountyManager handles all bounty logic
- Cached bounty data with file persistence
- Integration with Vault economy
- SkullMeta for player head display
- Configurable minimum bounty amount

### Files Created
```
src/main/java/com/antony125/bountyCore/
├── bounty/
│   ├── BountyManager.java
│   └── BountyGUI.java
├── commands/
│   └── BountyCommand.java
└── listeners/
    └── BountyListener.java

bounties.yml (auto-generated)
```

### Config Options
```yaml
bounty:
  minimum-amount: 10000
```

### Broadcast Format
`§6§l[BOUNTY] §e<killer> §7has claimed §e<victim>§7's bounty for §c$<amount>§7!`

---

## 🚧 To Do Next

### Utility Commands
- `/reload` blocker for all players/ops/console

### Stats System
- `/stats [player]` - View player statistics
  - Kills
  - Deaths
  - KDR
  - Total heads sold (from HeadHunter)
  - HeadHunter level
  - Active bounty amount
  - Money balance

---

## Notes
- Economy is the foundation - all other systems will integrate with it
- HeadHunter integration via Vault economy is ready
- Bounty system will use the economy for rewards
- Stats system will pull HeadHunter data from playerdata.json
