package dev.yours4nty.minigames.buildbattle;

import dev.yours4nty.MinigamesCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public class BuildBattleTask extends BukkitRunnable {
    private final BuildBattleArena arena;
    private final MinigamesCore plugin;
    private int endingTimer = 5;

    public BuildBattleTask(BuildBattleArena arena, MinigamesCore plugin) {
        this.arena = arena;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        switch (arena.getState()) {
            case COUNTDOWN -> {
                if (arena.getTimer() <= 0) {
                    arena.startBuildPhase();
                } else {
                    arena.updateBossBar("buildbattle.countdown",
                            Map.of("time", String.valueOf(arena.getTimer())));
                    arena.decreaseTimer();
                }
            }
            case BUILDING -> {
                if (arena.getTimer() <= 0) {
                    arena.startVoting();
                } else {
                    arena.updateBossBar("buildbattle.building-time",
                            Map.of("time", String.valueOf(arena.getTimer())));
                    arena.decreaseTimer();
                }
            }
            case VOTING -> {
                if (arena.getTimer() <= 0) {
                    UUID builder = arena.getPlayers().get(arena.getVotingIndex());
                    arena.tallyVotes(builder);
                    arena.setVotingIndex(arena.getVotingIndex() + 1);
                    arena.nextVote();
                } else {
                    UUID builder = arena.getPlayers().get(arena.getVotingIndex());
                    Player builderPlayer = Bukkit.getPlayer(builder);
                    String builderName = builderPlayer != null ? builderPlayer.getName() : "N/A";

                    arena.updateBossBar("buildbattle.voting", Map.of(
                            "time", String.valueOf(arena.getTimer()),
                            "player", builderName
                    ));
                    arena.decreaseTimer();
                }
            }
            case ENDING -> {
                if (endingTimer <= 0) {
                    arena.resetArena();
                    endingTimer = 5;
                } else {
                    arena.updateBossBar("buildbattle.ending",
                            Map.of("time", String.valueOf(endingTimer)));
                    endingTimer--;
                }
            }
            case WAITING -> {
            }
        }
    }
}
