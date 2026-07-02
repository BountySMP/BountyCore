package com.bountysmp.bountyCore.scoreboard;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.settings.PlayerSettings;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager implements Listener {

    private static final int MAX_LINES = 15;
    private static final String[] ENTRY_CODES =
        {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e"};
    private static final String OBJECTIVE_NAME = "bc_sidebar";

    private final BountyCore plugin;
    private final Map<UUID, Scoreboard> playerBoards = new HashMap<>();
    private FileConfiguration config;
    private int titleIndex = 0;

    public ScoreboardManager(BountyCore plugin) {
        this.plugin = plugin;
        ColorUtils.init();
        load();
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "scoreboard.yml");
        if (!file.exists()) plugin.saveResource("scoreboard.yml", false);
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        load();
        updateAll();
    }

    public boolean isEnabled() {
        return config.getBoolean("SCOREBOARD.ENABLED", true);
    }

    public long getUpdateTicks() {
        return Math.max(1, config.getLong("SCOREBOARD.UPDATE-TICKS", 20));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) setupPlayer(player);
        }, 5L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        playerBoards.remove(event.getPlayer().getUniqueId());
    }

    public void setupPlayer(Player player) {
        if (!isEnabled() || !isVisibleFor(player)) {
            hidePlayer(player);
            return;
        }
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, ColorUtils.toComponent(firstTitle()));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        playerBoards.put(player.getUniqueId(), board);
        player.setScoreboard(board);
        updateText(player, board, obj);
    }

    public void applyVisibility(Player player) {
        if (!isEnabled() || !isVisibleFor(player)) {
            hidePlayer(player);
            return;
        }
        if (!playerBoards.containsKey(player.getUniqueId())) {
            setupPlayer(player);
        } else {
            update(player);
        }
    }

    public void update(Player player) {
        if (!isEnabled() || !isVisibleFor(player)) {
            hidePlayer(player);
            return;
        }
        Scoreboard board = playerBoards.get(player.getUniqueId());
        if (board == null) {
            setupPlayer(player);
            return;
        }
        Objective obj = board.getObjective(OBJECTIVE_NAME);
        if (obj == null) return;
        updateText(player, board, obj);
    }

    public void updateAll() {
        List<String> titles = config.getStringList("SCOREBOARD.TITLE");
        if (!titles.isEmpty()) titleIndex = (titleIndex + 1) % titles.size();
        for (Player player : Bukkit.getOnlinePlayers()) {
            update(player);
        }
    }

    public void removePlayer(UUID uuid) {
        playerBoards.remove(uuid);
    }

    private void updateText(Player player, Scoreboard board, Objective obj) {
        List<String> titles = config.getStringList("SCOREBOARD.TITLE");
        if (!titles.isEmpty()) {
            obj.displayName(ColorUtils.toComponent(titles.get(titleIndex % titles.size()), player));
        }

        List<String> lines = config.getStringList("SCOREBOARD.LINES");
        int count = Math.min(lines.size(), MAX_LINES);
        syncLineSlots(board, obj, count);

        for (int i = 0; i < count; i++) {
            Team team = board.getTeam("sb_" + i);
            if (team != null) {
                team.prefix(ColorUtils.toComponent(lines.get(i), player));
            }
        }

        if (config.getBoolean("SCOREBOARD.HIDE-NUMBERS", true)) {
            obj.numberFormat(NumberFormat.blank());
        }
    }

    private void syncLineSlots(Scoreboard board, Objective obj, int count) {
        for (int i = 0; i < count; i++) {
            Team team = board.getTeam("sb_" + i);
            if (team == null) {
                team = board.registerNewTeam("sb_" + i);
                team.addEntry(entry(i));
            }
            obj.getScore(entry(i)).setScore(count - i);
        }
        for (int i = count; i < MAX_LINES; i++) {
            board.resetScores(entry(i));
        }
    }

    private String entry(int index) {
        return "§" + ENTRY_CODES[index] + "§r";
    }

    private String firstTitle() {
        List<String> titles = config.getStringList("SCOREBOARD.TITLE");
        return titles.isEmpty() ? "&aBountySMP" : titles.get(0);
    }

    private boolean isVisibleFor(Player player) {
        PlayerSettings settings = plugin.getSettingsManager().getSettings(player.getUniqueId()).getNow(null);
        return settings == null || settings.isScoreboard();
    }

    private void hidePlayer(Player player) {
        playerBoards.remove(player.getUniqueId());
        if (Bukkit.getScoreboardManager() != null) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }
}
