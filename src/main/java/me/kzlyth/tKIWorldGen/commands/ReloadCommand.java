package me.kzlyth.tKIWorldGen.commands;

import me.kzlyth.tKIWorldGen.TKIWorldGen;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand implements CommandExecutor, TabCompleter {

    private final TKIWorldGen plugin;

    public ReloadCommand(TKIWorldGen plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.getConfigManager().reloadConfig();

        if (plugin.getCreateWorldCommand() != null) {
            plugin.getCreateWorldCommand().loadConfigSettings();
        }

        sender.sendMessage("§aTKIWorldGen Konfiguration wurde neu geladen!");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}