package me.kzlyth.tKIWorldGen;

import me.kzlyth.tKIWorldGen.commands.CreateWorldCommand;
import me.kzlyth.tKIWorldGen.commands.ReloadCommand;
import me.kzlyth.tKIWorldGen.commands.TeleportCommand;
import me.kzlyth.tKIWorldGen.utils.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class TKIWorldGen extends JavaPlugin {

    private ConfigManager configManager;
    private CreateWorldCommand createWorldCommand;

    @Override
    public void onEnable() {
        String pluginVersion = this.getPluginMeta().getVersion();

        getLogger().info("TKIWorldGen v" + pluginVersion + " is starting up!");
        getLogger().info("Minecraft Version: " + Bukkit.getVersion());

        configManager = new ConfigManager(this);
        configManager.setupConfig();

        try {
            createWorldCommand = new CreateWorldCommand(this);
            getCommand("createworld").setExecutor(createWorldCommand);
            getCommand("createworld").setTabCompleter(createWorldCommand);
            getLogger().info("CreateWorld-Command registered!");

            TeleportCommand teleportCommand = new TeleportCommand(this);
            getCommand("tpworld").setExecutor(teleportCommand);
            getCommand("tpworld").setTabCompleter(teleportCommand);
            getLogger().info("TeleportCommand registered!");

            ReloadCommand reloadCommand = new ReloadCommand(this);
            getCommand("tkiworldgenreload").setExecutor(reloadCommand);
            getCommand("tkiworldgenreload").setTabCompleter(reloadCommand);
            getLogger().info("Reload-Command registered!");
        } catch (Exception e) {
            getLogger().severe("Error while registering the Commands: " + e.getMessage());
        }

        getLogger().info("TKIWorldGen got activated!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Deactivating...");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CreateWorldCommand getCreateWorldCommand() {
        return createWorldCommand;
    }
}