package dev.yours4nty.commands;

import dev.yours4nty.MinigamesCore;
import dev.yours4nty.managers.Arena;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class MinigamesTabCompleter implements TabCompleter {

    private final MinigamesCore plugin;

    private final List<String> subcommands = Arrays.asList(
            "create", "delete", "list",
            "setspawn", "setlobby", "setplayzone", "join", "reload"
    );

    private final List<String> arenaTypes = Arrays.asList("TNT_RUN", "TNT_TAG", "SPLEEF", "BUILD_BATTLE", "OITC", "PAINTBALL", "SUMO");

    public MinigamesTabCompleter(MinigamesCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return filter(subcommands, args[0]);
        }

        String sub = args[0].toLowerCase();

        if (args.length == 2 && Arrays.asList("delete", "setspawn", "setlobby", "setplayzone").contains(sub)) {
            return filter(plugin.getArenaManager().getAllArenas().stream()
                    .map(Arena::getName)
                    .toList(), args[1]);
        }

        // /mg create <name> <type> <icon> <display_name>
        if (sub.equals("create")) {
            if (args.length == 2) {
                return Collections.singletonList("arena_name");
            }

            if (args.length == 3) {
                return filter(arenaTypes, args[2]);
            }

            if (args.length == 4) {
                return filter(Arrays.stream(Material.values())
                        .map(Enum::name)
                        .toList(), args[3]);
            }

            if (args.length == 5) {
                return Collections.singletonList("&aDisplay_Name");
            }
        }

        return Collections.emptyList();
    }

    private List<String> filter(Collection<String> options, String prefix) {
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .toList();
    }
}
