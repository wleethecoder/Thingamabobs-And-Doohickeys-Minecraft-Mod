package com.leecrafts.thingamabobs.item.client;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import com.leecrafts.thingamabobs.item.custom.ComicallyLargeMalletItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ComicallyLargeMalletModel extends GeoModel<ComicallyLargeMalletItem> {

    private static final ResourceLocation COMICALLY_LARGE_MALLET_MODEL = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "geo/mallet.geo.json");
    private static final ResourceLocation COMICALLY_LARGE_MALLET_TEXTURE = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "textures/item/mallet.png");

    @Override
    public ResourceLocation getModelResource(ComicallyLargeMalletItem animatable) {
        return COMICALLY_LARGE_MALLET_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ComicallyLargeMalletItem animatable) {
        return COMICALLY_LARGE_MALLET_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ComicallyLargeMalletItem animatable) {
        return null;
    }

}
