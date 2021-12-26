package com.joekeen03.geostrata_layergen.world;

import Reika.GeoStrata.Blocks.BlockSmooth;
import com.joekeen03.geostrata_layergen.GeoStrataLayerGen;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.WorldServer;

import java.util.HashMap;
import java.util.Random;

import static java.lang.Math.max;

public class CobbleDecorator {

    private static final double cobbleChanceBottom = 0.23D;
    private static final double cobbleChanceSide = 0.05D;
    private static final double cobbleFallChance = 0.3D;
    private static final double dirtFallChance = 0.6D;
    private static final double grassFallChance = 0.4D;

    private final HashMap<Block, Block> cobbleLookup;

    public CobbleDecorator(RockTypeBlockPair[] genTypes)
    {
        GeoStrataLayerGen.info("Creating CobbleDecorator.");
        cobbleLookup = new HashMap<>(genTypes.length);
        for (RockTypeBlockPair genType : genTypes) {
            cobbleLookup.put(genType.smooth, genType.cobble);
        }
        for (Block block : cobbleLookup.keySet()) {
            GeoStrataLayerGen.info("Added: "+block.getLocalizedName()+": "+cobbleLookup.get(block).getLocalizedName());
        }
    }

    public void Decorate(WorldServer world, int chunkX, int chunkZ, Random random)
    {
        // Decoration (which this is) only happens when a chunk's +X, +Z, and +XZ neighbors are generated.
        int startX = chunkX*16+8;
        int startZ = chunkZ*16+8;

        for (int dx = 0; dx < 16; dx++)
        {
            int x = startX+dx;
            for (int dz = 0; dz < 16; dz++)
            {
                int z = startZ+dz;
                int yMax = max(63, world.getHeightValue(startX+dx, startZ+dz) + 1);
                for (int y = 1; y < yMax; y++)
                {
                    Block b = world.getBlock(x, y, z);
                    if (b == Blocks.stone || b instanceof BlockSmooth)
                    {
                        if (world.getBlock(x, y-1, z).getMaterial().isReplaceable()
                                && random.nextDouble() <= cobbleChanceBottom)
                        {
                            if (random.nextDouble() <= cobbleFallChance)
                            {
                                FallAndPile(world, cobbleLookup.get(b), 0, x, y, z, random);
                            }
                            else
                            {
                                world.setBlock(x, y, z, cobbleLookup.get(b), 0, 2);
                            }
                        }
                        // Problem is, any logic which depends on the blocks next to it can introduce non-determinism
                        //  into the code (where it depends on the order the chunks are generated in).
                        //  This is because, if you mutate a block based on something about the blocks to its side,
                        //  and that mutation can affect the logic applied to the blocks at its side, and these blocks
                        //  are on either side of a chunk boundary, you can end up with a dependency on the order those
                        //  two chunks are generated.
                        // I think there is a way around it, though - if you have some way to store the original state
                        //  of the blocks (or at least, their effect on the other blocks' logic) that are mutated,
                        //  you could keep the logic of the surrounding blocks independent of that mutation.
//                        if ((world.isAirBlock(x-1, y, z) || world.isAirBlock(x, y, z-1)
//                                || world.isAirBlock(x+1, y, z) || world.isAirBlock(x, y, z+1))
//                                && random.nextDouble() <= cobbleChanceSide)
//                        {
//                            world.setBlock(x, y, z, cobbleLookup.get(b), 0, 2);
//                        }
                    }
                    else if (b == Blocks.dirt)
                    {
                        if (world.getBlock(x, y-1, z).getMaterial().isReplaceable()
                                && random.nextDouble() <= dirtFallChance)
                        {
                            FallAndPile(world, b, world.getBlockMetadata(x, y, z), x, y, z, random);
                        }
                    }
                    else if (b == Blocks.grass)
                    {
                        if (world.getBlock(x, y-1, z).getMaterial().isReplaceable()
                                && random.nextDouble() <= grassFallChance)
                        {
                            FallAndPile(world, Blocks.dirt, 0, x, y, z, random);
                        }
                    }
                }
            }
        }
    }
    public void FallAndPile(WorldServer world, Block fallingBlock, int meta, int x, int y, int z, Random random)
    {
        // Find lowest block
        int destY = y-1;
        while (destY > 0 && world.getBlock(x, destY-1, z).getMaterial().isReplaceable())
        {
            destY--;
        }
        // Maybe, if the block it would replace is lava, give it a chance of turning into
        //  lava? Also, if the block is dirt/sand, it should have a higher chance of falling.
        world.setBlockToAir(x, y, z);
        world.setBlock(x, destY, z, fallingBlock, meta, 2);
        // What about piling, where a column of fallen blocks will spread out in a rough pile, a la TFC physics?
    }
}
