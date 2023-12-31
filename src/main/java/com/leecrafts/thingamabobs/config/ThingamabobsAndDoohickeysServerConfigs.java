package com.leecrafts.thingamabobs.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ThingamabobsAndDoohickeysServerConfigs {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> PUNCHY_GLOVE_GRIEFING;
    public static final ForgeConfigSpec.ConfigValue<Boolean> EXPLOSIVE_PASTRY_GRIEFING;
    public static final ForgeConfigSpec.ConfigValue<Boolean> MAGNET_AFFECTS_PLAYER;

    static {
        BUILDER.push("Configs for Thingamabobs and Doohickeys");

        PUNCHY_GLOVE_GRIEFING = BUILDER.comment("Warning: If this is set to true, glass-based builds may be in danger in multiplayer servers.")
                .define("Spring-Loaded Boxing Glove Griefing", false);
        EXPLOSIVE_PASTRY_GRIEFING = BUILDER.comment("Warning: Catastropic results may occur if this is set to true, especially in multiplayer servers.")
                .define("Explosive Pastry Griefing", false);
        MAGNET_AFFECTS_PLAYER = BUILDER.comment("If this is set to true, players using magnets can pull in other players that are wearing metallic armor. Severe bullying may occur in multiplayer servers.")
                .define("Magnet Affects Player", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

}
