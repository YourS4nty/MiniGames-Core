package dev.yours4nty.minigames.tntrun;

import dev.yours4nty.MinigamesCore;
import org.bukkit.Bukkit;

public class TNTRunGame {

    private final TNTRunManager manager;

    public TNTRunGame(MinigamesCore plugin) {
        this.manager = new TNTRunManager(plugin);
        plugin.getArenaManager().getAllArenas().forEach(manager::registerArena);
        Bukkit.getPluginManager().registerEvents(new TNTRunListener(manager, plugin), plugin);
    }

    public TNTRunManager getManager() {
        return manager;
    }
}
