package dev.yours4nty.minigames.paintball;

import dev.yours4nty.MinigamesCore;
import org.bukkit.Bukkit;

public class PaintballGame {
    private final PaintballManager manager;

    public PaintballGame(MinigamesCore plugin) {
        this.manager = new PaintballManager(plugin);
        plugin.getArenaManager().getAllArenas().forEach(manager::registerArena);
        Bukkit.getPluginManager().registerEvents(new PaintballListener(manager), plugin);
    }

    public PaintballManager getManager() {
        return manager;
    }
}
