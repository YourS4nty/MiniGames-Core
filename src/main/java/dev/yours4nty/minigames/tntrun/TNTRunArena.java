package dev.yours4nty.minigames.tntrun;

import dev.yours4nty.MinigamesCore;
import dev.yours4nty.managers.Arena;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BarFlag;

import java.util.*;

public class TNTRunArena {
    private final Arena baseArena;
    private final MinigamesCore plugin;

    private final Map<UUID, TNTRunPlayer> players = new HashMap<>();
    private final Map<Location, Material> originalBlocks = new HashMap<>();
    private TNTRunState state = TNTRunState.WAITING;
    private final BossBar bossBar;
    private TNTRunTask task;
    private final List<TNTPowerUp> activePowerUps = new ArrayList<>();

    public List<TNTPowerUp> getActivePowerUps() {
        return activePowerUps;
    }

    public TNTRunArena(Arena arena, MinigamesCore plugin) {
        this.baseArena = arena;
        this.plugin = plugin;
        this.bossBar = Bukkit.createBossBar("Waiting...", BarColor.WHITE, BarStyle.SOLID, BarFlag.CREATE_FOG);
    }

    public void addPlayer(Player player) {
        TNTRunPlayer runPlayer = new TNTRunPlayer(player);
        players.put(player.getUniqueId(), runPlayer);
        bossBar.addPlayer(player);
        player.teleport(baseArena.getLobby());

        if (players.size() >= 2 && state == TNTRunState.WAITING) {
            startCountdown();
        } else {
            updateBossBar("tntrun.waiting", players.size());
        }
    }

    public void removePlayer(Player player) {
        TNTRunPlayer runPlayer = players.remove(player.getUniqueId());
        bossBar.removePlayer(player);

        if (runPlayer != null) {
            runPlayer.setAlive(false);
            runPlayer.setSpectator(true);
        }

        if (state == TNTRunState.RUNNING && getAlivePlayers().size() == 1) {
            Player winner = getAlivePlayers().get(0).getPlayer();
            endGame(winner);
        }

        if (players.size() < 2 && state == TNTRunState.COUNTDOWN) {
            cancelCountdown();
            updateBossBar("tntrun.waiting", players.size());
        }
    }

    public Map<UUID, TNTRunPlayer> getPlayers() {
        return players;
    }

    private void startCountdown() {
        state = TNTRunState.COUNTDOWN;
        task = new TNTRunTask(this, plugin, plugin.getTNTRunGame().getManager());
        task.runTaskTimer(plugin, 0L, 20L);
    }

    public void startGame() {
        cachePlayzone();
        state = TNTRunState.RUNNING;
        for (TNTRunPlayer p : players.values()) {
            p.getPlayer().teleport(baseArena.getSpawn());
        }
        int interval = plugin.getConfig().getInt("tntrun.powerup-spawn-interval", 40);
        int chance = plugin.getConfig().getInt("tntrun.powerup-spawn-chance", 50);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (getState() != TNTRunState.RUNNING) return;
            if (Math.random() * 100 < chance) {
                task.spawnRandomPowerUp();
            }
        }, interval, interval);

        updateBossBar("tntrun.running", players.size());
    }

    public boolean isPlayerInGame(Player player) {
        TNTRunPlayer runPlayer = players.get(player.getUniqueId());
        return runPlayer != null && runPlayer.isAlive();
    }

    public void cachePlayzone() {
        if (getBaseArena().getPlayzone1() == null || getBaseArena().getPlayzone2() == null) return;

        Location pos1 = getBaseArena().getPlayzone1();
        Location pos2 = getBaseArena().getPlayzone2();

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        originalBlocks.clear();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location loc = new Location(pos1.getWorld(), x, y, z);
                    Material mat = loc.getBlock().getType();
                    if (mat != Material.AIR) {
                        originalBlocks.put(loc, mat);
                    }
                }
            }
        }
    }

    public void restorePlayzone() {
        for (Map.Entry<Location, Material> entry : originalBlocks.entrySet()) {
            entry.getKey().getBlock().setType(entry.getValue());
        }
    }

    public void clearPowerUps() {
        for (TNTPowerUp powerUp : activePowerUps) {
            if (powerUp.getEntity() != null && !powerUp.getEntity().isDead()) {
                powerUp.getEntity().remove();
            }
        }
        activePowerUps.clear();
    }

    public void endGame(Player winner) {
        state = TNTRunState.ENDING;
        bossBar.setTitle(plugin.getLanguageManager().get("tntrun.gamewinstate"));
        restorePlayzone();

        clearPowerUps();

        for (TNTRunPlayer p : players.values()) {
            Player pl = p.getPlayer();
            pl.setGameMode(GameMode.SURVIVAL);
            pl.showPlayer(plugin, pl);
            pl.sendTitle("§aWinner!", winner.getName(), 10, 60, 10);
            pl.teleport(baseArena.getLobby());
            p.setSpectator(false);
            p.setAlive(true);
        }

        Bukkit.getScheduler().runTaskLater(plugin, this::resetArena, 100L);
    }

    public TNTRunPlayer getTNTRunPlayer(Player player) {
        return players.get(player.getUniqueId());
    }

    public void eliminatePlayer(Player player) {
        TNTRunPlayer p = players.get(player.getUniqueId());
        if (p == null) return;

        p.eliminate();
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(baseArena.getSpawn());

        if (getAlivePlayers().size() == 1) {
            Player winner = getAlivePlayers().get(0).getPlayer();
            endGame(winner);
        }
    }

    public List<TNTRunPlayer> getAlivePlayers() {
        List<TNTRunPlayer> list = new ArrayList<>();
        for (TNTRunPlayer p : players.values()) {
            if (p.isAlive()) list.add(p);
        }
        return list;
    }

    public void cancelCountdown() {
        state = TNTRunState.WAITING;
        if (task != null) task.cancel();
    }

    public Arena getBaseArena() {
        return baseArena;
    }

    public TNTRunState getState() {
        return state;
    }

    public void updateBossBar(String langKey, int count) {
        String msg = plugin.getLanguageManager().get(langKey, Map.of("count", String.valueOf(count)));
        bossBar.setTitle(msg);
    }

    public void resetArena() {
        this.state = TNTRunState.WAITING;
        bossBar.removeAll();
        bossBar.setVisible(true);
        updateBossBar("tntrun.waiting", 0);

        for (TNTRunPlayer runPlayer : players.values()) {
            Player player = runPlayer.getPlayer();
            runPlayer.setAlive(true);
            player.teleport(baseArena.getLobby());
            bossBar.addPlayer(player);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int lobbyCount = 0;
            for (TNTRunPlayer runPlayer : players.values()) {
                Player player = runPlayer.getPlayer();
                if (baseArena.isInLobbyZone(player.getLocation())) {
                    lobbyCount++;
                }
            }

            updateBossBar("tntrun.waiting", lobbyCount);

            if (lobbyCount >= 2) {
                startCountdown();
            }
        }, 20L);
    }
}
