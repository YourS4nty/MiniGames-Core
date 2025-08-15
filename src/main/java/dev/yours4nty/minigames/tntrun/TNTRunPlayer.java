package dev.yours4nty.minigames.tntrun;

import org.bukkit.entity.Player;

public class TNTRunPlayer {
    private final Player player;
    private boolean alive;
    private boolean spectator;
    private boolean hasSecondChancePowerUp = false;
    private boolean usedSecondChance = false;

    public TNTRunPlayer(Player player) {
        this.player = player;
        this.alive = true;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void setSpectator(boolean spectator) {
        this.spectator = spectator;
    }

    public boolean isSpectator() {
        return spectator;
    }

    public void eliminate() {
        this.alive = false;
        this.spectator = true;
    }

    public boolean hasSecondChancePowerUp() {
        return hasSecondChancePowerUp;
    }

    public void grantSecondChancePowerUp() {
        hasSecondChancePowerUp = true;
    }

    public boolean hasUsedSecondChance() {
        return usedSecondChance;
    }

    public void useSecondChance() {
        this.usedSecondChance = true;
    }

    public void resetSecondChancePowerUp() {
        this.hasSecondChancePowerUp = false;
    }
}
