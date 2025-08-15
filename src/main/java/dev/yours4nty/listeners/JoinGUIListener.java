package dev.yours4nty.listeners;

import dev.yours4nty.MinigamesCore;
import dev.yours4nty.managers.Arena;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Event.Result;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class JoinGUIListener implements Listener {

    private final MinigamesCore plugin;

    public JoinGUIListener(MinigamesCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getLanguageManager().get("gui.title"));

        if (!event.getView().getTitle().equals(title)) return;

        event.setCancelled(true);
        event.setResult(Result.DENY);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "arena_name");

        assert meta != null;
        String arenaName = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);

        if (arenaName == null) {
            player.sendMessage(plugin.getLanguageManager().get("mgarenanotfound"));
            return;
        }

        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage(plugin.getLanguageManager().get("mgarenanotfound"));
            return;
        }

        if (arena.isAlready(player)) {
            player.sendMessage(plugin.getLanguageManager().get("alreadyin"));
            return;
        }

        boolean success = plugin.getArenaManager().joinArena(player, arena);
        if (success) {
            player.sendMessage(plugin.getLanguageManager().get("mgjoined", Map.of("name", arena.getName())));
            plugin.getInventoryManager().giveLeaveItem(player);
        }

        player.closeInventory();
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getLanguageManager().get("gui.title"));

        if (!event.getView().getTitle().equals(title)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.BARRIER) return;
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        if (!meta.hasDisplayName()) return;

        String expectedName = plugin.getLanguageManager().get("leaveitemname");
        if (!meta.getDisplayName().equals(expectedName)) return;

        event.setCancelled(true);

        boolean success = plugin.getArenaManager().leaveArena(player);
        if (success) {
            player.sendMessage(plugin.getLanguageManager().get("leftarena"));
        } else {
            player.sendMessage(plugin.getLanguageManager().get("notinarena"));
        }
    }
}
