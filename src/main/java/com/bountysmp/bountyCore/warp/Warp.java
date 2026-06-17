package com.bountysmp.bountyCore.warp;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.UUID;

public class Warp {
    private final String name;
    private Location location;
    private final UUID creatorUuid;
    private Material iconMaterial;
    private final long creationTime;

    public Warp(String name, Location location, UUID creatorUuid, Material iconMaterial, long creationTime) {
        this.name = name;
        this.location = location;
        this.creatorUuid = creatorUuid;
        this.iconMaterial = iconMaterial;
        this.creationTime = creationTime;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public UUID getCreatorUuid() {
        return creatorUuid;
    }

    public Material getIconMaterial() {
        return iconMaterial;
    }

    public void setIconMaterial(Material iconMaterial) {
        this.iconMaterial = iconMaterial;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
