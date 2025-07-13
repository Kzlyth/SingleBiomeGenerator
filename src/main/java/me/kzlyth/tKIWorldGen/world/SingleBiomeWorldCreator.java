package me.kzlyth.tKIWorldGen.world;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
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

        creator.generator(new NoWaterChunkGenerator(biome));

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

    private static class NoWaterChunkGenerator extends ChunkGenerator {
        private final Biome biome;

        public NoWaterChunkGenerator(Biome biome) {
            this.biome = biome;
        }

        @Override
        public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
            SimplexOctaveGenerator generator = new SimplexOctaveGenerator(new Random(worldInfo.getSeed()), 8);
            generator.setScale(0.005D);

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int worldX = chunkX * 16 + x;
                    int worldZ = chunkZ * 16 + z;

                    int height = (int) (generator.noise(worldX, worldZ, 0.5D, 0.5D) * 10 + 75);

                    for (int y = 0; y < 5; y++) {
                        chunkData.setBlock(x, y, z, Material.BEDROCK);
                    }

                    for (int y = 5; y < height - 5; y++) {
                        chunkData.setBlock(x, y, z, Material.STONE);
                    }

                    for (int y = height - 5; y < height - 1; y++) {
                        chunkData.setBlock(x, y, z, Material.DIRT);
                    }

                    Material surfaceBlock = getSurfaceBlockForBiome(biome);
                    chunkData.setBlock(x, height - 1, z, surfaceBlock);

                    int vegetationChance = getVegetationChanceForBiome(biome);
                    if (random.nextInt(vegetationChance) == 0) {
                        Material vegetation = getVegetationForBiome(biome);
                        if (vegetation != null && height < worldInfo.getMaxHeight() - 1) {
                            chunkData.setBlock(x, height, z, vegetation);
                        }
                    }
                }
            }
        }

        private int getVegetationChanceForBiome(Biome biome) {
            if (biome.equals(Biome.DESERT)) {
                return 100;
            } else if (biome.equals(Biome.BADLANDS) || biome.toString().contains("BADLANDS")) {
                return 80;
            } else if (biome.equals(Biome.JUNGLE)) {
                return 100;
            } else if (biome.equals(Biome.FOREST) || biome.equals(Biome.BIRCH_FOREST)) {
                return 100;
            } else if (biome.equals(Biome.TAIGA)) {
                return 100;
            } else if (biome.toString().contains("MUSHROOM")) {
                return 25;
            } else {
                return 30;
            }
        }

        private void generateCaveSystem(ChunkData chunkData, int x, int y, int z, Random random, int depth) {
            if (depth > 8 || x < 0 || x >= 16 || z < 0 || z >= 16 || y < 8 || y > 60) {
                return;
            }

            int radius = 2 + random.nextInt(2);
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        int newX = x + dx;
                        int newY = y + dy;
                        int newZ = z + dz;

                        if (newX >= 0 && newX < 16 && newZ >= 0 && newZ < 16 && newY > 5 && newY < chunkData.getMaxHeight()) {
                            double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);
                            if (distance <= radius - random.nextDouble() * 0.5) {
                                chunkData.setBlock(newX, newY, newZ, Material.AIR);
                            }
                        }
                    }
                }
            }

            int branches = 1 + random.nextInt(3);
            for (int i = 0; i < branches; i++) {
                int direction = random.nextInt(6);
                int tunnelLength = 3 + random.nextInt(8);

                int dirX = 0, dirY = 0, dirZ = 0;
                switch (direction) {
                    case 0: dirX = 1; break;
                    case 1: dirX = -1; break;
                    case 2: dirZ = 1; break;
                    case 3: dirZ = -1; break;
                    case 4: dirY = 1; break;
                    case 5: dirY = -1; break;
                }

                for (int step = 1; step <= tunnelLength; step++) {
                    int tunnelX = x + dirX * step + random.nextInt(3) - 1;
                    int tunnelY = y + dirY * step + random.nextInt(3) - 1;
                    int tunnelZ = z + dirZ * step + random.nextInt(3) - 1;

                    // Tunnel-Radius
                    int tunnelRadius = 1 + random.nextInt(2);
                    for (int tx = -tunnelRadius; tx <= tunnelRadius; tx++) {
                        for (int ty = -tunnelRadius; ty <= tunnelRadius; ty++) {
                            for (int tz = -tunnelRadius; tz <= tunnelRadius; tz++) {
                                int finalX = tunnelX + tx;
                                int finalY = tunnelY + ty;
                                int finalZ = tunnelZ + tz;

                                if (finalX >= 0 && finalX < 16 && finalZ >= 0 && finalZ < 16 && finalY > 5 && finalY < chunkData.getMaxHeight()) {
                                    double dist = Math.sqrt(tx*tx + ty*ty + tz*tz);
                                    if (dist <= tunnelRadius) {
                                        chunkData.setBlock(finalX, finalY, finalZ, Material.AIR);
                                    }
                                }
                            }
                        }
                    }

                    if (step == tunnelLength && random.nextInt(3) == 0) {
                        generateCaveSystem(chunkData, tunnelX, tunnelY, tunnelZ, random, depth + 1);
                    }
                }
            }
        }

        private void generateMineshaft(ChunkData chunkData, Random random) {
            int startX = random.nextInt(16);
            int startZ = random.nextInt(16);
            int startY = 20 + random.nextInt(25);

            int direction = random.nextInt(4);
            int length = 8 + random.nextInt(8);

            for (int i = 0; i < length; i++) {
                int x = startX;
                int z = startZ;

                switch (direction) {
                    case 0: x += i; break;
                    case 1: x -= i; break;
                    case 2: z += i; break;
                    case 3: z -= i; break;
                }

                if (x >= 0 && x < 16 && z >= 0 && z < 16) {
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = 0; dy <= 2; dy++) {
                            for (int dz = -1; dz <= 1; dz++) {
                                int finalX = x + dx;
                                int finalY = startY + dy;
                                int finalZ = z + dz;

                                if (finalX >= 0 && finalX < 16 && finalZ >= 0 && finalZ < 16 && finalY > 5 && finalY < chunkData.getMaxHeight()) {
                                    chunkData.setBlock(finalX, finalY, finalZ, Material.AIR);
                                }
                            }
                        }
                    }

                    if (i % 4 == 0) {
                        chunkData.setBlock(x, startY + 2, z, Material.OAK_FENCE);
                        if (direction == 0 || direction == 1) {
                            chunkData.setBlock(x, startY + 1, z - 1, Material.OAK_FENCE);
                            chunkData.setBlock(x, startY + 1, z + 1, Material.OAK_FENCE);
                        } else {
                            chunkData.setBlock(x - 1, startY + 1, z, Material.OAK_FENCE);
                            chunkData.setBlock(x + 1, startY + 1, z, Material.OAK_FENCE);
                        }
                    }
                }
            }
        }

        private Material getSurfaceBlockForBiome(Biome biome) {
            if (biome.equals(Biome.DESERT)) {
                return Material.SAND;
            } else if (biome.equals(Biome.SNOWY_PLAINS) || biome.equals(Biome.SNOWY_TAIGA)) {
                return Material.SNOW_BLOCK;
            } else if (biome.toString().contains("MUSHROOM")) {
                return Material.MYCELIUM;
            } else if (biome.equals(Biome.NETHER_WASTES)) {
                return Material.NETHERRACK;
            } else if (biome.equals(Biome.THE_END)) {
                return Material.END_STONE;
            } else {
                return Material.GRASS_BLOCK;
            }
        }

        private Material getVegetationForBiome(Biome biome) {
            if (biome.equals(Biome.PLAINS)) {
                return Material.GRASS_BLOCK;
            } else if (biome.equals(Biome.FOREST) || biome.equals(Biome.BIRCH_FOREST)) {
                return Material.OAK_SAPLING;
            } else if (biome.equals(Biome.JUNGLE)) {
                return Material.JUNGLE_SAPLING;
            } else if (biome.equals(Biome.TAIGA)) {
                return Material.SPRUCE_SAPLING;
            } else if (biome.equals(Biome.DESERT)) {
                return Material.DEAD_BUSH;
            } else if (biome.equals(Biome.BADLANDS) || biome.toString().contains("BADLANDS")) {
                return Material.DEAD_BUSH;
            } else if (biome.toString().contains("MUSHROOM")) {
                return Material.RED_MUSHROOM;
            } else {
                return Material.GRASS_BLOCK;
            }
        }

        @Override
        public @NotNull List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
            return List.of();
        }
    }
}
