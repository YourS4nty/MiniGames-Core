package dev.yours4nty.minigames.spleef;

import dev.yours4nty.MinigamesCore;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class SpleefTask extends BukkitRunnable {

    private final SpleefArena arena;
    private int seconds = 10;

    public SpleefTask(SpleefArena arena, MinigamesCore plugin) {
        this.arena = arena;
    }

    @Override
    public void run() {
        if (arena.getAlivePlayers().size() < 2) {
            cancel();
            arena.cancelCountdown();
            return;
        }

        if (seconds <= 0) {
            cancel();
            arena.startGame();
            return;
        }

        arena.updateBossBar("spleef.countdown", Map.of("count", String.valueOf(seconds)));
        seconds--;
    }
}
