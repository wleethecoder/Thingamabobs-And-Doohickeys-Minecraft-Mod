package com.leecrafts.thingamabobs.entity.custom;

import com.leecrafts.thingamabobs.entity.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class ExplosiveCakeEntity extends AbstractExplosivePastryEntity {

    public ExplosiveCakeEntity(EntityType<? extends ExplosiveCakeEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ExplosiveCakeEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.EXPLOSIVE_CAKE.get(), shooter, level);
    }

    public ExplosiveCakeEntity(Level level, double x, double y, double z) {
        super(ModEntityTypes.EXPLOSIVE_CAKE.get(), level, x, y, z);
    }

}
