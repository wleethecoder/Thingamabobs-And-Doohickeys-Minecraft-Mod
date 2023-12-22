package com.leecrafts.thingamabobs.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ThingamabobsAndDoohickeysServerConfigs {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> EXPLOSIVE_PASTRY_GRIEFING;

    static {
        BUILDER.push("Configs for Thingamabobs and Doohickeys");

        EXPLOSIVE_PASTRY_GRIEFING = BUILDER.comment("Warning: Catastropic results may occur if this is set to true, especially in multiplayer servers.")
                .define("Explosive Pastry Griefing", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

}
