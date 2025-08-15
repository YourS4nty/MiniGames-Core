package dev.yours4nty.minigames.oitc;

import dev.yours4nty.MinigamesCore;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class OITCTask extends BukkitRunnable {

    private final OITCArena arena;
    private int seconds = 10;

    public OITCTask(OITCArena arena, MinigamesCore plugin) {
        this.arena = arena;
    }

    @Override
    public void run() {
        if (arena.getAlivePlayers().size() < 2) {
            cancel();
            arena.cancelCountdown();
            return;
        }

        if (seconds == 10) {
            arena.broadcast("oitc.instructions", Map.of());
        }

        if (seconds <= 0) {
            cancel();
            arena.startGame();
            return;
        }

        arena.updateBossBar("oitc.countdown", Map.of("count", String.valueOf(seconds)));
        seconds--;
    }
}
