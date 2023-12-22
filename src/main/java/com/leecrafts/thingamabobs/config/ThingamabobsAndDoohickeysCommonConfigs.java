package com.leecrafts.thingamabobs.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ThingamabobsAndDoohickeysCommonConfigs {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> PUNCHY_GLOVE_GRIEFING;

    static {
        BUILDER.push("Configs for Thingamabobs and Doohickeys");

        PUNCHY_GLOVE_GRIEFING = BUILDER.comment("Warning: If this is set to true, glass-based builds may be in danger in multiplayer servers.")
                .define("Spring-Loaded Boxing Glove Griefing", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

}
