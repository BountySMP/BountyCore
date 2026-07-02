package com.bountysmp.bountyCore.settings.storage;

import com.bountysmp.bountyCore.settings.PlayerSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FlatFileSettingsStorage implements SettingsStorage {
    private final File dataFile;
    private final Gson gson;
    private final Map<UUID, PlayerSettings> cache;
    private final Logger logger;

    public FlatFileSettingsStorage(File dataFolder, Logger logger) {
        this.dataFile = new File(dataFolder, "player_settings.json");
        this.gson     = new GsonBuilder().setPrettyPrinting().create();
        this.cache    = new ConcurrentHashMap<>();
        this.logger   = logger;
        if (!dataFolder.exists()) dataFolder.mkdirs();
        loadAll();
    }

    private void loadAll() {
        if (!dataFile.exists()) return;
        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, SettingsData>>(){}.getType();
            Map<String, SettingsData> data = gson.fromJson(reader, type);
            if (data == null) return;
            data.forEach((uuidStr, sd) -> {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    PlayerSettings s = new PlayerSettings(uuid);
                    // Boolean (boxed) fields are null when absent in old JSON — fall back to defaults
                    if (sd.allowMsg            != null) s.setAllowMsg(sd.allowMsg);
                    if (sd.allowTpa            != null) s.setAllowTpa(sd.allowTpa);
                    if (sd.allowTpaHere        != null) s.setAllowTpaHere(sd.allowTpaHere);
                    if (sd.autoConfirmTpa      != null) s.setAutoConfirmTpa(sd.autoConfirmTpa);
                    if (sd.tpaConfirmMenu      != null) s.setTpaConfirmMenu(sd.tpaConfirmMenu);
                    if (sd.payAlerts           != null) s.setPayAlerts(sd.payAlerts);
                    if (sd.payConfirmMenu      != null) s.setPayConfirmMenu(sd.payConfirmMenu);
                    if (sd.serverBroadcasts    != null) s.setServerBroadcasts(sd.serverBroadcasts);
                    if (sd.auctionNotifications!= null) s.setAuctionNotifications(sd.auctionNotifications);
                    if (sd.bountyAlerts        != null) s.setBountyAlerts(sd.bountyAlerts);
                    if (sd.teamInvites         != null) s.setTeamInvites(sd.teamInvites);
                    if (sd.keyAllNotifications != null) s.setKeyAllNotifications(sd.keyAllNotifications);
                    if (sd.hotbarMessages      != null) s.setHotbarMessages(sd.hotbarMessages);
                    if (sd.scoreboard          != null) s.setScoreboard(sd.scoreboard);
                    cache.put(uuid, s);
                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid UUID in settings data: " + uuidStr);
                }
            });
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load settings data", e);
        }
    }

    @Override
    public CompletableFuture<PlayerSettings> loadSettings(UUID uuid) {
        return CompletableFuture.completedFuture(cache.computeIfAbsent(uuid, PlayerSettings::new));
    }

    @Override
    public CompletableFuture<Void> saveSettings(PlayerSettings settings) {
        cache.put(settings.getUuid(), settings);
        return saveAll();
    }

    private CompletableFuture<Void> saveAll() {
        return CompletableFuture.runAsync(() -> {
            Map<String, SettingsData> data = new ConcurrentHashMap<>();
            cache.forEach((uuid, s) -> data.put(uuid.toString(), new SettingsData(s)));
            try (FileWriter writer = new FileWriter(dataFile)) {
                gson.toJson(data, writer);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to save settings data", e);
            }
        });
    }

    @Override
    public void close() { saveAll().join(); }

    private static class SettingsData {
        // Boxed Boolean so Gson leaves them null when absent (preserves old-file compat)
        Boolean allowMsg, allowTpa, allowTpaHere, autoConfirmTpa, tpaConfirmMenu;
        Boolean payAlerts, payConfirmMenu;
        Boolean serverBroadcasts, auctionNotifications, bountyAlerts;
        Boolean teamInvites, keyAllNotifications, hotbarMessages, scoreboard;

        SettingsData() {}

        SettingsData(PlayerSettings s) {
            allowMsg             = s.isAllowMsg();
            allowTpa             = s.isAllowTpa();
            allowTpaHere         = s.isAllowTpaHere();
            autoConfirmTpa       = s.isAutoConfirmTpa();
            tpaConfirmMenu       = s.isTpaConfirmMenu();
            payAlerts            = s.isPayAlerts();
            payConfirmMenu       = s.isPayConfirmMenu();
            serverBroadcasts     = s.isServerBroadcasts();
            auctionNotifications = s.isAuctionNotifications();
            bountyAlerts         = s.isBountyAlerts();
            teamInvites          = s.isTeamInvites();
            keyAllNotifications  = s.isKeyAllNotifications();
            hotbarMessages       = s.isHotbarMessages();
            scoreboard           = s.isScoreboard();
        }
    }
}
