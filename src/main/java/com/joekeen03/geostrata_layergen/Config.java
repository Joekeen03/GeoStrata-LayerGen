package com.joekeen03.geostrata_layergen;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;

public class Config {
    
    private static class Defaults {
        public static final String greeting = "Hello World";
        public static final Boolean retrogen = false;
    }

    private static class Categories {
        public static final String general = "general";
    }
    
    public static String greeting = Defaults.greeting;
    public static Boolean retrogen = Defaults.retrogen;

    public static void syncronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);
        configuration.load();

        Property greetingProperty = configuration.get(Categories.general, "greeting", Defaults.greeting, "How shall I greet?");
        Property retrogenProperty = configuration.get(Categories.general, "Retrogen", Defaults.retrogen, "Set to true if this should retrogen stone.");
        greeting = greetingProperty.getString();
        retrogen = retrogenProperty.getBoolean();

        if (retrogen)
        {
            retrogenProperty.set(false);
        }

        if(configuration.hasChanged()) {
            configuration.save();
        }
    }
}