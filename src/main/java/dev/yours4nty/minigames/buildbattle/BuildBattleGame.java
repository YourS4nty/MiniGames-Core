package dev.yours4nty.minigames.buildbattle;

import dev.yours4nty.MinigamesCore;
import org.bukkit.Bukkit;

public class BuildBattleGame {
    private final BuildBattleManager manager;

    public BuildBattleGame(MinigamesCore plugin) {
        this.manager = new BuildBattleManager(plugin);
        plugin.getArenaManager().getAllArenas().forEach(manager::registerArena);
        Bukkit.getPluginManager().registerEvents(new BuildBattleListener(manager), plugin);
    }

    public BuildBattleManager getManager() {
        return manager;
    }
}
