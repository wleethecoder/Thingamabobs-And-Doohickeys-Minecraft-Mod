package com.leecrafts.thingamabobs.render;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

public class GeckoPlayerModel extends GeoModel<GeckoPlayer> {

    private ResourceLocation texture;
    protected boolean smallArms;

    @Override
    public ResourceLocation getModelResource(GeckoPlayer animatable) {
        System.out.println("wankers model heah:");
        return new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "geo/player.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GeckoPlayer animatable) {
        return this.texture;
    }

    @Override
    public ResourceLocation getAnimationResource(GeckoPlayer animatable) {
        return new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "animations/player.animation.json");
    }

    public void setTexture(AbstractClientPlayer player) {
        this.texture = player.getSkinTextureLocation();
        System.out.println("texture is here: " + (this.texture != null));
    }

    public void setSmallArms(boolean smallArms) {
        this.smallArms = smallArms;
    }

}
