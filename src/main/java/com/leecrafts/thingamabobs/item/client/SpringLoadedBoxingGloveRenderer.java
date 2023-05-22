package com.leecrafts.thingamabobs.item.client;

import com.leecrafts.thingamabobs.item.custom.SpringLoadedBoxingGloveItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SpringLoadedBoxingGloveRenderer extends GeoItemRenderer<SpringLoadedBoxingGloveItem> {

    public SpringLoadedBoxingGloveRenderer() {
        super(new SpringLoadedBoxingGloveModel());
    }

}
