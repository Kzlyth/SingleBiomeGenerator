package me.kzlyth.tKIWorldGen.commands;

import me.kzlyth.tKIWorldGen.TKIWorldGen;
import me.kzlyth.tKIWorldGen.world.SingleBiomeWorldCreator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CreateWorldCommand implements CommandExecutor, TabCompleter {

    private final TKIWorldGen plugin;
    private final SingleBiomeWorldCreator worldCreator;
    private boolean generateStructures;
    private boolean generateVillages;

    private final List<String> biomeNames;

    public CreateWorldCommand(TKIWorldGen plugin) {
        this.plugin = plugin;
        this.worldCreator = new SingleBiomeWorldCreator(plugin);

        loadConfigSettings();
        this.biomeNames = new ArrayList<>();
        addAllBiomes();
    }

    public void loadConfigSettings() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        this.generateStructures = config.getBoolean("Settings.Structures", true);
        this.generateVillages = config.getBoolean("Settings.Villages", true);
    }

    private void addAllBiomes() {
        biomeNames.add("plains");
        biomeNames.add("desert");
        biomeNames.add("forest");
        biomeNames.add("taiga");
        biomeNames.add("swamp");
        biomeNames.add("snowy_plains");
        biomeNames.add("jungle");
        biomeNames.add("savanna");
        biomeNames.add("badlands");
        biomeNames.add("mushroom_fields");
        biomeNames.add("dripstone_caves");
        biomeNames.add("lush_caves");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cVerwendung: /createworld <weltname> <biom>");
            return false;
        }

        String worldName = args[0];
        String biomeName = args[1].toLowerCase();

        if (!biomeNames.contains(biomeName)) {
            sender.sendMessage("§cBiome '" + biomeName + "' wasnt found! Available Bioms: " +
                    String.join(", ", biomeNames));
            return false;
        }

        Biome biome = getBiomeFromString(biomeName);
        if (biome == null) {
            sender.sendMessage("§cError while working with the provided biome!");
            return false;
        }

        if (plugin.getConfigManager().getConfig().getBoolean("Debug", false)) {
            plugin.getLogger().info("Creating World: " + worldName);
            plugin.getLogger().info("Biome: " + biome.toString());
            plugin.getLogger().info("Structures:: " + generateStructures);
            plugin.getLogger().info("Dörfer: " + generateVillages);
        }

        sender.sendMessage("§aErstelle Welt '" + worldName + "' mit Biom " + biome.toString() + "...");

        final String finalWorldName = worldName;
        final Biome finalBiome = biome;

        Bukkit.getScheduler().runTask(plugin, () -> {
            World world = worldCreator.createSingleBiomeWorld(
                    finalWorldName,
                    finalBiome,
                    generateStructures,
                    generateVillages
            );

            if (world != null) {
                sender.sendMessage("§aWorld '" + finalWorldName + "' created!");

                if (sender instanceof Player player) {
                    player.teleport(world.getSpawnLocation());
                    sender.sendMessage("§aYou got teleported to the new World!");
                }
            } else {
                sender.sendMessage("§cError while creating'" + finalWorldName + "'!");
            }
        });

        return true;
    }

    private Biome getBiomeFromString(String biomeName) {
        switch (biomeName) {
            case "plains": return Biome.PLAINS;
            case "desert": return Biome.DESERT;
            case "forest": return Biome.FOREST;
            case "taiga": return Biome.TAIGA;
            case "swamp": return Biome.SWAMP;
            case "snowy_plains": return Biome.SNOWY_PLAINS;
            case "jungle": return Biome.JUNGLE;
            case "savanna": return Biome.SAVANNA;
            case "badlands": return Biome.BADLANDS;
            case "mushroom_fields": return Biome.MUSHROOM_FIELDS;
            case "dripstone_caves": return Biome.DRIPSTONE_CAVES;
            case "lush_caves": return Biome.LUSH_CAVES;
            default: return null;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            return completions;
        } else if (args.length == 2) {
            completions.addAll(biomeNames);
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}