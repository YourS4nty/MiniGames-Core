package dev.yours4nty.minigames.tnttag;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class TNTTagListener implements Listener {

    private final TNTTagManager manager;

    public TNTTagListener(TNTTagManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player damager)) return;

        TNTTagArena arena = manager.getArenaByPlayer(damager);
        if (arena == null) return;

        if (arena.getState() == TNTTagState.RUNNING &&
                arena.getItPlayer() != null &&
                arena.getItPlayer().getPlayer().equals(damager)) {

            arena.tagPlayer(damager, victim);
        }

        event.setCancelled(true);
    }
    @EventHandler
    public void onSpectatorMove(org.bukkit.event.player.PlayerMoveEvent e) {
        Player player = e.getPlayer();
        TNTTagArena arena = manager.getArenaByPlayer(player);

        if (arena != null && arena.getState() == TNTTagState.RUNNING) {
            TNTTagPlayer tntTagPlayer = arena.getPlayers().get(player.getUniqueId());

            if (tntTagPlayer != null && !tntTagPlayer.isAlive()) {
                if (!arena.getBaseArena().isInPlayZone(e.getTo())) {
                    e.setTo(arena.getBaseArena().getSpawn());
                }
            }
        }
    }
}