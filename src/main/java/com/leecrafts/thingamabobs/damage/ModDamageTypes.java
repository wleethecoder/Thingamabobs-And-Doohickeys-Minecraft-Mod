package com.leecrafts.thingamabobs.damage;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public interface ModDamageTypes {

    ResourceKey<DamageType> WHAM = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "wham"));
    ResourceKey<DamageType> WALLOP = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "wallop"));

}
