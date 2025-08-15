package dev.yours4nty.minigames.oitc;

import dev.yours4nty.MinigamesCore;
import dev.yours4nty.managers.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.boss.*;
import java.util.*;

public class OITCArena {
    private final Arena baseArena;
    private final MinigamesCore plugin;
    private final Map<UUID, OITCPlayer> players = new HashMap<>();
    private OITCState state = OITCState.WAITING;
    private final BossBar bossBar;
    private OITCTask task;
    private final int maxKills;
    private final int lives;

    public OITCArena(Arena arena, MinigamesCore plugin) {
        this.baseArena = arena;
        this.plugin = plugin;
        this.maxKills = plugin.getConfig().getInt("oitc.max-kills", 20);
        this.lives = plugin.getConfig().getInt("oitc.lives", 5);
        this.bossBar = Bukkit.createBossBar("Waiting...", BarColor.YELLOW, BarStyle.SOLID, BarFlag.CREATE_FOG);
    }

    public void addPlayer(Player player) {

        if (state == OITCState.ENDING) {
            player.sendMessage("§cLa arena está reiniciando...");
            return;
        }

        OITCPlayer oitcPlayer = new OITCPlayer(player, lives);
        players.put(player.getUniqueId(), oitcPlayer);
        bossBar.addPlayer(player);
        player.teleport(baseArena.getLobby());
        player.sendMessage(plugin.getLanguageManager().get("oitc.joined", Map.of()));

        if (players.size() >= 2 && state == OITCState.WAITING) {
            startCountdown();
        } else {
            updateBossBar("oitc.waiting", Map.of("count", String.valueOf(players.size())));
        }
    }

    public Map<UUID, OITCPlayer> getPlayers() {
        return players;
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        bossBar.removePlayer(player);
        player.sendMessage(plugin.getLanguageManager().get("oitc.left", Map.of()));

        if (state == OITCState.COUNTDOWN && players.size() < 2) {
            cancelCountdown();
        }
        if (state == OITCState.WAITING || state == OITCState.COUNTDOWN) {
            updateBossBar("oitc.waiting", Map.of("count", String.valueOf(players.size())));
        }
    }

    public void broadcast(String langKey, Map<String, String> placeholders) {
        String message = plugin.getLanguageManager().get(langKey, placeholders);
        for (OITCPlayer p : players.values()) {
            p.getPlayer().sendMessage(message);
        }
    }

    private void startCountdown() {
        state = OITCState.COUNTDOWN;
        task = new OITCTask(this, plugin);
        task.runTaskTimer(plugin, 0L, 20L);
    }

    public void startGame() {
        state = OITCState.RUNNING;
        for (OITCPlayer p : players.values()) {
            Player pl = p.getPlayer();
            pl.teleport(baseArena.getSpawn());
            pl.getInventory().addItem(new ItemStack(Material.BOW, 1));
            pl.getInventory().addItem(new ItemStack(Material.ARROW, 1));
        }
        updateBossBar("oitc.running", Map.of("count", String.valueOf(players.size())));
    }

    public void onKill(Player killer, Player victim) {
        OITCPlayer killerData = players.get(killer.getUniqueId());
        OITCPlayer victimData = players.get(victim.getUniqueId());
        if (killerData == null || victimData == null) return;

        killerData.addKill();
        victimData.loseLife();

        killer.getInventory().addItem(new ItemStack(Material.ARROW, 1));
        victim.teleport(baseArena.getSpawn());

        broadcast("oitc.kill", Map.of(
                "killer", killer.getName(),
                "victim", victim.getName(),
                "victim_lives", String.valueOf(victimData.getLives())
        ));

        if (!victimData.isAlive()) {
            victim.sendMessage(plugin.getLanguageManager().get("oitc.eliminated", Map.of()));
            checkForWinner();
        }

        if (killerData.getKills() >= maxKills) {
            endGame(killer);
        }
    }

    public void checkForWinner() {
        List<OITCPlayer> alive = getAlivePlayers();
        if (alive.size() == 1) {
            endGame(alive.get(0).getPlayer());
        }
    }

    public void endGame(Player winner) {
        state = OITCState.ENDING;
        updateBossBar("oitc.gamewinstate", Map.of("player", winner.getName()));

        for (OITCPlayer p : players.values()) {
            Player pl = p.getPlayer();
            pl.getInventory().remove(Material.BOW);
            pl.getInventory().remove(Material.ARROW);
            pl.sendTitle("§aWinner!", winner.getName(), 10, 60, 10);
            pl.teleport(baseArena.getLobby());
        }
        Bukkit.getScheduler().runTaskLater(plugin, this::resetArena, 100L);
    }

    public void resetArena() {
        this.state = OITCState.WAITING;
        bossBar.removeAll();
        bossBar.setVisible(true);

        players.values().removeIf(p -> !p.getPlayer().isOnline());

        for (OITCPlayer oitcPlayer : players.values()) {
            oitcPlayer.reset(lives);
            Player player = oitcPlayer.getPlayer();
            player.teleport(baseArena.getLobby());
            bossBar.addPlayer(player);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            long lobbyCount = players.values().stream()
                    .filter(p -> p.getPlayer().isOnline() && baseArena.isInLobbyZone(p.getPlayer().getLocation()))
                    .count();

            updateBossBar("oitc.waiting", Map.of("count", String.valueOf(lobbyCount)));

            if (state == OITCState.WAITING && lobbyCount >= 2) {
                startCountdown();
            }
        }, 20L);
    }

    public void cancelCountdown() {
        state = OITCState.WAITING;
        if (task != null) task.cancel();
    }

    public List<OITCPlayer> getAlivePlayers() {
        List<OITCPlayer> list = new ArrayList<>();
        for (OITCPlayer p : players.values()) {
            if (p.isAlive()) list.add(p);
        }
        return list;
    }

    public OITCState getState() {
        return state;
    }

    public Arena getBaseArena() {
        return baseArena;
    }

    public void updateBossBar(String langKey, Map<String, String> placeholders) {
        String msg = plugin.getLanguageManager().get(langKey, placeholders);
        bossBar.setTitle(msg);
    }
}
