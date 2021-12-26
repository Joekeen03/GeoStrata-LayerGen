package com.joekeen03.geostrata_layergen.world;

import Reika.DragonAPI.Instantiable.Math.Noise.NoiseGeneratorBase;
import Reika.DragonAPI.Instantiable.Math.Noise.SimplexNoiseGenerator;
import Reika.DragonAPI.Libraries.World.ReikaBlockHelper;
import Reika.GeoStrata.Blocks.BlockSmooth;
import Reika.GeoStrata.Registry.GeoBlocks;
import Reika.GeoStrata.TileEntityGeoOre;
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
    private static final double lowerBound = -1.0f;
    private static final double upperBound = 1.0f;
    private static final double boundRange = upperBound-lowerBound;

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
        float[] rawFloats = new float[nLayers];
        for (int i = 0; i < nLayers; i++)
        {
            rawFloats[i] = generator.nextFloat();
        }
        Arrays.sort(rawFloats);
        // Should I cull layer boundaries that are too close together? Or should they be bumped upwards?

        layers = new Layer[nLayers];
        layerPermutations = new NoiseGeneratorBase[nLayers];
        LayerGenerator ref = LayerGenerator.instance;
        RockTypeBlockPair[] validRockTypes = new RockTypeBlockPair[ref.nRockTypes];
        validRockTypes[0] = LayerGenerator.instance.getType(ref.nRockTypes-1);
        for (int i = 0; i < nLayers; i++)
        {
            int nValidRockTypes = 1;
            for (int j = 0; j < ref.nRockTypes-1; j++)
            {
                RockTypeBlockPair rock = ref.getType(j);
                if ((rock.type.minY <= rawFloats[i]*ref.yRange) && (rock.type.maxY >= rawFloats[i]*ref.yRange))
                {
                    validRockTypes[nValidRockTypes] = rock;
                    nValidRockTypes++;
                }
            }
            this.layers[i] = new Layer(rawFloats[i]*boundRange+lowerBound,
                    validRockTypes[(generator.nextInt(nValidRockTypes))]);
            NoiseGeneratorBase perm = new SimplexNoiseGenerator(worldSeed).setFrequency(1/128D);
            layerPermutations[i] = perm;
        }
        layerPermutations[0] = new SimplexNoiseGenerator(worldSeed).setFrequency(1/512D);
        GeoStrataLayerGen.info("Created new plate:\n"+this);
    }

    public void Generate(World world, int x, int z, boolean cobbleSides) {
        // TODO Maybe randomly cobble blocks that are next to air? Or maybe veins of "weak" rock?
        // TODO Handle ocean/mountain biomes
        int yMax = max(63, world.getHeightValue(x, z) + 1); // Cap at y=64, so ravines and rivers cut through the stone.
        double cumulativePermutation = (layerPermutations[0].getValue(x, z)+1)/2; // Range [-0.2/1.8, 1.0]
        int y = max(0, (int)(yMax*(layers[0].startFraction+cumulativePermutation)));
        for (int i = 0; i < nLayers; i++) {
            int stop = yMax;
            if (i < (nLayers - 1)) {
                // TODO Find a better way to ensure that a given layer can't "vanish" - every layer will generate at
                //  least one block. Or more?
                //
                cumulativePermutation += (layerPermutations[i+1].getValue(x, z)+1)/32;
                double stopFrac = layers[i + 1].startFraction + cumulativePermutation;
                stop = (int) ceil(yMax * stopFrac);
            }
            RockTypeBlockPair rock = layers[i].blockType;
            for (; y < stop; y++) {
                Block b = world.getBlock(x, y, z);
                int meta = world.getBlockMetadata(x, y, z);
                if (b == Blocks.stone || b instanceof BlockSmooth) {
                    world.setBlock(x, y, z, rock.smooth, 0, 2);
                }
                else if (ReikaBlockHelper.isOre(b, meta))
                {
                    if (rock.type != null) {
                        TileEntityGeoOre te = new TileEntityGeoOre();
                        te.initialize(rock.type, b, meta);
                        world.setBlock(x, y, z, GeoBlocks.ORETILE.getBlockInstance());
                        world.setTileEntity(x, y, z, te);
                    }
                }
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
