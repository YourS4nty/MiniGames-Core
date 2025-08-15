package dev.yours4nty.minigames.tntrun;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TNTRunTask extends BukkitRunnable {

    private final TNTRunArena arena;
    private final Plugin plugin;
    private final TNTRunManager manager;
    private int seconds = 10;

    public TNTRunTask(TNTRunArena arena, Plugin plugin, TNTRunManager manager) {
        this.arena = arena;
        this.plugin = plugin;
        this.manager = manager;
    }

    private PowerUpType choosePowerUpType() {
        int speedChance = plugin.getConfig().getInt("tntrun.powerup-chances.SPEED", 55);
        int jumpChance = plugin.getConfig().getInt("tntrun.powerup-chances.JUMP", 43);
        int secondChance = plugin.getConfig().getInt("tntrun.powerup-chances.SECOND_CHANCE", 2);

        int total = speedChance + jumpChance + secondChance;
        int random = (int) (Math.random() * total);

        if (random < speedChance) return PowerUpType.SPEED;
        if (random < speedChance + jumpChance) return PowerUpType.JUMP;
        return PowerUpType.SECOND_CHANCE;
    }

    void spawnRandomPowerUp() {
        List<Location> validBlocks = new ArrayList<>();
        for (Location loc : arena.getBaseArena().getPlayZoneBlocks()) {
            if (manager.getBreakableBlocks().contains(loc.getBlock().getType())) {
                validBlocks.add(loc);
            }
        }
        if (validBlocks.isEmpty()) return;

        Location loc = validBlocks.get((int) (Math.random() * validBlocks.size())).clone().add(0.5, 1.2, 0.5);
        PowerUpType type = choosePowerUpType();

        ItemStack itemStack = switch (type) {
            case SPEED -> new ItemStack(Material.SUGAR);
            case JUMP -> new ItemStack(Material.RABBIT_FOOT);
            case SECOND_CHANCE -> new ItemStack(Material.TOTEM_OF_UNDYING);
        };

        Item dropped = Objects.requireNonNull(loc.getWorld()).dropItem(loc, itemStack);
        dropped.setCustomName("§bPower-Up: " + type.name());
        dropped.setCustomNameVisible(true);
        dropped.setPickupDelay(0);
        dropped.setGlowing(true);

        arena.getActivePowerUps().add(new TNTPowerUp(type, dropped));
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
        arena.updateBossBar("tntrun.countdown", seconds);
        seconds--;
    }
}
