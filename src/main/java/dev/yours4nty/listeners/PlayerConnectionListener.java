package dev.yours4nty.listeners;

import dev.yours4nty.MinigamesCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.List;

public class PlayerConnectionListener implements Listener {

    private final MinigamesCore plugin;
    private final List<String> arenaDisconnectWorlds;

    public PlayerConnectionListener(MinigamesCore plugin) {
        this.plugin = plugin;
        this.arenaDisconnectWorlds = plugin.getConfig().getStringList("arena-disconnect-worlds");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (!arenaDisconnectWorlds.contains(player.getWorld().getName())) return;

        if (plugin.getArenaManager().isInArena(player)) {
            plugin.getArenaManager().leaveArena(player);
            player.sendMessage(plugin.getLanguageManager().get("arena.disconnect"));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (arenaDisconnectWorlds.contains(player.getWorld().getName())
                && plugin.getArenaManager().isInArena(player)) {
            plugin.getArenaManager().leaveArena(player);
            player.sendMessage(plugin.getLanguageManager().get("arena.disconnect"));
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (plugin.getArenaManager().isInArena(player)) {
            double finalHealth = player.getHealth() - event.getFinalDamage();
            if (finalHealth <= 0) {
                event.setCancelled(true);
                player.setHealth(player.getMaxHealth());
                player.setFireTicks(0);
            }
        }
    }
}