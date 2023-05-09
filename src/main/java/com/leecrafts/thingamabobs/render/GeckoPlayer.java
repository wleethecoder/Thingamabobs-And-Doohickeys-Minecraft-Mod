package com.leecrafts.thingamabobs.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GeckoPlayer implements GeoAnimatable {

    protected GeckoPlayerRenderer renderer;
    protected GeckoPlayerModel model; // extend GeoModel?
    private Player player;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static GeckoPlayerRenderer GECKO_RENDERER_NORMAL;
    public static GeckoPlayerModel GECKO_MODEL_NORMAL;
    public static GeckoPlayerRenderer GECKO_RENDERER_SLIM;
    public static GeckoPlayerModel GECKO_MODEL_SLIM;

    public GeckoPlayer(Player player) {
        this.player = player;
        this.setup(player);
    }

    public GeckoPlayerRenderer getRenderer() {
        return this.renderer;
    }

    public GeckoPlayerModel getModel() {
        return this.model;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setup(Player player) {
        if (((AbstractClientPlayer) player).getModelName().equals("slim")) {
            this.model = GECKO_MODEL_SLIM;
            this.renderer = GECKO_RENDERER_SLIM;
        }
        else {
            this.model = GECKO_MODEL_NORMAL;
            this.renderer = GECKO_RENDERER_NORMAL;
        }
        System.out.println(this.model);
        System.out.println(this.renderer);
    }

    public static void initRenderer() {
        GECKO_MODEL_NORMAL = new GeckoPlayerModel();
        GECKO_MODEL_SLIM = new GeckoPlayerModel();
        GECKO_MODEL_SLIM.setSmallArms(true);

        Minecraft minecraft = Minecraft.getInstance();
        EntityRenderDispatcher entityRenderDispatcher = minecraft.getEntityRenderDispatcher();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        BlockRenderDispatcher blockRenderDispatcher = minecraft.getBlockRenderer();
        ItemInHandRenderer itemInHandRenderer = entityRenderDispatcher.getItemInHandRenderer();
        ResourceManager resourceManager = minecraft.getResourceManager();
        EntityModelSet entityModelSet = minecraft.getEntityModels();
        Font font = minecraft.font;
        EntityRendererProvider.Context context = new EntityRendererProvider.Context(entityRenderDispatcher, itemRenderer, blockRenderDispatcher, itemInHandRenderer, resourceManager, entityModelSet, font);
        GeckoPlayerRenderer geckoPlayerRenderer = new GeckoPlayerRenderer(context, false, GECKO_MODEL_NORMAL);
        GECKO_RENDERER_NORMAL = geckoPlayerRenderer;
        GeckoPlayerRenderer geckoPlayerRendererSlim = new GeckoPlayerRenderer(context, true, GECKO_MODEL_SLIM);
        GECKO_RENDERER_SLIM = geckoPlayerRendererSlim;
    }

    private <E extends GeoAnimatable> PlayState predicate(AnimationState<E> event) {
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "malletController", 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object o) {
        return ((Entity) o).tickCount;
    }

}
