package com.leecrafts.thingamabobs.damage;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public class ModDamageSources {

    private final Registry<DamageType> damageTypes;

    public ModDamageSources(RegistryAccess registryAccess) {
        this.damageTypes = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);
    }

    private DamageSource source(ResourceKey<DamageType> resourceKey) {
        return new DamageSource(this.damageTypes.getHolderOrThrow(resourceKey));
    }

    private DamageSource source(ResourceKey<DamageType> resourceKey, @Nullable Entity entity) {
        return new DamageSource(this.damageTypes.getHolderOrThrow(resourceKey), entity);
    }

    private DamageSource source(ResourceKey<DamageType> resourceKey, @Nullable Entity directEntity, @Nullable Entity causingEntity) {
        return new DamageSource(this.damageTypes.getHolderOrThrow(resourceKey), directEntity, causingEntity);
    }

    public DamageSource wham(LivingEntity livingEntity) {
        return this.source(ModDamageTypes.WHAM, livingEntity);
    }

    public DamageSource wallop(Entity entity, @Nullable LivingEntity livingEntity) {
        return this.source(ModDamageTypes.WALLOP, entity, livingEntity);
    }

}
