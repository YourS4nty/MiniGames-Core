package dev.yours4nty.minigames.tnttag;

import dev.yours4nty.MinigamesCore;
import dev.yours4nty.managers.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.boss.*;

import java.util.*;

public class TNTTagArena {
    private final Arena baseArena;
    private final MinigamesCore plugin;

    private final Map<UUID, TNTTagPlayer> players = new HashMap<>();
    private TNTTagState state = TNTTagState.WAITING;
    private final BossBar bossBar;
    private TNTTagTask task;
    private TNTTagPlayer itPlayer;
    private int timer;

    public TNTTagArena(Arena arena, MinigamesCore plugin) {
        this.baseArena = arena;
        this.plugin = plugin;
        this.bossBar = Bukkit.createBossBar("Waiting...", BarColor.RED, BarStyle.SOLID, BarFlag.CREATE_FOG);
    }

    public void addPlayer(Player player) {
        TNTTagPlayer tntPlayer = new TNTTagPlayer(player, plugin);
        players.put(player.getUniqueId(), tntPlayer);
        bossBar.addPlayer(player);
        player.teleport(baseArena.getLobby());

        if (players.size() >= 2 && state == TNTTagState.WAITING) {
            startCountdown();
        } else {
            updateBossBar("tnttag.waiting", Map.of("count", String.valueOf(players.size())));
        }
    }

    public void removePlayer(Player player) {
        TNTTagPlayer removed = players.remove(player.getUniqueId());
        bossBar.removePlayer(player);

        if (removed != null) {
            removed.setAlive(false);
            removed.setIt(false);

            // Si se va el que tiene la bomba, reasignar otro
            if (removed == itPlayer && state == TNTTagState.RUNNING && getAlivePlayers().size() > 1) {
                pickRandomIt(false);
            }
        }

        // Revisión de final de partida
        if (state == TNTTagState.RUNNING && getAlivePlayers().size() <= 1) {
            Player winner = getAlivePlayers().isEmpty() ? null : getAlivePlayers().get(0).getPlayer();
            endGame(winner);
        }

        if (players.size() < 2 && state == TNTTagState.COUNTDOWN) {
            cancelCountdown();
            updateBossBar("tnttag.waiting", Map.of("count", String.valueOf(players.size())));
        }
    }

    private void startCountdown() {
        state = TNTTagState.COUNTDOWN;
        timer = 10;
        task = new TNTTagTask(this, plugin);
        task.runTaskTimer(plugin, 0L, 20L);
    }

    public void startGame() {
        state = TNTTagState.RUNNING;
        for (TNTTagPlayer p : players.values()) {
            p.getPlayer().teleport(baseArena.getSpawn());
        }
        pickRandomIt(true);
        updateBossBar("tnttag.running", Map.of("count", String.valueOf(players.size())));
    }

    private void broadcastItChange(Player newIt) {
        String message = plugin.getLanguageManager().get("tnttag.started", Map.of("it", newIt.getName()));
        for (TNTTagPlayer p : players.values()) {
            p.getPlayer().sendMessage(message);
        }
    }

    private void pickRandomIt(boolean first) {
        List<TNTTagPlayer> alive = getAlivePlayers();
        itPlayer = alive.get(new Random().nextInt(alive.size()));
        itPlayer.setIt(true);

        broadcastItChange(itPlayer.getPlayer());

        timer = first ? 10 : (3 + new Random().nextInt(6));
    }

    public void tagPlayer(Player from, Player to) {
        if (state != TNTTagState.RUNNING) return;
        if (itPlayer == null || !itPlayer.getPlayer().equals(from)) return;

        TNTTagPlayer newIt = players.get(to.getUniqueId());
        if (newIt == null || !newIt.isAlive()) return;

        itPlayer.setIt(false);
        itPlayer = newIt;
        itPlayer.setIt(true);

        broadcastItChange(itPlayer.getPlayer());

        timer = 3 + new Random().nextInt(6);
    }

    public void explodeIt() {
        if (itPlayer == null) return;
        Player exploded = itPlayer.getPlayer();
        exploded.getWorld().createExplosion(exploded.getLocation(), 0F);
        eliminatePlayer(exploded);
        itPlayer = null;

        if (getAlivePlayers().size() > 1) {
            pickRandomIt(false);
        } else {
            endGame(getAlivePlayers().isEmpty() ? null : getAlivePlayers().get(0).getPlayer());
        }
    }

    public TNTTagPlayer getPlayer(Player player) {
        return players.get(player.getUniqueId());
    }

    public void eliminatePlayer(Player player) {
        TNTTagPlayer p = players.get(player.getUniqueId());
        if (p == null) return;

        p.eliminate();
        p.setSpectator(true);
        player.teleport(baseArena.getSpawn());

        if (getAlivePlayers().size() == 1) {
            endGame(getAlivePlayers().get(0).getPlayer());
        }
    }

    public List<TNTTagPlayer> getAlivePlayers() {
        List<TNTTagPlayer> list = new ArrayList<>();
        for (TNTTagPlayer p : players.values()) {
            if (p.isAlive()) list.add(p);
        }
        return list;
    }

    public Map<UUID, TNTTagPlayer> getPlayers() {
        return players;
    }

    public void endGame(Player winner) {
        state = TNTTagState.ENDING;

        String winnerName = (winner != null) ? winner.getName() : "§cNull";
        updateBossBar("tnttag.gamewinstate", Map.of("player", winnerName));

        for (TNTTagPlayer p : players.values()) {
            Player pl = p.getPlayer();
            pl.sendTitle("§aWinner!", winnerName, 10, 60, 10);
            p.setSpectator(false);
            p.setIt(false);
            pl.teleport(baseArena.getLobby());
        }
        Bukkit.getScheduler().runTaskLater(plugin, this::resetArena, 100L);
    }

    public void resetArena() {
        this.state = TNTTagState.WAITING;
        bossBar.removeAll();
        bossBar.setVisible(true);
        updateBossBar("tnttag.waiting", Map.of("count", String.valueOf(0)));

        for (TNTTagPlayer tntPlayer : players.values()) {
            Player player = tntPlayer.getPlayer();
            tntPlayer.setAlive(true);
            tntPlayer.setIt(false);
            player.teleport(baseArena.getLobby());
            bossBar.addPlayer(player);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int lobbyCount = 0;
            for (TNTTagPlayer tntPlayer : players.values()) {
                Player player = tntPlayer.getPlayer();
                if (baseArena.isInLobbyZone(player.getLocation())) {
                    lobbyCount++;
                }
            }
            updateBossBar("tnttag.waiting", Map.of("count", String.valueOf(lobbyCount)));
            if (lobbyCount >= 2) {
                startCountdown();
            }
        }, 20L);
    }

    public void cancelCountdown() {
        state = TNTTagState.WAITING;
        if (task != null) task.cancel();
    }

    public void updateBossBar(String langKey, Map<String, String> placeholders) {
        String msg = plugin.getLanguageManager().get(langKey, placeholders);
        bossBar.setTitle(msg);
    }

    public TNTTagState getState() {
        return state;
    }

    public TNTTagPlayer getItPlayer() {
        return itPlayer;
    }

    public int getTimer() {
        return timer;
    }

    public Arena getBaseArena() {
        return baseArena;
    }

    public void decreaseTimer() {
        timer--;
    }
}
