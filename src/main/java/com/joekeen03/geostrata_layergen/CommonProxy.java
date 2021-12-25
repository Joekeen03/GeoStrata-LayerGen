package com.joekeen03.geostrata_layergen;

import Reika.DragonAPI.Auxiliary.Trackers.RetroGenController;
import com.joekeen03.geostrata_layergen.world.LayerGenerator;
import cpw.mods.fml.common.event.*;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items,
    // etc, and register them with the GameRegistry."
    public void preInit(FMLPreInitializationEvent event) 	{
        Config.syncronizeConfiguration(event.getSuggestedConfigurationFile());
        Long2IntOpenHashMap test = new Long2IntOpenHashMap();

        GeoStrataLayerGen.info(Config.greeting);
        GeoStrataLayerGen.info("I am " + Tags.MODNAME + " at version " + Tags.VERSION + " and group name " + Tags.GROUPNAME);
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes."
    public void init(FMLInitializationEvent event) {
        RetroGenController.instance.addHybridGenerator(LayerGenerator.instance, 0, false);
//        GeoStrataLayerGen.info("Added hybrid generator.");
    }

    // postInit "Handle interaction with other mods, complete your setup based on this."
    public void postInit(FMLPostInitializationEvent event) {

    }

    public void serverAboutToStart(FMLServerAboutToStartEvent event) {

    }

    // register server commands in this event handler
    public void serverStarting(FMLServerStartingEvent event) {

    }

    public void serverStarted(FMLServerStartedEvent event) {

    }

    public void serverStopping(FMLServerStoppingEvent event) {

    }

    public void serverStopped(FMLServerStoppedEvent event) {

    }
}
