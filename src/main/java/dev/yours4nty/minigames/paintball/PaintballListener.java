package dev.yours4nty.minigames.paintball;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PaintballListener implements Listener {
    private final PaintballManager manager;
    private final Map<UUID, Long> gunCooldown = new HashMap<>();
    private final Map<UUID, Integer> jumpCount = new HashMap<>();
    private final Map<UUID, Long> jumpCooldown = new HashMap<>();
    private final Map<UUID, Long> bootsCooldown = new HashMap<>();

    public PaintballListener(PaintballManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        PaintballArena arena = manager.getArenaByPlayer(player);
        if (arena == null || arena.getState() != PaintballState.RUNNING) return;

        String name = Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta()).getDisplayName();
        long now = System.currentTimeMillis();

        if (name.equals("§cPaint-Gun")) {
            if (gunCooldown.getOrDefault(player.getUniqueId(), 0L) > now) return;
            player.launchProjectile(Snowball.class);
            gunCooldown.put(player.getUniqueId(), now + 2000); // 2s cooldown
            player.playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1f, 1f);
        }

        if (name.equals("§bJump")) {
            if (jumpCooldown.getOrDefault(player.getUniqueId(), 0L) > now) {
                player.sendMessage("§cCooldown activo para la Pluma de Salto");
                return;
            }
            int remaining = jumpCount.getOrDefault(player.getUniqueId(), 3);
            if (remaining > 0) {
                player.setVelocity(player.getVelocity().setY(0.6));
                jumpCount.put(player.getUniqueId(), remaining - 1);
                player.sendMessage("§bSalto extra! Restan §f" + (remaining - 1));
                if (remaining - 1 == 0) {
                    jumpCooldown.put(player.getUniqueId(), now + 5000);
                    Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () -> {
                        jumpCount.put(player.getUniqueId(), 3);
                        player.sendMessage("§a¡Saltos recargados!");
                    }, 100L);
                }
            }
        }

        if (name.equals("§bSpeed")) {
            event.setCancelled(true);
            if (bootsCooldown.getOrDefault(player.getUniqueId(), 0L) > now) {
                player.sendMessage("§cCooldown activo para las Botas Rápidas");
                return;
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1)); // 5s Speed II
            bootsCooldown.put(player.getUniqueId(), now + 15000);
            player.sendMessage("§a¡Velocidad 2 activada por 5 segundos!");
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) return;
        if (!(snowball.getShooter() instanceof Player shooter)) return;
        if (!(event.getHitEntity() instanceof Player victim)) return;

        PaintballArena arena = manager.getArenaByPlayer(shooter);
        if (arena == null || arena.getState() != PaintballState.RUNNING) return;

        if (!victim.equals(shooter)) {
            PaintballPlayer pbVictim = arena.getAlivePlayers().stream()
                    .filter(p -> p.getPlayer().equals(victim))
                    .findFirst().orElse(null);

            if (pbVictim != null && pbVictim.isAlive()) {
                pbVictim.addHit();
                shooter.sendMessage("§a¡Has golpeado a §f" + victim.getName());
                victim.sendMessage("§c¡Has sido golpeado por §f" + shooter.getName());

                if (pbVictim.getHits() >= 3) {
                    arena.eliminatePlayer(victim);
                    shooter.sendMessage("§a¡Eliminaste a §f" + victim.getName());
                    victim.sendMessage("§c¡Has sido eliminado!");
                }
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PaintballArena arena = manager.getArenaByPlayer(player);
        if (arena != null) arena.removePlayer(player);
    }
}
