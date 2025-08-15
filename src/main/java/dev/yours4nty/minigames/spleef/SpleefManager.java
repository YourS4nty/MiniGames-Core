package dev.yours4nty.minigames.spleef;

import dev.yours4nty.MinigamesCore;
import dev.yours4nty.managers.Arena;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

public class SpleefManager {
    private final Map<String, SpleefArena> arenas = new HashMap<>();
    private final MinigamesCore plugin;

    public SpleefManager(MinigamesCore plugin) {
        this.plugin = plugin;
        loadArenasFromDB();
    }

    public void registerArena(Arena arena) {
        if (!arena.getType().equalsIgnoreCase("SPLEEF")) return;

        SpleefArena spleefArena = new SpleefArena(arena, plugin);
        arenas.put(arena.getName().toLowerCase(), spleefArena);
    }

    public Map<String, SpleefArena> getArenas() {
        return arenas;
    }

    private void loadArenasFromDB() {
        for (Arena arena : plugin.getArenaManager().getArenasByType("SPLEEF")) {
            registerArena(arena);
        }
    }

    public SpleefArena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    public SpleefArena getArenaByPlayer(Player player) {
        return arenas.values().stream()
                .filter(a -> a.getAlivePlayers().stream().anyMatch(p -> p.getPlayer().equals(player)))
                .findFirst()
                .orElse(null);
    }
}
