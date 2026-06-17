package com.bountysmp.bountyCore.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MessagingManager {
    private final Map<UUID, UUID> replyTargets;

    public MessagingManager() {
        this.replyTargets = new HashMap<>();
    }

    public void setReplyTarget(UUID player, UUID target) {
        replyTargets.put(player, target);
    }

    public UUID getReplyTarget(UUID player) {
        return replyTargets.get(player);
    }

    public void removeReplyTarget(UUID player) {
        replyTargets.remove(player);
    }
}
