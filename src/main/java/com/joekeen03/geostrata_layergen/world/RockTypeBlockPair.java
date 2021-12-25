package com.joekeen03.geostrata_layergen.world;

import Reika.GeoStrata.Registry.RockShapes;
import Reika.GeoStrata.Registry.RockTypes;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import javax.annotation.Nullable;

/**
 * Class that holds a rock type, and the corresponding smooth variant of that rock.
 * Holds Blocks.stone if it's passed null
 * Will probably be changed to allow it to handle other types of stone.
 */
public class RockTypeBlockPair {
    public final RockTypes type;
    public final Block block;
    @Nullable
    public RockTypeBlockPair(RockTypes rockType)
    {
        this.type = rockType;
        if (rockType != null)
        {
            block = rockType.getID(RockShapes.SMOOTH);
        }
        else
        {
            block = Blocks.stone;
        }
    }
}
