package com.leecrafts.thingamabobs.entity.custom;

import com.leecrafts.thingamabobs.entity.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class ExplosivePumpkinPieEntity extends AbstractExplosivePastryEntity {

    public ExplosivePumpkinPieEntity(EntityType<? extends ExplosivePumpkinPieEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ExplosivePumpkinPieEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.EXPLOSIVE_PUMPKIN_PIE.get(), shooter, level);
    }

}
