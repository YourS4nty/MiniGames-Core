package dev.yours4nty.minigames.tnttag;

import dev.yours4nty.MinigamesCore;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Map;

public class TNTTagTask extends BukkitRunnable {
    private final TNTTagArena arena;

    public TNTTagTask(TNTTagArena arena, MinigamesCore plugin) {
        this.arena = arena;
    }

    @Override
    public void run() {
        switch (arena.getState()) {
            case COUNTDOWN -> {
                if (arena.getTimer() <= 0) {
                    arena.startGame();
                } else {
                    arena.updateBossBar("tnttag.countdown", Map.of("count", String.valueOf(arena.getTimer())));
                    arena.decreaseTimer();
                }
            }
            case RUNNING -> {
                if (arena.getTimer() <= 0) {
                    arena.explodeIt();
                } else {
                    arena.updateBossBar("tnttag.it", Map.of("time", String.valueOf(arena.getTimer())));
                    arena.decreaseTimer();
                }
            }
            case ENDING, WAITING -> cancel();
        }
    }
}