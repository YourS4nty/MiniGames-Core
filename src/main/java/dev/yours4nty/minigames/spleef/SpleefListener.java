package dev.yours4nty.minigames.spleef;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public class SpleefListener implements Listener {

    private final SpleefManager manager;

    public SpleefListener(SpleefManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        SpleefArena arena = manager.getArenaByPlayer(player);
        if (arena == null || arena.getState() != SpleefState.RUNNING) return;

        Block block = event.getBlock();
        if (!arena.getBaseArena().isInPlayZone(block.getLocation())) return;

        if (!breakSpleefBlock(block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSnowballHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player shooter)) return;

        SpleefArena arena = manager.getArenaByPlayer(shooter);
        if (arena == null || arena.getState() != SpleefState.RUNNING) return;

        Block block = event.getHitBlock();
        if (block == null) return;

        breakSpleefBlock(block);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        assert event.getTo() != null;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        SpleefArena arena = manager.getArenaByPlayer(player);
        if (arena == null || arena.getState() != SpleefState.RUNNING) return;

        if (!arena.getBaseArena().isInPlayZone(player.getLocation())) {
            arena.eliminatePlayer(player);
        }
    }

    private boolean breakSpleefBlock(Block block) {
        if (block.getType() == Material.SNOW_BLOCK) {
            block.setType(Material.AIR);
            return true;
        }
        return false;
    }
}
