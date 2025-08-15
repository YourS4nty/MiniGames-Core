package dev.yours4nty.minigames.oitc;

import dev.yours4nty.MinigamesCore;
import org.bukkit.Bukkit;

public class OITCGame {
    private final OITCManager manager;

    public OITCGame(MinigamesCore plugin) {
        this.manager = new OITCManager(plugin);
        plugin.getArenaManager().getAllArenas().forEach(manager::registerArena);
        Bukkit.getPluginManager().registerEvents(new OITCListener(manager), plugin);
    }

    public OITCManager getManager() {
        return manager;
    }
}
