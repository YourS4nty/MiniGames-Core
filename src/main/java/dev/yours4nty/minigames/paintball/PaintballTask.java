package dev.yours4nty.minigames.paintball;

import dev.yours4nty.MinigamesCore;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class PaintballTask extends BukkitRunnable {
    private final PaintballArena arena;
    private final MinigamesCore plugin;

    public PaintballTask(PaintballArena arena, MinigamesCore plugin) {
        this.arena = arena;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        switch (arena.getState()) {
            case COUNTDOWN -> {
                if (arena.getTimer() <= 0) {
                    arena.startGame();
                } else {
                    arena.updateBossBar("paintball.countdown", Map.of("count", String.valueOf(arena.getTimer())));
                    arena.decreaseTimer();
                }
            }
            case RUNNING -> {}
            case ENDING, WAITING -> cancel();
        }
    }
}
