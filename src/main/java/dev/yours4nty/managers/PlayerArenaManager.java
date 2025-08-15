package dev.yours4nty.managers;

import dev.yours4nty.MinigamesCore;
import org.bukkit.entity.Player;
import java.util.*;

public class PlayerArenaManager {

    private final MinigamesCore plugin;
    private final Map<UUID, String> playerArenaMap = new HashMap<>();
    private final Set<UUID> cooldown = new HashSet<>();

    public PlayerArenaManager(MinigamesCore plugin) {
        this.plugin = plugin;
    }

    public boolean joinArena(Player player, String arenaName) {
        if (cooldown.contains(player.getUniqueId())) return false;
        cooldown(player);

        if (!plugin.getArenaManager().exists(arenaName)) return false;
        if (playerArenaMap.containsKey(player.getUniqueId())) return false;

        playerArenaMap.put(player.getUniqueId(), arenaName);
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena != null && arena.getLobby() != null) {
            player.teleport(arena.getLobby());
        }
        return true;
    }

    public boolean leaveArena(Player player) {
        if (cooldown.contains(player.getUniqueId())) return false;
        cooldown(player);

        if (!playerArenaMap.containsKey(player.getUniqueId())) return false;

        playerArenaMap.remove(player.getUniqueId());
        player.teleport(player.getWorld().getSpawnLocation());
        return true;
    }

    public boolean isInArena(Player player) {
        return playerArenaMap.containsKey(player.getUniqueId());
    }

    public String getArena(Player player) {
        return playerArenaMap.get(player.getUniqueId());
    }

    private void cooldown(Player player) {
        cooldown.add(player.getUniqueId());
        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                cooldown.remove(player.getUniqueId()), 20);
    }
}
