package dev.yours4nty.minigames.sumo;

import dev.yours4nty.MinigamesCore;
import dev.yours4nty.managers.Arena;
import dev.yours4nty.minigames.tntrun.TNTRunArena;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SumoManager {
    private final Map<String, SumoArena> arenas = new HashMap<>();
    private final MinigamesCore plugin;

    public SumoManager(MinigamesCore plugin) {
        this.plugin = plugin;
        loadArenasFromDB();
    }

    public void registerArena(Arena arena) {
        if (!arena.getType().equalsIgnoreCase("SUMO")) return;
        arenas.put(arena.getName().toLowerCase(), new SumoArena(arena, plugin));
    }

    public Map<String, SumoArena> getArenas() {
        return arenas;
    }

    private void loadArenasFromDB() {
        for (Arena arena : plugin.getArenaManager().getArenasByType("SUMO")) {
            registerArena(arena);
        }
    }

    public SumoArena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    public SumoArena getArenaByPlayer(Player player) {
        return arenas.values().stream()
                .filter(a -> a.getPlayers().containsKey(player.getUniqueId()))
                .findFirst()
                .orElse(null);
    }

}
