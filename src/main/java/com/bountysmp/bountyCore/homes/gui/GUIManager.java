package com.bountysmp.bountyCore.homes.gui;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIManager {
    private final Map<UUID, HomeGUISession> homeSessions;
    private final Map<UUID, HomeDeleteSession> deleteSessions;

    public GUIManager() {
        this.homeSessions = new HashMap<>();
        this.deleteSessions = new HashMap<>();
    }

    public void openHomeGUI(HomeGUI gui, Player player, int page) {
        homeSessions.put(player.getUniqueId(), new HomeGUISession(gui, page));
    }

    public HomeGUISession getHomeSession(UUID uuid) {
        return homeSessions.get(uuid);
    }

    public void removeHomeSession(UUID uuid) {
        homeSessions.remove(uuid);
    }

    public void openDeleteGUI(HomeDeleteConfirmGUI gui, Player player, String homeName, int returnPage) {
        deleteSessions.put(player.getUniqueId(), new HomeDeleteSession(gui, homeName, returnPage));
    }

    public HomeDeleteSession getDeleteSession(UUID uuid) {
        return deleteSessions.get(uuid);
    }

    public void removeDeleteSession(UUID uuid) {
        deleteSessions.remove(uuid);
    }

    public static class HomeGUISession {
        private final HomeGUI gui;
        private final int page;

        public HomeGUISession(HomeGUI gui, int page) {
            this.gui = gui;
            this.page = page;
        }

        public HomeGUI getGui() {
            return gui;
        }

        public int getPage() {
            return page;
        }
    }

    public static class HomeDeleteSession {
        private final HomeDeleteConfirmGUI gui;
        private final String homeName;
        private final int returnPage;

        public HomeDeleteSession(HomeDeleteConfirmGUI gui, String homeName, int returnPage) {
            this.gui = gui;
            this.homeName = homeName;
            this.returnPage = returnPage;
        }

        public HomeDeleteConfirmGUI getGui() {
            return gui;
        }

        public String getHomeName() {
            return homeName;
        }

        public int getReturnPage() {
            return returnPage;
        }
    }
}
