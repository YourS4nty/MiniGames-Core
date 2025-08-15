package dev.yours4nty.minigames.buildbattle;

import dev.yours4nty.MinigamesCore;
import dev.yours4nty.managers.Arena;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import java.util.*;

public class BuildBattleArena {

    private final Arena baseArena;
    private final MinigamesCore plugin;
    private final Map<UUID, Integer> scores = new HashMap<>();
    private final Map<UUID, Location> playerPlots = new HashMap<>();
    private final Map<UUID, PlotRegion> plotRegions = new HashMap<>();
    private final Map<UUID, Integer> votes = new HashMap<>();
    private final List<UUID> players = new ArrayList<>();
    private final BossBar bossBar;

    private BuildBattleState state = BuildBattleState.WAITING;
    private int timer;
    private BukkitTask countdownTask;
    private int votingIndex = 0;

    public BuildBattleArena(Arena baseArena, MinigamesCore plugin) {
        this.baseArena = baseArena;
        this.plugin = plugin;
        this.bossBar = Bukkit.createBossBar("Waiting...", BarColor.PINK, BarStyle.SOLID, BarFlag.CREATE_FOG);
    }

    public static class PlotRegion {
        public final Location corner1;
        public final Location corner2;
        public final List<Location> roofBlocks;

        public PlotRegion(Location c1, Location c2, List<Location> roof) {
            this.corner1 = c1;
            this.corner2 = c2;
            this.roofBlocks = roof;
        }
    }

    public void addPlayer(Player player) {
        if (state != BuildBattleState.WAITING) {
            player.sendMessage(ChatColor.RED + plugin.getLanguageManager().get("buildbattle.cannot-join"));
            return;
        }
        players.add(player.getUniqueId());
        scores.put(player.getUniqueId(), 0);
        bossBar.addPlayer(player);
        player.teleport(baseArena.getLobby());
        updateBossBar("buildbattle.waiting", Map.of("count", String.valueOf(players.size())));
        if (players.size() >= 2) startCountdown();
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        bossBar.removePlayer(player);
        if (state == BuildBattleState.COUNTDOWN && players.size() < 2) cancelCountdown();
    }

    private void startCountdown() {
        state = BuildBattleState.COUNTDOWN;
        timer = 10;
        countdownTask = new BuildBattleTask(this, plugin).runTaskTimer(plugin, 0L, 20L);
    }

