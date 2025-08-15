package dev.yours4nty.minigames.sumo;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class SumoListener implements Listener {
    private final SumoManager manager;

    public SumoListener(SumoManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        SumoArena arena = manager.getArenaByPlayer(player);
        if (arena == null) return;

        SumoPlayer sp = arena.getPlayers().get(player.getUniqueId());
        if (sp == null) return;

        // Si es espectador: intocable
        if (sp.isSpectator()) {
            event.setCancelled(true);
            return;
        }

        // Si el daño fue causado por otra entidad (jugador, mob, etc.)
        if (event instanceof org.bukkit.event.entity.EntityDamageByEntityEvent hitEvent) {
            if (hitEvent.getDamager() instanceof Player damager) {
                event.setCancelled(true); // cancelamos daño
                applyKnockback(player, damager); // aplicamos knockback manual
            } else {
                event.setDamage(0); // mobs -> solo knockback normal
            }
        } else {
            // Caídas, fuego, etc. => sin daño
            event.setDamage(0);
        }
    }

    private void applyKnockback(Player victim, Player attacker) {
        var direction = victim.getLocation().toVector()
                .subtract(attacker.getLocation().toVector())
                .normalize()
                .multiply(0.8); // fuerza del empuje
        direction.setY(0.4); // levanta un poco
        victim.setVelocity(direction);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        // Optimización: ignorar si solo mueve la cámara
        assert event.getTo() != null;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        SumoArena arena = manager.getArenaByPlayer(player);
        if (arena == null || event.getTo() == null) return;

        SumoPlayer sp = arena.getPlayers().get(player.getUniqueId());
        if (sp == null) return;

        // Si es espectador, no puede salir de la zona de juego
        if (sp.isSpectator()) {
            if (!arena.getBaseArena().isInPlayZone(event.getTo())) {
                event.setTo(arena.getBaseArena().getSpawn());
            }
            return;
        }

        // Si el juego está corriendo y el jugador vivo sale de la zona, queda eliminado
        if (arena.getState() == SumoState.RUNNING && sp.isAlive()) {
            if (!arena.getBaseArena().isInPlayZone(event.getTo())) {
                arena.eliminatePlayer(player);
            }
        }
    }
}
