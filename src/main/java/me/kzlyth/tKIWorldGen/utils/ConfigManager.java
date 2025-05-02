package me.kzlyth.tKIWorldGen.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final JavaPlugin plugin;
    private File configFile;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setupConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdir();
                plugin.getLogger().info("Plugin Folder created!");
            }

            plugin.saveDefaultConfig();
            plugin.getLogger().info("Basic config created1");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("Confg got loaded!");
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
            plugin.getLogger().info("Config got saved!");
        } catch (IOException e) {
            plugin.getLogger().severe("Error while saving the config: " + e.getMessage());
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("Reloaded config");
    }
}