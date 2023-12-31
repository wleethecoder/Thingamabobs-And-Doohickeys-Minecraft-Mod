package com.leecrafts.thingamabobs.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ThingamabobsAndDoohickeysClientConfigs {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> MALLET_FRIENDLY_FIRE;

    static {
        BUILDER.push("Configs for Thingamabobs and Doohickeys");

        MALLET_FRIENDLY_FIRE = BUILDER.comment("If this is set to true, the player using the mallet does not have to hold down right-click in order to hit villagers, iron golems, tamed animals, or other players.")
                .define("Mallet Friendly Fire", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

}
