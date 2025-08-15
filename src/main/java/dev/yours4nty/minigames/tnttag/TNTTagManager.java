package dev.yours4nty.minigames.tnttag;

import dev.yours4nty.MinigamesCore;
import dev.yours4nty.managers.Arena;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TNTTagManager {

    private final Map<String, TNTTagArena> arenas = new HashMap<>();
    private final MinigamesCore plugin;

    public TNTTagManager(MinigamesCore plugin) {
        this.plugin = plugin;
        loadArenasFromDB();
    }

    public void registerArena(Arena arena) {
        if (!arena.getType().equalsIgnoreCase("TNT_TAG")) return;

        TNTTagArena tntTagArena = new TNTTagArena(arena, plugin);
        arenas.put(arena.getName().toLowerCase(), tntTagArena);
    }

    public Map<String, TNTTagArena> getArenas() {
        return arenas;
    }

    private void loadArenasFromDB() {
        for (Arena arena : plugin.getArenaManager().getArenasByType("TNT_RUN")) {
            registerArena(arena);
        }
    }

    public TNTTagArena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    public TNTTagArena getArenaByPlayer(Player player) {
        return arenas.values().stream()
                .filter(a -> a.getAlivePlayers().stream().anyMatch(p -> p.getPlayer().equals(player)))
                .findFirst()
                .orElse(null);
    }
}