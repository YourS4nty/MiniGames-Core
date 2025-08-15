package dev.yours4nty.minigames.paintball;

import dev.yours4nty.MinigamesCore;
import dev.yours4nty.managers.Arena;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class PaintballManager {
    private final Map<String, PaintballArena> arenas = new HashMap<>();
    private final MinigamesCore plugin;

    public PaintballManager(MinigamesCore plugin) {
        this.plugin = plugin;
        loadArenasFromDB();
    }

    public void registerArena(Arena arena) {
        if (!arena.getType().equalsIgnoreCase("PAINTBALL")) return;
        PaintballArena pbArena = new PaintballArena(arena, plugin);
        arenas.put(arena.getName().toLowerCase(), pbArena);
    }

    public Map<String, PaintballArena> getArenas() {
        return arenas;
    }

    private void loadArenasFromDB() {
        for (Arena arena : plugin.getArenaManager().getArenasByType("PAINTBALL")) {
            registerArena(arena);
        }
    }

    public PaintballArena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    public PaintballArena getArenaByPlayer(Player player) {
        return arenas.values().stream()
                .filter(a -> a.getAlivePlayers().stream()
                        .anyMatch(p -> p.getPlayer().equals(player)))
                .findFirst()
                .orElse(null);
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
