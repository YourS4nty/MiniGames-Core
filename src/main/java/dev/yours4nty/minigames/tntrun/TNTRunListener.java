package dev.yours4nty.minigames.tntrun;

import dev.yours4nty.MinigamesCore;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TNTRunListener implements Listener {

    private final TNTRunManager manager;
    private final MinigamesCore plugin;
    private final Set<Block> scheduledToBreak = new HashSet<>();

    public TNTRunListener(TNTRunManager manager, MinigamesCore plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        TNTRunArena arena = manager.getArenaOfPlayer(player);
        if (arena == null || arena.getState() != TNTRunState.RUNNING) return;
        if (!arena.isPlayerInGame(player)) return;

        if (!arena.getBaseArena().isInPlayZone(player.getLocation())) {
            TNTRunPlayer runPlayer = arena.getTNTRunPlayer(player);
            if (runPlayer == null) return;

            if (runPlayer.hasSecondChancePowerUp() && !runPlayer.hasUsedSecondChance()) {
                runPlayer.useSecondChance();
                player.sendMessage(plugin.getLanguageManager().get("tntrun.pw.usedSC"));
                player.teleport(arena.getBaseArena().getSpawn());
                runPlayer.resetSecondChancePowerUp();
            } else {
                arena.eliminatePlayer(player);
            }
            return;
        }

        if (player.getGameMode() == GameMode.SPECTATOR || player.isFlying()) return;

        Block blockBelow = player.getLocation().getBlock().getRelative(0, -1, 0);
        Set<Material> breakables = manager.getBreakableBlocks();

        if (breakables.contains(blockBelow.getType()) && !scheduledToBreak.contains(blockBelow)) {
            scheduledToBreak.add(blockBelow);

            new BukkitRunnable() {
                @Override
                public void run() {
                    blockBelow.setType(Material.AIR);
                    scheduledToBreak.remove(blockBelow);
                }
            }.runTaskLater(manager.getPlugin(), 6L);
        }
    }

    @EventHandler
    public void onPowerUpPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        TNTRunArena arena = manager.getArenaOfPlayer(player);
        if (arena == null || arena.getState() != TNTRunState.RUNNING) return;

        TNTRunPlayer runPlayer = arena.getTNTRunPlayer(player);
        if (runPlayer == null || !runPlayer.isAlive()) return;

        Item item = event.getItem();

        Iterator<TNTPowerUp> iterator = arena.getActivePowerUps().iterator();
        while (iterator.hasNext()) {
            TNTPowerUp powerUp = iterator.next();
            if (powerUp.getEntity().getUniqueId().equals(item.getUniqueId())) {
                event.setCancelled(true);

                switch (powerUp.getType()) {
                    case SPEED -> player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
                    case JUMP -> player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 200, 2));
                    case SECOND_CHANCE -> {
                        if (!runPlayer.hasSecondChancePowerUp()) {
                            runPlayer.grantSecondChancePowerUp();
                            player.sendMessage(plugin.getLanguageManager().get("tntrun.pw.getSC"));
                        } else {
                            player.sendMessage(plugin.getLanguageManager().get("tntrun.pw.hasSC"));
                        }
                    }
                }
                powerUp.getEntity().remove();
                iterator.remove();
                break;
            }
        }
    }

    @EventHandler
    public void onSpectatorMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        TNTRunArena arena = manager.getArenaOfPlayer(player);

        if (arena != null) {
            TNTRunPlayer tntRunPlayer = arena.getTNTRunPlayer(player);
            if (tntRunPlayer != null && tntRunPlayer.isSpectator()) {
                if (!arena.getBaseArena().isInPlayZone(e.getTo())) {
                    e.setTo(arena.getBaseArena().getSpawn());
                }
            }
        }
    }
}
