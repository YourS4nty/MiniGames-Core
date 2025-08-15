package dev.yours4nty.minigames.tnttag;

import dev.yours4nty.MinigamesCore;
import org.bukkit.Bukkit;

public class TNTTagGame {

    private final TNTTagManager manager;

    public TNTTagGame(MinigamesCore plugin) {
        this.manager = new TNTTagManager(plugin);
        plugin.getArenaManager().getAllArenas().forEach(manager::registerArena);
        Bukkit.getPluginManager().registerEvents(new TNTTagListener(manager), plugin);
    }

    public TNTTagManager getManager() {
        return manager;
    }
}
