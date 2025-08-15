package dev.yours4nty.commands;

import dev.yours4nty.MinigamesCore;
import dev.yours4nty.managers.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class MinigamesCommand implements CommandExecutor {

    private final MinigamesCore plugin;

    public MinigamesCommand(MinigamesCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLanguageManager().get("ingameonly"));
            return true;
        }

        if (args.length == 0) {
            plugin.getLanguageManager().getList("help").forEach(player::sendMessage);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create" -> {
                if (args.length < 5) {
                    player.sendMessage(plugin.getLanguageManager().get("mgcreate"));
                    return true;
                }

                String name = args[1];
                String type = args[2];
                String iconName = args[3].toUpperCase();
                String displayName = String.join(" ", Arrays.copyOfRange(args, 4, args.length));

                Material icon;
                try {
                    icon = Material.valueOf(iconName);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(plugin.getLanguageManager().get("mginvalidicon", Map.of("icon", iconName)));
                    return true;
                }

                boolean created = plugin.getArenaManager().createArena(name, type, icon, displayName);
                String message = created
                        ? plugin.getLanguageManager().get("mgcreated", Map.of("name", name))
                        : plugin.getLanguageManager().get("mgcreatexists");

                player.sendMessage(message);
            }

            case "setspawn" -> {
                if (args.length < 2) {
                    player.sendMessage(plugin.getLanguageManager().get("mgspawn"));
                    return true;
                }
                Location loc = player.getLocation();
                boolean ok = plugin.getArenaManager().updateSpawn(args[1], loc);
                String message = ok
                        ? plugin.getLanguageManager().get("mgspawnset")
                        : plugin.getLanguageManager().get("mgarenanotfound");
                player.sendMessage(message);
            }

            case "setlobby" -> {
                if (args.length < 2) {
                    player.sendMessage(plugin.getLanguageManager().get("mglobby"));
                    return true;
                }
                Location loc = player.getLocation();
                boolean ok = plugin.getArenaManager().updateLobby(args[1], loc);
                String message = ok
                        ? plugin.getLanguageManager().get("mglobbyset")
                        : plugin.getLanguageManager().get("mgarenanotfound");
                player.sendMessage(message);
            }

            case "setplayzone" -> {
                if (args.length < 2) {
                    player.sendMessage(plugin.getLanguageManager().get("mgplayzone"));
                    return true;
                }

                Location pos1 = plugin.getSelectionManager().getPos1(player);
                Location pos2 = plugin.getSelectionManager().getPos2(player);

                if (pos1 == null || pos2 == null) {
                    player.sendMessage(plugin.getLanguageManager().get("mgselectpositions"));
                    return true;
                }

                boolean ok = plugin.getArenaManager().updatePlayzone(args[1], pos1, pos2);
                String message = ok
                        ? plugin.getLanguageManager().get("mgplayzoneset")
                        : plugin.getLanguageManager().get("mgarenanotfound");
                player.sendMessage(message);
            }

            case "delete" -> {
                if (args.length < 2) {
                    player.sendMessage(plugin.getLanguageManager().get("mgdelete"));
                    return true;
                }
                String name = args[1];
                boolean deleted = plugin.getArenaManager().deleteArena(name);
                String message = deleted
                        ? plugin.getLanguageManager().get("mgdeleted", Map.of("name", name))
                        : plugin.getLanguageManager().get("mgarenanotfound");
                player.sendMessage(message);
            }

            case "list" -> {
                Collection<Arena> arenas = plugin.getArenaManager().getAllArenas();
                if (arenas.isEmpty()) {
                    player.sendMessage(plugin.getLanguageManager().get("mglistempty"));
                    return true;
                }
                player.sendMessage(plugin.getLanguageManager().get("mglistheader"));
                arenas.forEach(arena -> {
                    String line = plugin.getLanguageManager().get("mglistitem", Map.of(
                            "name", arena.getName(),
                            "type", arena.getType()
                    ));
                    player.sendMessage(line);
                });
            }

            case "join" -> {
                if (plugin.getPlayerArenaManager().isInArena(player)) {
                    player.sendMessage(plugin.getLanguageManager().get("alreadyin"));
                    return true;
                }

                plugin.getJoinGUI().open(player);
            }

            case "reload" -> {
                if (args.length > 1 && args[1].equalsIgnoreCase("players")) {

                    plugin.getTNTRunGame().getManager().getArenas().values().forEach(arena -> {
                        arena.getPlayers().values().forEach(p -> {
                            p.getPlayer().teleport(player.getWorld().getSpawnLocation());
                            p.getPlayer().sendMessage(plugin.getLanguageManager().get("force-reload"));
                        });
                        arena.eliminatePlayer(player);
                        arena.removePlayer(player);
                        arena.resetArena();
                    });

                    plugin.getTNTTagGame().getManager().getArenas().values().forEach(arena -> {
                        arena.getPlayers().values().forEach(p -> {
                            p.getPlayer().teleport(player.getWorld().getSpawnLocation());
                            p.getPlayer().sendMessage(plugin.getLanguageManager().get("force-reload"));
                        });
                        arena.eliminatePlayer(player);
                        arena.removePlayer(player);
                        arena.resetArena();
                    });

                    plugin.getSpleefGame().getManager().getArenas().values().forEach(arena -> {
                        arena.getPlayers().values().forEach(p -> {
                            p.getPlayer().teleport(player.getWorld().getSpawnLocation());
                            p.getPlayer().sendMessage(plugin.getLanguageManager().get("force-reload"));
                        });
                        arena.eliminatePlayer(player);
                        arena.removePlayer(player);
                        arena.resetArena();
                    });

                    plugin.getOITCGame().getManager().getArenas().values().forEach(arena -> {
                        arena.getPlayers().values().forEach(p -> {
                            p.getPlayer().teleport(player.getWorld().getSpawnLocation());
                            p.getPlayer().sendMessage(plugin.getLanguageManager().get("force-reload"));
                        });
                        arena.removePlayer(player);
                        arena.resetArena();
                    });

                    plugin.getSumoGame().getManager().getArenas().values().forEach(arena -> {
                        arena.getPlayers().values().forEach(p -> {
                            p.getPlayer().teleport(player.getWorld().getSpawnLocation());
                            p.getPlayer().sendMessage(plugin.getLanguageManager().get("force-reload"));
                        });
                        arena.eliminatePlayer(player);
                        arena.removePlayer(player);
                        arena.resetArena();
                    });

                    plugin.getPaintballGame().getManager().getArenas().values().forEach(arena -> {
                        arena.getPlayers().values().forEach(p -> {
                            p.getPlayer().teleport(player.getWorld().getSpawnLocation());
                            p.getPlayer().sendMessage(plugin.getLanguageManager().get("force-reload"));
                        });
                        arena.eliminatePlayer(player);
                        arena.removePlayer(player);
                        arena.resetArena();
                    });

                    plugin.getBuildBattleGame().getManager().getArenas().values().forEach(arena -> {
                        arena.getPlayers().forEach(uuid -> {
                            Player p = Bukkit.getPlayer(uuid);
                                    if (p != null) {
                                        Objects.requireNonNull(p.getPlayer()).teleport(player.getWorld().getSpawnLocation());
                                        p.getPlayer().sendMessage(plugin.getLanguageManager().get("force-reload"));
                                    }
                        });
                        arena.removePlayer(player);
                        arena.resetArena();
                    });



                    player.sendMessage(plugin.getLanguageManager().get("forcedr"));
                    return true;
                }

                plugin.reloadConfig();
                plugin.getLanguageManager().loadMessages();
                plugin.getArenaManager().loadArenas();
                player.sendMessage(plugin.getLanguageManager().get("mgreloaded"));
            }
            
            default -> player.sendMessage("§cSubcomando no válido. Usa /mg");
        }
        return true;
    }
}
