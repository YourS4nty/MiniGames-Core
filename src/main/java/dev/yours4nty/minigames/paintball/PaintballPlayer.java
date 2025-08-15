package dev.yours4nty.minigames.paintball;

import org.bukkit.entity.Player;

public class PaintballPlayer {
    private final Player player;
    private boolean alive = true;
    private int hits = 0;

    public PaintballPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
        this.hits = 0;
    }

    public int getHits() {
        return hits;
    }

    public void addHit() {
        hits++;
    }

    public void resetStats() {
        alive = true;
        hits = 0;
    }
}
