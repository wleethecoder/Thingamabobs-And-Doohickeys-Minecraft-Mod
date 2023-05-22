package com.leecrafts.thingamabobs.item.client;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import com.leecrafts.thingamabobs.item.custom.SpringLoadedBoxingGloveItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SpringLoadedBoxingGloveModel extends GeoModel<SpringLoadedBoxingGloveItem> {

    private static final ResourceLocation SPRING_LOADED_BOXING_GLOVE_MODEL = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "geo/punchy_glove.geo.json");
    private static final ResourceLocation SPRING_LOADED_BOXING_GLOVE_TEXTURE = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "textures/item/punchy_glove.png");
    private static final ResourceLocation SPRING_LOADED_BOXING_GLOVE_ANIMATION = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "animations/punchy_glove.animation.json");

    @Override
    public ResourceLocation getModelResource(SpringLoadedBoxingGloveItem animatable) {
        return SPRING_LOADED_BOXING_GLOVE_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SpringLoadedBoxingGloveItem animatable) {
        return SPRING_LOADED_BOXING_GLOVE_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SpringLoadedBoxingGloveItem animatable) {
        return SPRING_LOADED_BOXING_GLOVE_ANIMATION;
    }

}
