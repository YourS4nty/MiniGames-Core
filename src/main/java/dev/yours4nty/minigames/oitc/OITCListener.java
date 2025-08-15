package dev.yours4nty.minigames.oitc;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class OITCListener implements Listener {

    private final OITCManager manager;

    public OITCListener(OITCManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player killer = null;
        if (event.getDamager() instanceof Player p) {
            killer = p;
        } else if (event.getDamager() instanceof Arrow arrow && arrow.getShooter() instanceof Player shooter) {
            killer = shooter;
        }

        if (killer == null) return;

        OITCArena arena = manager.getArenaByPlayer(killer);
        if (arena == null || arena.getState() != OITCState.RUNNING) return;

        event.setCancelled(true);

        if (killer.equals(victim)) return;

        arena.onKill(killer, victim);
    }
}
