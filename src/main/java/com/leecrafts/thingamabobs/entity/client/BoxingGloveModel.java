package com.leecrafts.thingamabobs.entity.client;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import com.leecrafts.thingamabobs.entity.custom.BoxingGloveEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BoxingGloveModel extends GeoModel<BoxingGloveEntity> {

    private static final ResourceLocation BOXING_GLOVE_MODEL = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "geo/punchy_glove_projectile.geo.json");
    private static final ResourceLocation BOXING_GLOVE_TEXTURE = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "textures/entity/punchy_glove_projectile.png");

    @Override
    public ResourceLocation getModelResource(BoxingGloveEntity animatable) {
        return BOXING_GLOVE_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(BoxingGloveEntity animatable) {
        return BOXING_GLOVE_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(BoxingGloveEntity animatable) {
        return null;
    }

}
