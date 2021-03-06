package com.joekeen03.geostrata_layergen.world;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class GenLayerPlateID extends GenLayer {
    private static final String __OBFID = "CL_00006558";

    public GenLayerPlateID(long p_i2124_1_)
    {
        super(p_i2124_1_);
    }

    public int[] getInts(int p_75904_1_, int p_75904_2_, int p_75904_3_, int p_75904_4_)
    {
        int[] aint = IntCache.getIntCache(p_75904_3_ * p_75904_4_);

        for (int i1 = 0; i1 < p_75904_4_; ++i1)
        {
            for (int j1 = 0; j1 < p_75904_3_; ++j1)
            {
                this.initChunkSeed((long)(p_75904_1_ + j1), (long)(p_75904_2_ + i1));
                aint[j1 + i1 * p_75904_3_] = this.nextInt(Integer.MAX_VALUE);
            }
        }

        if (p_75904_1_ > -p_75904_3_ && p_75904_1_ <= 0 && p_75904_2_ > -p_75904_4_ && p_75904_2_ <= 0)
        {
            aint[-p_75904_1_ + -p_75904_2_ * p_75904_3_] = 1;
        }

        return aint;
    }
}
