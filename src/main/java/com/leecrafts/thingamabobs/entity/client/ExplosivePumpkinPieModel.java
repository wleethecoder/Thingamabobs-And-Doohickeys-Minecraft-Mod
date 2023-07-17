package com.leecrafts.thingamabobs.entity.client;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import com.leecrafts.thingamabobs.entity.custom.ExplosivePumpkinPieEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ExplosivePumpkinPieModel extends GeoModel<ExplosivePumpkinPieEntity> {

    private static final ResourceLocation EXPLOSIVE_PUMPKIN_PIE_MODEL = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "geo/explosive_pumpkin_pie.geo.json");
    private static final ResourceLocation EXPLOSIVE_PUMPKIN_PIE_TEXTURE = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "textures/entity/explosive_pumpkin_pie.png");
    
    @Override
    public ResourceLocation getModelResource(ExplosivePumpkinPieEntity animatable) {
        return EXPLOSIVE_PUMPKIN_PIE_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ExplosivePumpkinPieEntity animatable) {
        return EXPLOSIVE_PUMPKIN_PIE_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ExplosivePumpkinPieEntity animatable) {
        return null;
    }

}
