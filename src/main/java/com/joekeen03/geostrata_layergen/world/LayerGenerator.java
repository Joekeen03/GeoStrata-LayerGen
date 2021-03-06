package com.joekeen03.geostrata_layergen.world;

import Reika.DragonAPI.Interfaces.RetroactiveGenerator;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.GeoStrata.Registry.RockTypes;
import com.google.common.primitives.Ints;
import com.joekeen03.geostrata_layergen.GeoStrataLayerGen;
import com.joekeen03.geostrata_layergen.Tags;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.layer.*;

import javax.annotation.Nullable;
import java.util.*;


public class LayerGenerator implements RetroactiveGenerator {

    public static final LayerGenerator instance = new LayerGenerator();

    private static final int layerIDSeed = 13;
    private static final int layerPermutationSeed = 20;

    public final int nRockTypes;
    public final int maxY;
    public final int minY;
    public final int yRange;

    private final RockTypeBlockPair[] worldGenRocks;
    private GenLayer[] genLayers;
    private Int2ObjectOpenHashMap<Plate> plateCache;
    private long seed = 0;
    private boolean logNonServer = true;
    private final CobbleDecorator cobbler;

    protected LayerGenerator()
    {
        int i = 0;
        nRockTypes = RockTypes.values().length+1;
        worldGenRocks = new RockTypeBlockPair[nRockTypes];
        GeoStrataLayerGen.info("Number of layers: "+nRockTypes);
        for (; i < RockTypes.values().length; i++)
        {
            GeoStrataLayerGen.info("Adding stone for index "+i);
            worldGenRocks[i] = new RockTypeBlockPair(RockTypes.values()[i]);
        }
        worldGenRocks[i] = new RockTypeBlockPair(null);
        cobbler = new CobbleDecorator(worldGenRocks);
        maxY = Collections.max(Arrays.asList(RockTypes.values()), (val1, val2) -> Ints.compare(val1.maxY, val2.maxY)).maxY;
        minY = Collections.min(Arrays.asList(RockTypes.values()), (val1, val2) -> Ints.compare(val1.minY, val2.minY)).minY;
        yRange = maxY-minY;
        GeoStrataLayerGen.info("Created Layer Generator.");
    }

    /**
     *
     * @param random
     * @param chunkX
     * @param chunkZ
     * @param world
     * @param chunkGenerator
     * @param chunkProvider
     */
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
    {
        /**
         *  Generation logic:
         *  First two genLayers generate an int for a given (x, z) position; this int is then fed into the third GenLayer
         *  to generate the layerID for the position. This layerID is used to look up the layerSet for the given,
         *  or create it from scratch if it isn't cached.
             *  To generate the layerSet, you use the layerID as a seed for a new RNG, and create the layerSet based on
             *  those.
         *  Then, to generate the rock layers, you generate each layer, as according to its relative thickness, but
         *  randomly permuted up and down.
         *      Not sure if the permutations should be cumulative - meaning, if the bottom layer's top is shifted up, the
         *      layer above it has its top point shifted by that much, plus its own permutation.
         *      And the successive layers are likewise shifted up by the sum of the previous layers' permutations.
         *      Thinking the permutations could actually be generated by another GenLayer? It generates a series of
         *      numbers from the current position and the layerID, which are the permuations of the layers.
         *  Q: How to handle sloping the boundary between plates? I could, for each position, search out to see if
         *      there's a boundary between two plates, and determine if that boundary is supposed to slope into the
         *      current position...
         *      A more effective method might be to use another GenLayer, which generates a boundary "permutation"
         *      The main GenLayer used for generating represents where the plates are at, say, the world bottom;
         *      You then have a permutation GenLayer(s?), which yields the x and z offset of the GenLayer at the top of
         *      the world.
         *  Note: maybe have some bias, where if the max y-level is above sea level, the top layers are "eroded"?
         *  Meaning, the layers below are stretched up extra, so they sort of "jut" up to the top of the mountain, as if
         *  the weaker top layers were eroded away
         *  Could do something similar for rivers, too.
         *  Maybe it'd be easier to detect the biome?
         */

        if (genLayers == null || seed != world.getSeed())
        {
            // Create generator layers
            seed = world.getSeed();
            GenLayer layerIDGen = new GenLayerPlateID(layerIDSeed);
            layerIDGen = GenLayerZoom.magnify(6, layerIDGen, 6);
            layerIDGen = new GenLayerFuzzyZoom(13, layerIDGen);
            layerIDGen = new GenLayerFuzzyZoom(23, layerIDGen);
            layerIDGen = GenLayerZoom.magnify(6, layerIDGen, 2);
            layerIDGen = new GenLayerFuzzyZoom(42, layerIDGen);
            layerIDGen = new GenLayerFuzzyZoom(66, layerIDGen);
            layerIDGen = new GenLayerVoronoiZoom(5, layerIDGen);
            GenLayer permutationLayer = new GenLayerIsland(layerPermutationSeed);
            layerIDGen.initWorldGenSeed(seed);
            permutationLayer.initWorldGenSeed(seed);
            genLayers = new GenLayer[] {layerIDGen, permutationLayer};
            plateCache = new Int2ObjectOpenHashMap<>(20);
        }

        if (world instanceof WorldServer)
        {
            int xStart = chunkX*16;
            int zStart = chunkZ*16;
            int[] layerIDs = genLayers[0].getInts(xStart, zStart, 16, 16);
            WorldServer worldServer = (WorldServer)world;
//            boolean neighborsExist = (ReikaWorldHelper.isChunkGeneratedChunkCoords(worldServer, chunkX+1, chunkZ)
//                    && ReikaWorldHelper.isChunkGeneratedChunkCoords(worldServer, chunkX, chunkZ+1));

            for (int dx = 0; dx < 16; dx++)
            {
                // Only cobble blocks (which requires looking up neighboring blocks) if the chunks the neighbors are
                //  in exist; otherwise, you can get a stack overflow due to infinite recursion.
                boolean neighborsExist = ((dx != 0) || ReikaWorldHelper.isChunkGeneratedChunkCoords(worldServer, chunkX-1, chunkZ));
                neighborsExist = neighborsExist && ((dx != 15) || ReikaWorldHelper.isChunkGeneratedChunkCoords(worldServer,chunkX+1, chunkZ));
                for (int dz = 0; dz < 16; dz++)
                {
                    // TODO Lookup plate or create it anew.
                    neighborsExist = neighborsExist && ((dz != 0) || ReikaWorldHelper.isChunkGeneratedChunkCoords(worldServer, chunkX, chunkZ-1));
                    neighborsExist = neighborsExist && ((dz != 15) || ReikaWorldHelper.isChunkGeneratedChunkCoords(worldServer, chunkX, chunkZ+1));
                    int plateID = layerIDs[dx*16+dz];
                    Plate plate = plateCache.computeIfAbsent(plateID, id -> new Plate(id, seed));
                    plate.Generate(world, xStart+dx, zStart+dz, neighborsExist);
                }
            }
            cobbler.Decorate(worldServer, chunkX, chunkZ, random);
        }
        else if (logNonServer)
        {
            GeoStrataLayerGen.info("Non-server world received!");
            logNonServer = false;
        }
        // TODO Implement as a map of layers? For a given x & z, generate
    }

    @Nullable
    public RockTypeBlockPair getType(int i)
    {
        if (i >= 0 && i < nRockTypes)
        {
            return worldGenRocks[i];
        }
        return null;
    }

    public boolean canGenerateAt(World world, int chunkX, int chunkZ)
    {
        return true;
    }

    public String getIDString()
    {
        return Tags.MODID+"_LayerGen";
    }
}
