package dev.yours4nty.minigames.tntrun;

import dev.yours4nty.MinigamesCore;
import dev.yours4nty.managers.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class TNTRunManager {

    private final MinigamesCore plugin;
    private final Map<String, TNTRunArena> arenas = new HashMap<>();

    public TNTRunManager(MinigamesCore plugin) {
        this.plugin = plugin;
        loadArenasFromDB();
    }

    public void registerArena(Arena arena) {
        if (!arena.getType().equalsIgnoreCase("TNT_RUN")) return;

        TNTRunArena tntrunArena = new TNTRunArena(arena, plugin);
        arenas.put(arena.getName().toLowerCase(), tntrunArena);
    }

    public Map<String, TNTRunArena> getArenas() {
        return arenas;
    }

    private void loadArenasFromDB() {
        for (Arena arena : plugin.getArenaManager().getArenasByType("TNT_RUN")) {
            registerArena(arena);
        }
    }

    public TNTRunArena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    public TNTRunArena getArenaOfPlayer(Player player) {
        return arenas.values().stream()
                .filter(a -> a.getBaseArena().isAlready(player))
                .findFirst()
                .orElse(null);
    }

    public Set<Material> getBreakableBlocks() {
        List<String> names = plugin.getConfig().getStringList("tntrun.breakable-blocks");
        Set<Material> materials = new HashSet<>();
        for (String name : names) {
            try {
                materials.add(Material.valueOf(name.toUpperCase()));
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("[MinigamesCore] Invalid block in config: " + name);
            }
        }
        return materials;
    }

    public MinigamesCore getPlugin() {
        return plugin;
    }
}
