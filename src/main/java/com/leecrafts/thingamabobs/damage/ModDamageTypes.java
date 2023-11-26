package com.leecrafts.thingamabobs.damage;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public interface ModDamageTypes {

    ResourceKey<DamageType> WHAM = register("wham");
    ResourceKey<DamageType> WALLOP = register("wallop");

    private static ResourceKey<DamageType> register(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(ThingamabobsAndDoohickeys.MODID, name));
    }

    static void bootstrap(BootstapContext<DamageType> bootstapContext) {
        bootstapContext.register(WHAM, new DamageType("wham", 0.1f));
        bootstapContext.register(WALLOP, new DamageType("wallop", 0.1f));
    }

}
