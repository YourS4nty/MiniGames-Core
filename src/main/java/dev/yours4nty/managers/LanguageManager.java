package dev.yours4nty.managers;

import dev.yours4nty.MinigamesCore;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LanguageManager {

    private final MinigamesCore plugin;
    private YamlConfiguration messages;
    private String currentLanguage;

    public LanguageManager(MinigamesCore plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
        currentLanguage = plugin.getConfig().getString("language", "en");
    }

    public String get(String path) {
        String fullPath = currentLanguage + "." + path;
        String msg = messages.getString(fullPath);
        return msg != null ? msg : "§cMensaje no encontrado: " + fullPath;
    }

    public String get(String path, Map<String, String> placeholders) {
        String message = get(path);
        return applyPlaceholders(message, placeholders);
    }

    public List<String> getList(String path) {
        String fullPath = currentLanguage + "." + path;
        return messages.getStringList(fullPath);
    }

    public List<String> getList(String path, Map<String, String> placeholders) {
        List<String> raw = getList(path);
        List<String> result = new ArrayList<>();
        for (String line : raw) {
            result.add(applyPlaceholders(line, placeholders));
        }
        return result;
    }

    private String applyPlaceholders(String message, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }
}