    private void cancelCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        state = BuildBattleState.WAITING;
    }

    public void startBuildPhase() {
        state = BuildBattleState.BUILDING;
        List<String> themes = plugin.getConfig().getStringList("buildbattle.themes");
        String currentTheme = themes.isEmpty() ? "Tema desconocido"
                : themes.get(new Random().nextInt(themes.size()));

        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendMessage(ChatColor.AQUA + plugin.getLanguageManager()
                        .get("buildbattle.theme-selected", Map.of("theme", currentTheme)));
                p.sendTitle(ChatColor.YELLOW + "Tema:", ChatColor.WHITE + currentTheme, 10, 70, 20);
            }
        }

        generatePlots();
        timer = plugin.getConfig().getInt("buildbattle.build-time", 180);
    }

    public void startVoting() {
        state = BuildBattleState.VOTING;
        votingIndex = 0;
        votes.clear();
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null)
                p.sendMessage(plugin.getLanguageManager().get("buildbattle.start-voting"));
        }
        nextVote();
    }

    public void nextVote() {
        if (votingIndex >= players.size()) {
            announceWinner();
            return;
        }
        UUID builder = players.get(votingIndex);
        Location plotCenter = playerPlots.get(builder);
        Player builderPlayer = Bukkit.getPlayer(builder);

        if (builderPlayer == null) {
            votingIndex++;
            nextVote();
            return;
        }

        for (UUID uuid : players) {
            Player voter = Bukkit.getPlayer(uuid);
            if (voter == null || uuid.equals(builder)) continue;
            voter.sendMessage(plugin.getLanguageManager().get("buildbattle.voting-next"));

            for (int i = 1; i <= 5; i++) {
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.YELLOW + "Votar " + i);
                item.setItemMeta(meta);
                voter.getInventory().setItem(i - 1, item);
            }
            voter.teleport(plotCenter.clone().add(0, 1, 0));
        }
        timer = plugin.getConfig().getInt("buildbattle.vote-time", 30);
    }

    public void castVote(Player voter, int score) {
        if (state != BuildBattleState.VOTING) return;
        if (votes.containsKey(voter.getUniqueId())) {
            voter.sendMessage(plugin.getLanguageManager().get("buildbattle.vote-already-cast"));
            return;
        }
        UUID builder = players.get(votingIndex);
        if (voter.getUniqueId().equals(builder)) {
            voter.sendMessage(plugin.getLanguageManager().get("buildbattle.vote-own-build"));
            return;
        }
        votes.put(voter.getUniqueId(), score);
        voter.sendMessage(ChatColor.GREEN + plugin.getLanguageManager()
                .get("buildbattle.vote-cast", Map.of("score", String.valueOf(score))));
    }

    void tallyVotes(UUID builder) {
        int total = votes.values().stream().mapToInt(Integer::intValue).sum();
        scores.put(builder, scores.getOrDefault(builder, 0) + total);
        votes.clear();
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.getInventory().remove(Material.PAPER);
        }
    }

    private void announceWinner() {
        state = BuildBattleState.ENDING;

        UUID winner = scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);
        Player winPlayer = winner != null ? Bukkit.getPlayer(winner) : null;
        String winName = winPlayer != null ? winPlayer.getName() : "N/A";
        int winScore = scores.getOrDefault(winner, 0);

        updateBossBar("buildbattle.winner-announce",
                Map.of("player", winName, "score", String.valueOf(winScore)));

        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            p.getInventory().remove(Material.PAPER);
            p.sendTitle("§aWinner!", winName, 10, 60, 10);
            p.sendMessage(plugin.getLanguageManager().get("buildbattle.winner-announce",
                    Map.of("player", winName, "score", String.valueOf(winScore))));
            p.teleport(baseArena.getLobby());
        }

        Bukkit.getScheduler().runTaskLater(plugin, this::resetArena, 100L);
    }

    public void resetArena() {
        state = BuildBattleState.WAITING;
        scores.clear();
        votes.clear();
        votingIndex = 0;

        for (PlotRegion region : plotRegions.values()) {
            deletePlot(region);
        }
        plotRegions.clear();

        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.teleport(baseArena.getLobby());
                bossBar.addPlayer(p);
            }
        }

        updateBossBar("buildbattle.waiting", Map.of("count", String.valueOf(players.size())));
        if (players.size() >= 2) startCountdown();
    }

    private void generatePlots() {
        int index = 0;
        for (UUID uuid : players) {
            createPlot(uuid, index++);
        }
    }

    private void createPlot(UUID playerUUID, int index) {
        int plotSize = plugin.getConfig().getInt("buildbattle.plot-size", 20);
        int gap = 5;

        int offsetX = (index % 3) * (plotSize + gap);
        int offsetZ = (index / 3) * (plotSize + gap);

        Location corner1 = baseArena.getPlayzone1().clone().add(offsetX, 0, offsetZ);
        Location corner2 = corner1.clone().add(plotSize, 10, plotSize);
        World world = Objects.requireNonNull(corner1.getWorld());

        for (int x = corner1.getBlockX(); x <= corner2.getBlockX(); x++) {
            for (int z = corner1.getBlockZ(); z <= corner2.getBlockZ(); z++) {
                for (int y = corner1.getBlockY(); y <= corner2.getBlockY() + 20; y++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
                world.getBlockAt(x, corner1.getBlockY(), z).setType(Material.GRASS_BLOCK);
            }
        }

        Material[] glasses = {
                Material.BLUE_STAINED_GLASS, Material.RED_STAINED_GLASS,
                Material.GREEN_STAINED_GLASS, Material.YELLOW_STAINED_GLASS,
                Material.PURPLE_STAINED_GLASS, Material.ORANGE_STAINED_GLASS
        };
        Material border = glasses[new Random().nextInt(glasses.length)];

        for (int x = corner1.getBlockX(); x <= corner2.getBlockX(); x++) {
            for (int z = corner1.getBlockZ(); z <= corner2.getBlockZ(); z++) {
                if (x == corner1.getBlockX() || x == corner2.getBlockX()
                        || z == corner1.getBlockZ() || z == corner2.getBlockZ()) {
                    for (int y = corner1.getBlockY(); y <= corner2.getBlockY() + 3; y++) {
                        world.getBlockAt(x, y, z).setType(border);
                    }
                }
            }
        }

        int roofY = corner2.getBlockY() + 4;
        List<Location> roofBlocks = new ArrayList<>();
        for (int x = corner1.getBlockX(); x <= corner2.getBlockX(); x++) {
            for (int z = corner1.getBlockZ(); z <= corner2.getBlockZ(); z++) {
                Location loc = new Location(world, x, roofY, z);
                loc.getBlock().setType(Material.BARRIER);
                roofBlocks.add(loc);
            }
        }

        PlotRegion region = new PlotRegion(corner1, corner2, roofBlocks);
        plotRegions.put(playerUUID, region);

        Location center = corner1.clone().add(plotSize / 2.0, 1, plotSize / 2.0);
        playerPlots.put(playerUUID, center);

        Player p = Bukkit.getPlayer(playerUUID);
        if (p != null) p.teleport(center);
    }

    private void deletePlot(PlotRegion region) {
        World world = Objects.requireNonNull(region.corner1.getWorld());
        int minX = Math.min(region.corner1.getBlockX(), region.corner2.getBlockX());
        int maxX = Math.max(region.corner1.getBlockX(), region.corner2.getBlockX());
        int minZ = Math.min(region.corner1.getBlockZ(), region.corner2.getBlockZ());
        int maxZ = Math.max(region.corner1.getBlockZ(), region.corner2.getBlockZ());
        int baseY = Math.min(region.corner1.getBlockY(), region.corner2.getBlockY());

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = baseY; y <= region.corner2.getBlockY() + 20; y++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }

    public void decreaseTimer() { timer--; }
    public void updateBossBar(String key, Map<String, String> placeholders) { bossBar.setTitle(plugin.getLanguageManager().get(key, placeholders)); }
    public void setVotingIndex(int votingIndex) { this.votingIndex = votingIndex; }
    public List<UUID> getPlayers() { return players; }
    public BuildBattleState getState() { return state; }
    public int getTimer() { return timer; }
    public MinigamesCore getPlugin() { return plugin; }
    public Arena getBaseArena() { return baseArena; }
    public int getVotingIndex() { return votingIndex; }
}
