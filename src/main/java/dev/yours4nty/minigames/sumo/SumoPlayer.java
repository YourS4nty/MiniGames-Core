package dev.yours4nty.minigames.sumo;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class SumoPlayer {
    private final Player player;
    private boolean alive = true;
    private boolean spectator = false;

    public boolean isSpectator() {
        return spectator;
    }

    public void setSpectator(boolean spectator) {
        this.spectator = spectator;
        if (spectator) {
            player.setGameMode(GameMode.SPECTATOR);
        } else {
            player.setGameMode(GameMode.ADVENTURE);
        }
    }

    public SumoPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isAlive() {
        return alive;
    }

    public void eliminate() {
        this.alive = false;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
