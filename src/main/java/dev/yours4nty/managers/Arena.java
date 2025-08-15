package dev.yours4nty.managers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.*;

public class Arena {
    private final String name;
    private final String type;
    private final Material icon;
    private final String displayName;
    private Location spawn;
    private Location lobby;
    private Location playzone1;
    private Location playzone2;
    private final Set<UUID> players = new HashSet<>();

    public Arena(String name, String type, Material icon, String displayName) {
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Location getSpawn() {
        return spawn;
    }

    public Location getLobby() {
        return lobby;
    }

    public Location getPlayzone1() {
        return playzone1;
    }

    public Location getPlayzone2() {
        return playzone2;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public Set<UUID> getPlayers() {
        return players;
    }

    public void setPlayzone2(Location playzone2) {
        this.playzone2 = playzone2;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    public void setLobby(Location lobby) {
        this.lobby = lobby;
    }

    public void setPlayzone1(Location playzone1) {
        this.playzone1 = playzone1;
    }

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
        player.teleport(getLobby());
    }

    public boolean isAlready(Player player){
        return players.contains(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
    }

    public boolean isInLobbyZone(Location loc) {
        Location lobby = getLobby();
        if (lobby == null || loc.getWorld() == null || !Objects.equals(lobby.getWorld(), loc.getWorld())) {
            return false;
        }

        double radius = 10.0;
        return loc.distanceSquared(lobby) <= radius * radius;
    }

    public boolean isInPlayZone(Location loc) {
        if (playzone1 == null || playzone2 == null || loc == null) return false;

        if (!Objects.equals(playzone1.getWorld(), loc.getWorld())) return false;

        double x1 = Math.min(playzone1.getX(), playzone2.getX());
        double y1 = Math.min(playzone1.getY(), playzone2.getY());
        double z1 = Math.min(playzone1.getZ(), playzone2.getZ());
        double x2 = Math.max(playzone1.getX(), playzone2.getX());
        double y2 = Math.max(playzone1.getY(), playzone2.getY());
        double z2 = Math.max(playzone1.getZ(), playzone2.getZ());

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        return x >= x1 && x <= x2
                && y >= y1 && y <= y2
                && z >= z1 && z <= z2;
    }

    public List<Location> getPlayZoneBlocks() {
        List<Location> blocks = new ArrayList<>();
        if (getPlayzone1() == null || getPlayzone2() == null) return blocks;

        int minX = Math.min(playzone1.getBlockX(), playzone2.getBlockX());
        int maxX = Math.max(playzone1.getBlockX(), playzone2.getBlockX());
        int minY = Math.min(playzone1.getBlockY(), playzone2.getBlockY());
        int maxY = Math.max(playzone1.getBlockY(), playzone2.getBlockY());
        int minZ = Math.min(playzone1.getBlockZ(), playzone2.getBlockZ());
        int maxZ = Math.max(playzone1.getBlockZ(), playzone2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    blocks.add(new Location(playzone1.getWorld(), x, y, z));
                }
            }
        }
        return blocks;
    }
}
