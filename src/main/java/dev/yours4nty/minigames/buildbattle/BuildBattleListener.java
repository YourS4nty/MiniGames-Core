package dev.yours4nty.minigames.buildbattle;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BuildBattleListener implements Listener {
    private final BuildBattleManager manager;

    public BuildBattleListener(BuildBattleManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onVote(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        BuildBattleArena arena = manager.getArenaByPlayer(player);
        if (arena == null) return;
        if (arena.getState() != BuildBattleState.VOTING) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName());
        if (name.startsWith("Votar ")) {
            try {
                int score = Integer.parseInt(name.replace("Votar ", "").trim());
                arena.castVote(player, score);
            } catch (NumberFormatException ignored) {
            }
        }
    }
}