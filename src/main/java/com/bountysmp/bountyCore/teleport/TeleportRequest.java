package com.bountysmp.bountyCore.teleport;

import java.util.UUID;

public class TeleportRequest {
    private final UUID requester;
    private final UUID target;
    private final long expireTime;
    private final RequestType type;

    public enum RequestType {
        TPA,      // requester wants to teleport TO target
        TPAHERE   // requester wants target to teleport TO requester
    }

    public TeleportRequest(UUID requester, UUID target, long expireTimeMillis, RequestType type) {
        this.requester = requester;
        this.target = target;
        this.expireTime = System.currentTimeMillis() + expireTimeMillis;
        this.type = type;
    }

    public UUID getRequester() {
        return requester;
    }

    public UUID getTarget() {
        return target;
    }

    public RequestType getType() {
        return type;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expireTime;
    }
}
