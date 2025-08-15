package dev.yours4nty;

import dev.yours4nty.commands.MinigamesCommand;
import dev.yours4nty.commands.MinigamesTabCompleter;
import dev.yours4nty.gui.JoinGUI;
import dev.yours4nty.listeners.JoinGUIListener;
import dev.yours4nty.listeners.PlayerConnectionListener;
import dev.yours4nty.listeners.WandListener;
import dev.yours4nty.managers.*;
import dev.yours4nty.minigames.buildbattle.BuildBattleGame;
import dev.yours4nty.minigames.oitc.OITCGame;
import dev.yours4nty.minigames.paintball.PaintballGame;
import dev.yours4nty.minigames.spleef.SpleefGame;
import dev.yours4nty.minigames.sumo.SumoGame;
import dev.yours4nty.minigames.tnttag.TNTTagGame;
import dev.yours4nty.minigames.tntrun.TNTRunGame;
import dev.yours4nty.storage.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class MinigamesCore extends JavaPlugin {

    private static MinigamesCore instance;

    // Managers
    private InventoryManager inventoryManager;
    private ArenaManager arenaManager;
    private PlayerArenaManager playerArenaManager;
    private SelectionManager selectionManager;
    private LanguageManager languageManager;
    private SQLite database;
    private JoinGUI joinGUI;

    // Games
    private TNTRunGame tntrunGame;
    private TNTTagGame tntTagGame;
    private SpleefGame spleefGame;
    private OITCGame oitcGame;
    private SumoGame sumoGame;
    private PaintballGame paintballGame;
    private BuildBattleGame buildBattleGame;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // 1. Config, Language & Database
        this.languageManager = new LanguageManager(this);
        this.database = new SQLite(this);
        this.database.connect();

        // 2. Managers primero (porque los juegos los necesitan)
        this.arenaManager = new ArenaManager(this);
        this.playerArenaManager = new PlayerArenaManager(this);
        this.selectionManager = new SelectionManager();
        this.joinGUI = new JoinGUI(this);
        this.inventoryManager = new InventoryManager(this);

        // 3. Juegos (ahora ArenaManager no es null)
        this.tntrunGame = new TNTRunGame(this);
        this.tntTagGame = new TNTTagGame(this);
        this.spleefGame = new SpleefGame(this);
        this.oitcGame = new OITCGame(this);
        this.sumoGame = new SumoGame(this);
        this.paintballGame = new PaintballGame(this);
        this.buildBattleGame = new BuildBattleGame(this);

        // 4. Events & Commands
        getServer().getPluginManager().registerEvents(new WandListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        Objects.requireNonNull(getCommand("mg")).setExecutor(new MinigamesCommand(this));
        Objects.requireNonNull(getCommand("mg")).setTabCompleter(new MinigamesTabCompleter(this));

        Bukkit.getConsoleSender().sendMessage("§9[MinigamesCore] §9╔══════════════════════════════════════════════╗");
        Bukkit.getConsoleSender().sendMessage("§9[MinigamesCore] §b  MinigamesCore Enabled!");
        Bukkit.getConsoleSender().sendMessage("§9[MinigamesCore] §7  Versión: §e1.0§7 | Autor: §bYourS4nty");
        Bukkit.getConsoleSender().sendMessage("§9[MinigamesCore] §7  DataBase: §eSQLite");
        Bukkit.getConsoleSender().sendMessage("§9[MinigamesCore] §9╚══════════════════════════════════════════════╝");
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("§9[MinigamesCore] §c  MinigamesCore disabled :(");
    }

    // --- Getters ---
    public static MinigamesCore getInstance() { return instance; }
    public SQLite getDatabase() { return database; }
    public LanguageManager getLanguageManager() { return languageManager; }
    public ArenaManager getArenaManager() { return arenaManager; }
    public PlayerArenaManager getPlayerArenaManager() { return playerArenaManager; }
    public SelectionManager getSelectionManager() { return selectionManager; }
    public JoinGUI getJoinGUI() { return joinGUI; }
    public InventoryManager getInventoryManager() { return inventoryManager; }
    public TNTRunGame getTNTRunGame() { return tntrunGame; }
    public TNTTagGame getTNTTagGame() { return tntTagGame; }
    public SpleefGame getSpleefGame() { return spleefGame; }
    public OITCGame getOITCGame() { return oitcGame; }
    public SumoGame getSumoGame() { return sumoGame; }
    public PaintballGame getPaintballGame() { return paintballGame; }
    public BuildBattleGame getBuildBattleGame() { return buildBattleGame; }
}
