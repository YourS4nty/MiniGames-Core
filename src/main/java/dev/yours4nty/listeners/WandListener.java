package dev.yours4nty.listeners;

import dev.yours4nty.MinigamesCore;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Player;

public class WandListener implements Listener {

    private final MinigamesCore plugin;

    public WandListener(MinigamesCore plugin) {
        this.plugin = plugin;
    }

    private String formatLocation(Location loc) {
        return String.format("(%s) x:%.1f y:%.1f z:%.1f",
                loc.getWorld() != null ? loc.getWorld().getName() : "null",
                loc.getX(), loc.getY(), loc.getZ());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWandClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.getInventory().getItemInMainHand().getType() != Material.BLAZE_ROD) return;
        if (event.getClickedBlock() == null) return;
        if (event.useInteractedBlock() == Event.Result.DENY) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        event.setCancelled(true);

        Location loc = event.getClickedBlock().getLocation();

        switch (event.getAction()) {
            case LEFT_CLICK_BLOCK -> {
                plugin.getSelectionManager().setPos1(player, loc);
                player.sendMessage(plugin.getLanguageManager().get("pos1")
                        .replace("{location}", formatLocation(loc)));
            }
            case RIGHT_CLICK_BLOCK -> {
                plugin.getSelectionManager().setPos2(player, loc);
                player.sendMessage(plugin.getLanguageManager().get("pos2")
                        .replace("{location}", formatLocation(loc)));
            }
        }

        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
    }
}
