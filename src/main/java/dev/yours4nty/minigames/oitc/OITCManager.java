package dev.yours4nty.minigames.oitc;

import dev.yours4nty.MinigamesCore;
import dev.yours4nty.managers.Arena;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

public class OITCManager {
    private final Map<String, OITCArena> arenas = new HashMap<>();
    private final MinigamesCore plugin;

    public OITCManager(MinigamesCore plugin) {
        this.plugin = plugin;
        loadArenasFromDB();
    }

    public void registerArena(Arena arena) {
        if (!arena.getType().equalsIgnoreCase("OITC")) return;

        OITCArena oitcArena = new OITCArena(arena, plugin);
        arenas.put(arena.getName().toLowerCase(), oitcArena);
    }

    public Map<String, OITCArena> getArenas() {
        return arenas;
    }

    private void loadArenasFromDB() {
        for (Arena arena : plugin.getArenaManager().getArenasByType("OITC")) {
            registerArena(arena);
        }
    }

    public OITCArena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    public OITCArena getArenaByPlayer(Player player) {
        return arenas.values().stream()
                .filter(a -> a.getAlivePlayers().stream().anyMatch(p -> p.getPlayer().equals(player)))
                .findFirst()
                .orElse(null);
    }
}
