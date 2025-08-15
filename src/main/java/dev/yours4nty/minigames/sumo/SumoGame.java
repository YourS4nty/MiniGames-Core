package dev.yours4nty.minigames.sumo;

import dev.yours4nty.MinigamesCore;
import org.bukkit.Bukkit;

public class SumoGame {
    private final SumoManager manager;

    public SumoGame(MinigamesCore plugin) {
        this.manager = new SumoManager(plugin);
        Bukkit.getPluginManager().registerEvents(new SumoListener(manager), plugin);
    }

    public SumoManager getManager() {
        return manager;
    }
}
