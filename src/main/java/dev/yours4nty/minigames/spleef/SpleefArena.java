package dev.yours4nty.minigames.spleef;

import dev.yours4nty.MinigamesCore;
import dev.yours4nty.managers.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.boss.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SpleefArena {

    private final Arena baseArena;
    private final MinigamesCore plugin;

    private final Map<UUID, SpleefPlayer> players = new HashMap<>();
    private SpleefState state = SpleefState.WAITING;
    private final BossBar bossBar;
    private SpleefTask task;

    public SpleefArena(Arena arena, MinigamesCore plugin) {
        this.baseArena = arena;
        this.plugin = plugin;
        this.bossBar = Bukkit.createBossBar("Waiting...", BarColor.BLUE, BarStyle.SOLID, BarFlag.CREATE_FOG);
    }

    public void addPlayer(Player player) {
        SpleefPlayer spleefPlayer = new SpleefPlayer(player);
        players.put(player.getUniqueId(), spleefPlayer);
        bossBar.addPlayer(player);
        player.teleport(baseArena.getLobby());

        if (players.size() >= 2 && state == SpleefState.WAITING) {
            startCountdown();
        } else {
            updateBossBar("spleef.waiting", Map.of("count", String.valueOf(players.size())));
        }
    }

    public Map<UUID, SpleefPlayer> getPlayers() {
        return players;
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        bossBar.removePlayer(player);
        if (players.size() < 2 && state == SpleefState.COUNTDOWN) {
            cancelCountdown();
            updateBossBar("spleef.waiting", Map.of("count", String.valueOf(players.size())));
        }
    }

    private void startCountdown() {
        state = SpleefState.COUNTDOWN;
        task = new SpleefTask(this, plugin);
        task.runTaskTimer(plugin, 0L, 20L);
    }

    public void startGame() {
        state = SpleefState.RUNNING;
        for (SpleefPlayer p : players.values()) {
            Player pl = p.getPlayer();
            pl.teleport(baseArena.getSpawn());
            giveSpleefItem(pl);
        }
        updateBossBar("spleef.running", Map.of("count", String.valueOf(players.size())));
    }

    public void eliminatePlayer(Player player) {
        SpleefPlayer p = players.get(player.getUniqueId());
        if (p == null) return;

        removeSpleefItems(player);
        p.eliminate();
        player.teleport(baseArena.getLobby());

        if (getAlivePlayers().size() == 1) {
            endGame(getAlivePlayers().get(0).getPlayer());
        }
    }

    public void endGame(Player winner) {
        state = SpleefState.ENDING;
        updateBossBar("spleef.gamewinstate", Map.of("player", winner.getName()));

        for (SpleefPlayer p : players.values()) {
            Player pl = p.getPlayer();
            removeSpleefItems(pl);
            pl.sendTitle("§aWinner!", winner.getName(), 10, 60, 10);
            pl.teleport(baseArena.getLobby());
        }
        Bukkit.getScheduler().runTaskLater(plugin, this::resetArena, 100L);
    }

    public void resetArena() {
        this.state = SpleefState.WAITING;
        bossBar.removeAll();
        bossBar.setVisible(true);
        updateBossBar("spleef.waiting", Map.of("count", String.valueOf(0)));

        for (SpleefPlayer spleefPlayer : players.values()) {
            Player player = spleefPlayer.getPlayer();
            spleefPlayer.setAlive(true);
            player.teleport(baseArena.getLobby());
            bossBar.addPlayer(player);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int lobbyCount = (int) players.values().stream()
                    .map(SpleefPlayer::getPlayer)
                    .filter(pl -> baseArena.isInLobbyZone(pl.getLocation()))
                    .count();

            updateBossBar("spleef.waiting", Map.of("count", String.valueOf(lobbyCount)));

            if (lobbyCount >= 2) {
                startCountdown();
            }
        }, 20L);
    }

    public void cancelCountdown() {
        state = SpleefState.WAITING;
        if (task != null) task.cancel();
    }

    public List<SpleefPlayer> getAlivePlayers() {
        List<SpleefPlayer> list = new ArrayList<>();
        for (SpleefPlayer p : players.values()) {
            if (p.isAlive()) list.add(p);
        }
        return list;
    }

    public SpleefState getState() {
        return state;
    }

    public Arena getBaseArena() {
        return baseArena;
    }

    public void updateBossBar(String langKey, Map<String, String> placeholders) {
        String msg = plugin.getLanguageManager().get(langKey, placeholders);
        bossBar.setTitle(msg);
    }
    private void giveSpleefItem(Player player) {
        ItemStack shovel = new ItemStack(Material.STONE_SHOVEL, 1);
        player.getInventory().addItem(shovel);
    }

    private void removeSpleefItems(Player player) {
        player.getInventory().remove(Material.STONE_SHOVEL);
    }
}
