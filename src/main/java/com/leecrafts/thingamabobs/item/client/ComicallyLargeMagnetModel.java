package com.leecrafts.thingamabobs.item.client;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import com.leecrafts.thingamabobs.item.custom.ComicallyLargeMagnetItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ComicallyLargeMagnetModel extends GeoModel<ComicallyLargeMagnetItem> {

    private static final ResourceLocation COMICALLY_LARGE_MAGNET_MODEL = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "geo/magnet.geo.json");
    private static final ResourceLocation COMICALLY_LARGE_MAGNET_TEXTURE = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "textures/item/magnet.png");

    @Override
    public ResourceLocation getModelResource(ComicallyLargeMagnetItem animatable) {
        return COMICALLY_LARGE_MAGNET_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ComicallyLargeMagnetItem animatable) {
        return COMICALLY_LARGE_MAGNET_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ComicallyLargeMagnetItem animatable) {
        return null;
    }

}
