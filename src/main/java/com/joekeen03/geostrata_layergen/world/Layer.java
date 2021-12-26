package com.joekeen03.geostrata_layergen.world;

import com.joekeen03.geostrata_layergen.GeoStrataLayerGen;

public class Layer {
    public final double startFraction;
    public final RockTypeBlockPair blockType;
    public Layer(double start, RockTypeBlockPair rock)
    {
        startFraction = start;
        blockType = rock;
    }

    public String toString()
    {
        String rockType = blockType.type == null ? "stone" : blockType.type.toString();
        return "Layer with startFraction "+startFraction+" and rockType "+rockType;
    }
}
