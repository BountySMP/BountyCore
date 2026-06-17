package com.bountysmp.bountyCore.teams;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team {
    private final UUID teamId;
    private String teamName;
    private UUID leaderId;
    private final List<UUID> members;
    private final long createdTime;
    private static final int MAX_MEMBERS = 10;

    public Team(UUID teamId, String teamName, UUID leaderId, long createdTime) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.leaderId = leaderId;
        this.members = new ArrayList<>();
        this.members.add(leaderId);
        this.createdTime = createdTime;
    }

    public Team(UUID teamId, String teamName, UUID leaderId, List<UUID> members, long createdTime) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.leaderId = leaderId;
        this.members = new ArrayList<>(members);
        this.createdTime = createdTime;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public UUID getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(UUID leaderId) {
        this.leaderId = leaderId;
    }

    public List<UUID> getMembers() {
        return new ArrayList<>(members);
    }

    public boolean addMember(UUID uuid) {
        if (members.size() >= MAX_MEMBERS) {
            return false;
        }
        if (members.contains(uuid)) {
            return false;
        }
        members.add(uuid);
        return true;
    }

    public boolean removeMember(UUID uuid) {
        return members.remove(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public boolean isLeader(UUID uuid) {
        return leaderId.equals(uuid);
    }

    public int getMemberCount() {
        return members.size();
    }

    public boolean isFull() {
        return members.size() >= MAX_MEMBERS;
    }

    public long getCreatedTime() {
        return createdTime;
    }
}
