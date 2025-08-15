package dev.yours4nty.minigames.spleef;

import org.bukkit.entity.Player;

public class SpleefPlayer {
    private final Player player;
    private boolean alive;

    public SpleefPlayer(Player player) {
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

    public void eliminate() {
        this.alive = false;
    }
}
