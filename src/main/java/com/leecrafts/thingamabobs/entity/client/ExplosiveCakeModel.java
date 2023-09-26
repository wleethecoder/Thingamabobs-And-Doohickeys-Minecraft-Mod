package com.leecrafts.thingamabobs.entity.client;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import com.leecrafts.thingamabobs.entity.custom.ExplosiveCakeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ExplosiveCakeModel extends GeoModel<ExplosiveCakeEntity> {

    private static final ResourceLocation EXPLOSIVE_CAKE_MODEL = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "geo/explosive_cake.geo.json");
    private static final ResourceLocation EXPLOSIVE_CAKE_TEXTURE = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "textures/entity/explosive_cake.png");

    @Override
    public ResourceLocation getModelResource(ExplosiveCakeEntity animatable) {
        return EXPLOSIVE_CAKE_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ExplosiveCakeEntity animatable) {
        return EXPLOSIVE_CAKE_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ExplosiveCakeEntity animatable) {
        return null;
    }

}
