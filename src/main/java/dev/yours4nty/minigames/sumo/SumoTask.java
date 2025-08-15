package dev.yours4nty.minigames.sumo;

import dev.yours4nty.MinigamesCore;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class SumoTask extends BukkitRunnable {
    private final SumoArena arena;
    private int countdown = 10;

    public SumoTask(SumoArena arena, MinigamesCore plugin) {
        this.arena = arena;
        this.countdown = 10;
    }

    @Override
    public void run() {
        switch (arena.getState()) {
            case COUNTDOWN -> {
                if (countdown <= 0) {
                    cancel();
                    arena.startGame();
                } else {
                    arena.updateBossBar("sumo.countdown", Map.of("count", String.valueOf(countdown)));
                    countdown--;
                }
            }
            case RUNNING, ENDING, WAITING -> cancel();
        }
    }
}
