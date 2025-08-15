package dev.yours4nty.gui;

import dev.yours4nty.MinigamesCore;
import dev.yours4nty.managers.Arena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JoinGUI {

    private final MinigamesCore plugin;

    public JoinGUI(MinigamesCore plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Collection<Arena> arenas = plugin.getArenaManager().getAllArenas();
        int count = arenas.size();

        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getLanguageManager().get("gui.title"));

        int size = 9 * 5;
        Inventory gui = Bukkit.createInventory(null, size, title);

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        assert fillerMeta != null;
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < size; i++) {
            gui.setItem(i, filler);
        }

        List<Integer> slots = new ArrayList<>();

        int centerSlot = 22;
        int offset = 0;

        if (count % 2 == 1) {
            slots.add(centerSlot);
            offset = 1;
        } else {
            offset = 1;
        }

        while (slots.size() < count) {
            int left = centerSlot - offset;
            int right = centerSlot + offset;

            if (left >= 0) slots.add(left);
            if (slots.size() >= count) break;
            if (right < size) slots.add(right);
            offset++;
        }

        Collections.sort(slots);

        int i = 0;
        for (Arena arena : arenas) {
            if (i >= slots.size()) break;

            ItemStack item = new ItemStack(arena.getIcon());
            ItemMeta meta = item.getItemMeta();
            assert meta != null;

            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', arena.getDisplayName()));
            NamespacedKey key = new NamespacedKey(plugin, "arena_name");
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(key, PersistentDataType.STRING, arena.getName());

            item.setItemMeta(meta);

            gui.setItem(slots.get(i), item);
            i++;
        }

        player.openInventory(gui);
    }
}
