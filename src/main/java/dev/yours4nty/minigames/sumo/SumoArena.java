package dev.yours4nty.minigames.sumo;

import dev.yours4nty.MinigamesCore;
import dev.yours4nty.managers.Arena;
import org.bukkit.Bukkit;
import org.bukkit.boss.*;
import org.bukkit.entity.Player;

import java.util.*;

public class SumoArena {
    private final Arena baseArena;
    private final MinigamesCore plugin;

    private final Map<UUID, SumoPlayer> players = new HashMap<>();
    private SumoState state = SumoState.WAITING;
    private final BossBar bossBar;
    private SumoTask task;

    public SumoArena(Arena arena, MinigamesCore plugin) {
        this.baseArena = arena;
        this.plugin = plugin;
        this.bossBar = Bukkit.createBossBar("Waiting...", BarColor.BLUE, BarStyle.SOLID, BarFlag.CREATE_FOG);
    }

    public void addPlayer(Player player) {
        SumoPlayer sp = new SumoPlayer(player);
        players.put(player.getUniqueId(), sp);
        bossBar.addPlayer(player);
        player.teleport(baseArena.getLobby());

        if (players.size() >= 2 && state == SumoState.WAITING) {
            startCountdown();
        } else {
            updateBossBar("sumo.waiting", Map.of("count", String.valueOf(players.size())));
        }
    }

    public boolean isPlayerInGame(Player player) {
        SumoPlayer runPlayer = players.get(player.getUniqueId());
        return runPlayer != null && runPlayer.isAlive();
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        bossBar.removePlayer(player);
        if (players.size() < 2 && state == SumoState.COUNTDOWN) {
            cancelCountdown();
            updateBossBar("sumo.waiting", Map.of("count", String.valueOf(players.size())));
        }
    }

    private void startCountdown() {
        if (task != null && !task.isCancelled()) return;
        state = SumoState.COUNTDOWN;
        task = new SumoTask(this, plugin);
        task.runTaskTimer(plugin, 0L, 20L);
    }

    public void startGame() {
        state = SumoState.RUNNING;
        updateBossBar(plugin.getLanguageManager().get("sumo.running"), Map.of());
        for (SumoPlayer sp : players.values()) {
            sp.getPlayer().teleport(baseArena.getSpawn());
        }
    }

    public Map<UUID, SumoPlayer> getPlayers() {
        return players;
    }

    public void eliminatePlayer(Player player) {
        SumoPlayer sp = players.get(player.getUniqueId());
        if (sp == null) return;
        sp.eliminate();
        sp.setSpectator(true);
        player.teleport(baseArena.getSpawn());
        broadcast(plugin.getLanguageManager().get("sumo.eliminated", Map.of("player", player.getName())));

        if (getAlivePlayers().size() == 1) {
            endGame(getAlivePlayers().get(0).getPlayer());
        }
    }

    public List<SumoPlayer> getAlivePlayers() {
        List<SumoPlayer> list = new ArrayList<>();
        for (SumoPlayer p : players.values()) {
            if (p.isAlive()) list.add(p);
        }
        return list;
    }

    public void endGame(Player winner) {
        state = SumoState.ENDING;
        updateBossBar("sumo.gamewinstate", Map.of("player", winner.getName()));

        for (SumoPlayer sp : players.values()) {
            Player pl = sp.getPlayer();
            sp.setSpectator(false);
            pl.sendTitle("§aWinner!", winner.getName(), 10, 60, 10);
            pl.teleport(baseArena.getLobby());
        }
        Bukkit.getScheduler().runTaskLater(plugin, this::resetArena, 100L);
    }

    public void resetArena() {
        this.state = SumoState.WAITING;
        bossBar.removeAll();
        bossBar.setVisible(true);
        updateBossBar("sumo.waiting", Map.of("count", String.valueOf(0)));

        for (SumoPlayer sp : players.values()) {
            sp.setAlive(true);
            sp.setSpectator(false);
            Player player = sp.getPlayer();
            player.teleport(baseArena.getLobby());
            bossBar.addPlayer(player);
        }

        bossBar.setVisible(true);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            long lobbyCount = players.values().stream()
                    .map(SumoPlayer::getPlayer)
                    .filter(p -> baseArena.isInLobbyZone(p.getLocation()))
                    .count();
            updateBossBar("sumo.waiting", Map.of("count", String.valueOf(lobbyCount)));
            if (lobbyCount >= 2) {
                startCountdown();
            }
        }, 20L);
    }

    public void cancelCountdown() {
        state = SumoState.WAITING;
        if (task != null) task.cancel();
    }

    public void broadcast(String message) {
        for (SumoPlayer sp : players.values()) {
            sp.getPlayer().sendMessage(message);
        }
    }

    public Arena getBaseArena() {
        return baseArena;
    }

    public SumoState getState() {
        return state;
    }

    public void updateBossBar(String langKey, Map<String, String> placeholders) {
        bossBar.setTitle(plugin.getLanguageManager().get(langKey, placeholders));
    }
}
