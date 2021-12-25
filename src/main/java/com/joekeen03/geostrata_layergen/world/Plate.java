package com.joekeen03.geostrata_layergen.world;

import Reika.DragonAPI.Instantiable.Math.Noise.NoiseGeneratorBase;
import Reika.DragonAPI.Instantiable.Math.Noise.SimplexNoiseGenerator;
import com.joekeen03.geostrata_layergen.GeoStrataLayerGen;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.ceil;
import static java.lang.Math.max;

public class Plate {
    private static final int maxLayers = 25;
    private static final float lowerBound = -1.0f;
    private static final float upperBound = 1.0f;
    private static final float boundRange = upperBound-lowerBound;

    public final int id;
    public final int nLayers;
    private final Layer[] layers;
    private final NoiseGeneratorBase[] layerPermutations;

    public Plate(int id, long worldSeed)
    {
        this.id = id;
        Random generator = new Random(id);

        nLayers = generator.nextInt(maxLayers+1)+1;

        // Since the floats are randomly generated, we need to generate all of them, then sort them to ensure
        // they're in ascending numerical order.
        float[] layerBoundaries = new float[nLayers];
        for (int i = 0; i < nLayers; i++)
        {
            layerBoundaries[i] = generator.nextFloat()*boundRange+lowerBound;
        }
        Arrays.sort(layerBoundaries);

        layers = new Layer[nLayers];
        layerPermutations = new NoiseGeneratorBase[nLayers];
        for (int i = 0; i < nLayers; i++)
        {
            this.layers[i] = new Layer(layerBoundaries[i],
                    LayerGenerator.instance.getType(generator.nextInt(LayerGenerator.instance.nRockTypes)));
            // TODO Properly implement the permutation creator.
            NoiseGeneratorBase perm = new SimplexNoiseGenerator(worldSeed).setFrequency(1/64D);
            layerPermutations[i] = perm;
        }
        GeoStrataLayerGen.info("Created new plate:\n"+this.toString());
    }

    public void Generate(World world, int x, int z) {
        // TODO Maybe randomly cobble blocks that are next to air? Or maybe veins of "weak" rock?
        // TODO Handle ocean/mountain biomes
        int yMax = max(63, world.getHeightValue(x, z) + 1); // Cap at y=64, so ravines and rivers cut through the stone.
        double cumulativePermutation = (layerPermutations[0].getValue(x, z)+1.8)/1.8; // Range [-0.2/1.8, 1.0]
        int y = max(0, (int)(yMax*(layers[0].startFraction+cumulativePermutation)));
        for (int i = 0; i < nLayers; i++) {
            int stop = yMax;
            if (i < (nLayers - 1)) {
                cumulativePermutation += layerPermutations[i+1].getValue(x, z)/16;
                double stopFrac = layers[i + 1].startFraction + cumulativePermutation;
                stop = (int) ceil(yMax * stopFrac);
            }
            RockTypeBlockPair rock = layers[i].blockType;
            for (; y < stop; y++) {
                Block b = world.getBlock(x, y, z);
                int meta = world.getBlockMetadata(x, y, z);
                if (b == Blocks.stone) {
                    world.setBlock(x, y, z, rock.block, 0, 2);
                }
//                else if (rock.type != null && ReikaBlockHelper.isOre(b, meta)) {
//                    TileEntityGeoOre te = new TileEntityGeoOre();
//                    te.initialize(rock.type, b, meta);
//                    world.setBlock(x, y, z, GeoBlocks.ORETILE.getBlockInstance());
//                    world.setTileEntity(x, y, z, te);
//                }
            }

        }
    }

    public String toString()
    {
        String layers = "";
        for (Layer layer : this.layers) {
            layers += layer.toString()+"\n";
        }
        return "Plate object with id "+id+" and "+nLayers+" layers:\n"+layers;
    }
}
