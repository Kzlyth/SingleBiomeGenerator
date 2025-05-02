package me.kzlyth.tKIWorldGen.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Logger;

public class SingleBiomeWorldCreator {

    private final JavaPlugin plugin;
    private final Logger logger;

    public SingleBiomeWorldCreator(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public World createSingleBiomeWorld(String worldName, Biome biome, boolean generateStructures,
                                        boolean generateVillages) {
        if (worldName == null || worldName.trim().isEmpty()) {
            logger.severe("No world name provided you monkey!");
            return null;
        }

        if (Bukkit.getWorld(worldName) != null) {
            logger.warning("A World with the name'" + worldName + "' exists already!");
            return null;
        }

        logger.info("Creating new world '" + worldName + "' with Biome: " + biome.toString());
        logger.info("Settings=" + generateStructures +
                ", Villages=" + generateVillages);

        WorldCreator creator = WorldCreator.name(worldName);

        creator.type(WorldType.NORMAL);

        creator.generateStructures(generateStructures);

        creator.biomeProvider(new BiomeProvider() {
            @Override
            public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
                return biome;
            }

            @Override
            public List<Biome> getBiomes(WorldInfo worldInfo) {
                return List.of(biome);
            }
        });

        try {
            World world = creator.createWorld();
            if (world != null) {
                logger.info("World '" + worldName + "' created!");

                if (generateVillages && generateStructures) {
                    world.setGameRule(org.bukkit.GameRule.DO_PATROL_SPAWNING, true);
                    world.setGameRule(org.bukkit.GameRule.DO_TRADER_SPAWNING, true);
                } else {
                    world.setGameRule(org.bukkit.GameRule.DO_PATROL_SPAWNING, false);
                    world.setGameRule(org.bukkit.GameRule.DO_TRADER_SPAWNING, false);
                }

                return world;
            } else {
                logger.severe("Error while creating world '" + worldName + "'!");
                return null;
            }
        } catch (Exception e) {
            logger.severe("Error while creating world '" + worldName + "': " + e.getMessage());
            return null;
        }
    }
}