package dev.yours4nty.minigames.spleef;

import dev.yours4nty.MinigamesCore;
import org.bukkit.Bukkit;

public class SpleefGame {
    private final SpleefManager manager;

    public SpleefGame(MinigamesCore plugin) {
        this.manager = new SpleefManager(plugin);
        plugin.getArenaManager().getAllArenas().forEach(manager::registerArena);
        Bukkit.getPluginManager().registerEvents(new SpleefListener(manager), plugin);
    }

    public SpleefManager getManager() {
        return manager;
    }
}
