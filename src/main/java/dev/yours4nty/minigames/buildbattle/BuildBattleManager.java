package dev.yours4nty.minigames.buildbattle;

import dev.yours4nty.MinigamesCore;
import dev.yours4nty.managers.Arena;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

public class BuildBattleManager {
    private final MinigamesCore plugin;
    private final Map<String, BuildBattleArena> arenas = new HashMap<>();

    public BuildBattleManager(MinigamesCore plugin) {
        this.plugin = plugin;
        loadArenasFromDB();
    }

    public void registerArena(Arena arena) {
        if (!arena.getType().equalsIgnoreCase("BUILD_BATTLE")) return;
        BuildBattleArena buildBattleArena = new BuildBattleArena(arena, plugin);
        arenas.put(arena.getName(), buildBattleArena);
    }

    public Map<String, BuildBattleArena> getArenas() {
        return arenas;
    }

    private void loadArenasFromDB() {
        for (Arena arena : plugin.getArenaManager().getArenasByType("BUILD_BATTLE")) {
            registerArena(arena);
        }
    }

    public BuildBattleArena getArenaByPlayer(Player player) {
        for (BuildBattleArena arena : arenas.values()) {
            if (arena != null && arena.getPlayers().contains(player.getUniqueId())) {
                return arena;
            }
        }
        return null;
    }

    public BuildBattleArena getArena(String name) {
        return arenas.get(name);
    }

    public void joinArena(Player player, String name) {
        BuildBattleArena arena = arenas.get(name);
        if (arena != null) arena.addPlayer(player);
    }

    public void leaveArena(Player player) {
        BuildBattleArena arena = getArenaByPlayer(player);
        if (arena != null) arena.removePlayer(player);
    }
}
