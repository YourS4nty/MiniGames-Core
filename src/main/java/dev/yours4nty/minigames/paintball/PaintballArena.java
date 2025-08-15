package dev.yours4nty.minigames.paintball;

import dev.yours4nty.MinigamesCore;
import dev.yours4nty.managers.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PaintballArena {
    private final Arena baseArena;
    private final MinigamesCore plugin;
    private final Map<UUID, PaintballPlayer> players = new HashMap<>();
    private PaintballState state = PaintballState.WAITING;
    private final BossBar bossBar;
    private PaintballTask task;
    private int timer;

    public PaintballArena(Arena arena, MinigamesCore plugin) {
        this.baseArena = arena;
        this.plugin = plugin;
        this.bossBar = Bukkit.createBossBar("Waiting...", BarColor.WHITE, BarStyle.SOLID, BarFlag.CREATE_FOG);
    }

    public void addPlayer(Player player) {
        PaintballPlayer pbPlayer = new PaintballPlayer(player);
        players.put(player.getUniqueId(), pbPlayer);
        bossBar.addPlayer(player);
        player.teleport(baseArena.getLobby());

        giveItems(player);

        if (players.size() >= 2 && state == PaintballState.WAITING) {
            startCountdown();
        } else {
            updateBossBar("paintball.waiting", Map.of("count", String.valueOf(players.size())));
        }
    }

    public Map<UUID, PaintballPlayer> getPlayers() {
        return players;
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        bossBar.removePlayer(player);
        if (players.size() < 2 && state == PaintballState.COUNTDOWN) {
            cancelCountdown();
            updateBossBar("paintball.waiting", Map.of("count", String.valueOf(players.size())));
        }
    }

    private void startCountdown() {
        state = PaintballState.COUNTDOWN;
        timer = 10;
        task = new PaintballTask(this, plugin);
        task.runTaskTimer(plugin, 0L, 20L);
    }

    public void startGame() {
        state = PaintballState.RUNNING;
        for (PaintballPlayer p : players.values()) {
            p.resetStats();
            p.getPlayer().teleport(baseArena.getSpawn());
        }
        updateBossBar("paintball.running", Map.of("count", String.valueOf(players.size())));
    }

    public void eliminatePlayer(Player player) {
        PaintballPlayer pb = players.get(player.getUniqueId());
        if (pb == null) return;

        pb.setAlive(false);
        player.teleport(baseArena.getLobby());

        if (getAlivePlayers().size() == 1) {
            Player winner = getAlivePlayers().get(0).getPlayer();
            endGame(winner);
        }
    }

    public List<PaintballPlayer> getAlivePlayers() {
        List<PaintballPlayer> alive = new ArrayList<>();
        for (PaintballPlayer p : players.values()) {
            if (p.isAlive()) alive.add(p);
        }
        return alive;
    }

    public void endGame(Player winner) {
        state = PaintballState.ENDING;
        updateBossBar("paintball.win", Map.of("player", winner.getName()));

        for (PaintballPlayer p : players.values()) {
            Player pl = p.getPlayer();
            removePaintballItems(pl);
            pl.sendTitle("§aWinner!", winner.getName(), 10, 60, 10);
            pl.teleport(baseArena.getLobby());
        }
        Bukkit.getScheduler().runTaskLater(plugin, this::resetArena, 100L);
    }

    public void resetArena() {
        this.state = PaintballState.WAITING;
        bossBar.removeAll();
        bossBar.setVisible(true);
        updateBossBar("paintball.waiting", Map.of("count", "0"));

        for (PaintballPlayer p : players.values()) {
            p.resetStats();
            Player pl = p.getPlayer();
            pl.teleport(baseArena.getLobby());
            bossBar.addPlayer(pl);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            long lobbyCount = players.values().stream()
                    .filter(p -> baseArena.isInLobbyZone(p.getPlayer().getLocation()))
                    .count();
            updateBossBar("paintball.waiting", Map.of("count", String.valueOf(lobbyCount)));
            if (lobbyCount >= 2) startCountdown();
        }, 20L);
    }

    public void cancelCountdown() {
        state = PaintballState.WAITING;
        if (task != null) task.cancel();
    }

    public void updateBossBar(String langKey, Map<String, String> placeholders) {
        String msg = plugin.getLanguageManager().get(langKey, placeholders);
        bossBar.setTitle(msg);
    }

    public PaintballState getState() {
        return state;
    }

    public int getTimer() {
        return timer;
    }

    public void decreaseTimer() {
        timer--;
    }

    private void giveItems(Player player) {
        ItemStack gun = new ItemStack(Material.BLAZE_ROD);
        ItemMeta gunMeta = gun.getItemMeta();
        assert gunMeta != null;
        gunMeta.setDisplayName("§cPaint-Gun");
        gun.setItemMeta(gunMeta);
        player.getInventory().setItem(0, gun);

        ItemStack feather = new ItemStack(Material.FEATHER);
        ItemMeta featherMeta = feather.getItemMeta();
        assert featherMeta != null;
        featherMeta.setDisplayName("§bJump");
        feather.setItemMeta(featherMeta);
        player.getInventory().setItem(1, feather);

        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        ItemMeta bootsMeta = boots.getItemMeta();
        assert bootsMeta != null;
        bootsMeta.setDisplayName("§bSpeed");
        boots.setItemMeta(bootsMeta);
        player.getInventory().setItem(2, boots);
    }
    private void removePaintballItems(Player player) {
        player.getInventory().remove(Material.BLAZE_ROD);
        player.getInventory().remove(Material.FEATHER);
        player.getInventory().remove(Material.RABBIT_FOOT);
    }

}
