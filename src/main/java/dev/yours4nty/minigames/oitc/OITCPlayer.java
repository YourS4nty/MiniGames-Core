package dev.yours4nty.minigames.oitc;

import org.bukkit.entity.Player;

public class OITCPlayer {
    private final Player player;
    private int kills = 0;
    private int lives;

    public OITCPlayer(Player player, int lives) {
        this.player = player;
        this.lives = lives;
    }

    public void reset(int lives) {
        this.kills = 0;
        this.lives = lives;
    }

    public Player getPlayer() {
        return player;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        kills++;
    }

    public int getLives() {
        return lives;
    }

    public void loseLife() {
        lives--;
    }

    public boolean isAlive() {
        return lives > 0;
    }
}
