package dev.yours4nty.minigames.tnttag;

import dev.yours4nty.MinigamesCore;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

public class TNTTagPlayer {
    private final Player player;
    private boolean alive = true;
    private boolean it = false;
    private boolean spectator = false;

    public TNTTagPlayer(Player player, MinigamesCore plugin) {
        this.player = player;
    }

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

    public Player getPlayer() {
        return player;
    }

    public boolean isAlive() {
        return alive;
    }

    public void eliminate() {
        alive = false;
        setIt(false);
    }

    public boolean isIt() {
        return it;
    }

    public void setIt(boolean it) {
        this.it = it;
        if (it) {
            player.getInventory().setItem(39, new ItemStack(Material.CREEPER_HEAD));
        } else {
            player.getInventory().setItem(39, null);
        }
        player.updateInventory();
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}