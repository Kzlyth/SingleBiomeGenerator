package me.kzlyth.tKIWorldGen.commands;

import me.kzlyth.tKIWorldGen.TKIWorldGen;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeleportCommand implements CommandExecutor, TabCompleter {

    private final TKIWorldGen plugin;

    public TeleportCommand(TKIWorldGen plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Befehl kann nur von einem Spieler ausgeführt werden!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("§cVerwendung: /tpworld <weltname>");
            return false;
        }

        String worldName = args[0];
        World targetWorld = Bukkit.getWorld(worldName);

        if (targetWorld == null) {
            player.sendMessage("§cDie Welt '" + worldName + "' existiert nicht!");
            return false;
        }

        if (plugin.getConfigManager().getConfig().getBoolean("Debug", false)) {
            plugin.getLogger().info("Teleportiere Spieler " + player.getName() + " zur Welt: " + worldName);
        }

        player.teleport(targetWorld.getSpawnLocation());
        player.sendMessage("§aDu wurdest zur Welt '" + worldName + "' teleportiert.");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions = Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .collect(Collectors.toList());
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}