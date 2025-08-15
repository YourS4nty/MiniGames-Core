package dev.yours4nty.minigames.tntrun;

import org.bukkit.Location;
import org.bukkit.entity.Item;

public class TNTPowerUp {
    private final PowerUpType type;
    private final Item entity;

    public TNTPowerUp(PowerUpType type, Item entity) {
        this.type = type;
        this.entity = entity;
    }

    public PowerUpType getType() {
        return type;
    }

    public Item getEntity() {
        return entity;
    }

    public Location getLocation() {
        return entity.getLocation();
    }
}
