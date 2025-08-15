package dev.yours4nty.managers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import dev.yours4nty.MinigamesCore;
import java.util.Objects;

public class InventoryManager {

    private final MinigamesCore plugin;

    public InventoryManager(MinigamesCore plugin) {
        this.plugin = plugin;
    }

    public void giveLeaveItem(Player player) {
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrier.getItemMeta();
        assert meta != null;
        meta.setDisplayName(plugin.getLanguageManager().get("leaveitemname"));
        barrier.setItemMeta(meta);
        player.getInventory().setItem(8, barrier);
    }

    public void removeLeaveItem(Player player) {
        if (player.getInventory().getItem(8) != null &&
                Objects.requireNonNull(player.getInventory().getItem(8)).getType() == Material.BARRIER) {
            player.getInventory().setItem(8, null);
        }
    }
}
