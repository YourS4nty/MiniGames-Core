package dev.yours4nty.managers;

import dev.yours4nty.MinigamesCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.sql.*;
import java.util.*;

public class ArenaManager {

    private final MinigamesCore plugin;
    private final Map<String, Arena> arenas = new HashMap<>();
    private final Map<UUID, Arena> playerArenaMap = new HashMap<>();

    public ArenaManager(MinigamesCore plugin) {
        this.plugin = plugin;
        loadArenas();
    }

    public void loadArenas() {
        arenas.clear();
        try (Connection conn = plugin.getDatabase().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM arenas")) {
            while (rs.next()) {
                String name = rs.getString("name");
                String type = rs.getString("type");
                String iconStr = rs.getString("icon");
                Material icon;
                try {
                    icon = Material.valueOf(iconStr);
                } catch (IllegalArgumentException e) {
                    icon = Material.BARRIER;
                }
                String displayName = rs.getString("display_name");
                Arena arena = new Arena(name, type, icon, displayName);
                arena.setSpawn(deserializeLocation(rs.getString("spawn")));
                arena.setLobby(deserializeLocation(rs.getString("lobby")));
                arena.setPlayzone1(deserializeLocation(rs.getString("playzone1")));
                arena.setPlayzone2(deserializeLocation(rs.getString("playzone2")));
                arenas.put(name.toLowerCase(), arena);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void notifyGameManagers(Arena arena) {
        switch (arena.getType().toUpperCase()) {
            case "TNT_RUN" -> plugin.getTNTRunGame().getManager().registerArena(arena);
            case "TNT_TAG" -> plugin.getTNTTagGame().getManager().registerArena(arena);
            case "SPLEEF" -> plugin.getSpleefGame().getManager().registerArena(arena);
            case "OITC" -> plugin.getOITCGame().getManager().registerArena(arena);
            case "SUMO" -> plugin.getSumoGame().getManager().registerArena(arena);
            case "PAINTBALL" -> plugin.getPaintballGame().getManager().registerArena(arena);
            case "BUILD_BATTLE" -> plugin.getBuildBattleGame().getManager().registerArena(arena);
        }
    }

    public boolean createArena(String name, String type, Material icon, String displayName) {
        if (arenas.containsKey(name.toLowerCase())) return false;

        try (PreparedStatement ps = plugin.getDatabase().getConnection().prepareStatement(
                "INSERT INTO arenas (name, type, icon, display_name) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, name);
            ps.setString(2, type);
            ps.setString(3, icon.name());
            ps.setString(4, displayName);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        loadArenas();
        Arena nuevaArena = getArena(name);
        if (nuevaArena != null) {
            notifyGameManagers(nuevaArena);
        }
        return true;
    }


    public boolean deleteArena(String name) {
        if (!arenas.containsKey(name.toLowerCase())) return false;

        try (PreparedStatement ps = plugin.getDatabase().getConnection().prepareStatement(
                "DELETE FROM arenas WHERE name = ?")) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        arenas.remove(name.toLowerCase());
        return true;
    }

    public Collection<Arena> getAllArenas() {
        return arenas.values();
    }

    public Arena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    private String serializeLocation(Location loc) {
        if (loc == null) return null;
        return Objects.requireNonNull(loc.getWorld()).getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
    }

    private Location deserializeLocation(String str) {
        if (str == null || str.isEmpty()) return null;
        String[] parts = str.split(",");
        return new Location(Bukkit.getWorld(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]));
    }

    public boolean updateSpawn(String name, Location spawn) {
        Arena arena = getArena(name);
        if (arena == null) return false;

        arena.setSpawn(spawn);
        return updateLocationColumn("spawn", name, spawn);
    }

    public boolean updateLobby(String name, Location lobby) {
        Arena arena = getArena(name);
        if (arena == null) return false;

        arena.setLobby(lobby);
        return updateLocationColumn("lobby", name, lobby);
    }

    public boolean updatePlayzone(String name, Location pos1, Location pos2) {
        Arena arena = getArena(name);
        if (arena == null) return false;

        arena.setPlayzone1(pos1);
        arena.setPlayzone2(pos2);

        try (PreparedStatement ps = plugin.getDatabase().getConnection().prepareStatement(
                "UPDATE arenas SET playzone1 = ?, playzone2 = ? WHERE name = ?")) {
            ps.setString(1, serializeLocation(pos1));
            ps.setString(2, serializeLocation(pos2));
            ps.setString(3, name);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateLocationColumn(String column, String arenaName, Location loc) {
        try (PreparedStatement ps = plugin.getDatabase().getConnection().prepareStatement(
                "UPDATE arenas SET " + column + " = ? WHERE name = ?")) {
            ps.setString(1, serializeLocation(loc));
            ps.setString(2, arenaName);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exists(String name) {
        return getArena(name) != null;
    }

    public boolean joinArena(Player player, Arena arena) {
        if (arena == null) return false;

        if (playerArenaMap.containsKey(player.getUniqueId())) {
            player.sendMessage(plugin.getLanguageManager().get("alreadyin"));
            return false;
        }

        arena.addPlayer(player);
        playerArenaMap.put(player.getUniqueId(), arena);
        plugin.getInventoryManager().giveLeaveItem(player);

        switch (arena.getType().toUpperCase()) {
            case "TNT_RUN" -> plugin.getTNTRunGame().getManager().getArena(arena.getName()).addPlayer(player);
            case "TNT_TAG" -> plugin.getTNTTagGame().getManager().getArena(arena.getName()).addPlayer(player);
            case "SPLEEF" -> plugin.getSpleefGame().getManager().getArena(arena.getName()).addPlayer(player);
            case "OITC" -> plugin.getOITCGame().getManager().getArena(arena.getName()).addPlayer(player);
           case "SUMO" -> plugin.getSumoGame().getManager().getArena(arena.getName()).addPlayer(player);
            case "PAINTBALL" -> plugin.getPaintballGame().getManager().getArena(arena.getName()).addPlayer(player);
            case "BUILD_BATTLE" -> plugin.getBuildBattleGame().getManager().getArena(arena.getName()).addPlayer(player);
        }
        return true;
    }

    public boolean leaveArena(Player player) {
        UUID uuid = player.getUniqueId();

        if (!playerArenaMap.containsKey(uuid)) {
            return false;
        }

        Arena arena = playerArenaMap.remove(uuid);
        if (arena == null) return false;

        arena.getPlayers().remove(uuid);

        switch (arena.getType().toUpperCase()) {
            case "TNT_RUN" -> plugin.getTNTRunGame().getManager().getArena(arena.getName()).removePlayer(player);
            case "TNT_TAG" -> plugin.getTNTTagGame().getManager().getArena(arena.getName()).removePlayer(player);
            case "SPLEEF" -> plugin.getSpleefGame().getManager().getArena(arena.getName()).removePlayer(player);
            case "OITC" -> plugin.getOITCGame().getManager().getArena(arena.getName()).removePlayer(player);
            case "SUMO" -> plugin.getSumoGame().getManager().getArena(arena.getName()).removePlayer(player);
            case "PAINTBALL" -> plugin.getPaintballGame().getManager().getArena(arena.getName()).removePlayer(player);
            case "BUILD_BATTLE" -> plugin.getBuildBattleGame().getManager().getArena(arena.getName()).removePlayer(player);
        }

        plugin.getInventoryManager().removeLeaveItem(player);
        player.teleport(player.getWorld().getSpawnLocation());
        return true;
    }

    public boolean isInArena(Player player) {
        return playerArenaMap.containsKey(player.getUniqueId());
    }

    public List<Arena> getArenasByType(String type) {
        List<Arena> list = new ArrayList<>();
        for (Arena arena : arenas.values()) {
            if (arena.getType().equalsIgnoreCase(type)) {
                list.add(arena);
            }
        }
        return list;
    }
}
