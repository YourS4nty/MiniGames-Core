package dev.yours4nty.storage;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import dev.yours4nty.MinigamesCore;

public class SQLite {

    private final MinigamesCore plugin;
    private Connection connection;

    public SQLite(MinigamesCore plugin) {
        this.plugin = plugin;
        connect();
        createTables();
    }

    public void connect() {
        try {
            if (connection != null && !connection.isClosed()) return;
            File dbFile = new File(plugin.getDataFolder(), "ArenaStorage.db");
            String url = "jdbc:sqlite:" + dbFile;
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        String sql = """
        CREATE TABLE IF NOT EXISTS arenas (
            name TEXT PRIMARY KEY,
            type TEXT,
            spawn TEXT,
            lobby TEXT,
            playzone1 TEXT,
            playzone2 TEXT,
            icon TEXT NOT NULL DEFAULT 'GRASS_BLOCK',
            display_name TEXT NOT NULL DEFAULT '§aArena'
        );
    """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

}
