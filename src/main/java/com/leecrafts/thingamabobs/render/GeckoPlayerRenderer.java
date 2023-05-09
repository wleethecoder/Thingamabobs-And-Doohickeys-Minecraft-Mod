package com.leecrafts.thingamabobs.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.common.MinecraftForge;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

public class GeckoPlayerRenderer extends PlayerRenderer implements GeoRenderer<GeckoPlayer> {

    public MultiBufferSource multiBufferSource;
    private GeckoPlayer geckoPlayer;
    private GeckoPlayerModel modelProvider;
    private Matrix4f worldRenderMat;

    public GeckoPlayerRenderer(EntityRendererProvider.Context pContext, boolean pUseSlimModel, GeckoPlayerModel modelProvider) {
        super(pContext, pUseSlimModel);
        PlayerModel<AbstractClientPlayer> animatedPlayerModel = new PlayerModel<>(pContext.bakeLayer(pUseSlimModel ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), pUseSlimModel);
        this.model = animatedPlayerModel;

        this.layers.clear();
        // TODO add layers??

        this.modelProvider = modelProvider;
        this.modelProvider.setSmallArms(pUseSlimModel);

        this.worldRenderMat = new Matrix4f();
        this.worldRenderMat.identity();
    }

    public void render(AbstractClientPlayer player, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, GeckoPlayer geckoPlayer) {
        this.multiBufferSource = multiBufferSource;
        this.geckoPlayer = geckoPlayer;
        // TODO set model visibilities?
        this.renderLiving(player, yaw, partialTicks, poseStack, multiBufferSource, packedLight, geckoPlayer);
    }

    public void renderLiving(AbstractClientPlayer player, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, GeckoPlayer geckoPlayer) {
        poseStack.pushPose();
        float f = Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot);
        float f1 = Mth.rotLerp(partialTicks, player.yHeadRotO, player.yHeadRot);
        float f2 = f1 - f;
        float f3 = Mth.lerp(partialTicks, player.xRotO, player.getXRot());
        float f4 = this.getBob(player, partialTicks);
        this.scale(player, poseStack, partialTicks);
        float f5 = 0.0f;
        float f6 = 0.0f;
        // TODO riding
        if (!this.model.riding && player.isAlive()) {
            f5 = Mth.lerp(partialTicks, player.animStepO, player.animStep);
            f6 = player.walkDist - player.animStep * (1.0f - partialTicks);
            if (f5 > 1.0f) {
                f5 = 1.0f;
            }
        }
        this.modelProvider.setCustomAnimations(geckoPlayer, player.getUUID().hashCode(), new AnimationState<>(geckoPlayer, 0, 0, partialTicks, false));
        // TODO check if this.modelProvider.getAnimationProcessor() has model render list
        this.applyRotations(player, poseStack, f, f1, partialTicks);
        // TODO set rotations for model?
//        Minecraft minecraft = Minecraft.getInstance();
        // TODO account for invisibility + glowing
        RenderType renderType = this.getRenderType(player, true, false, false);
        if (renderType != null) {
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(renderType);
//            int i = getOverlayCoords(player, this.getWhiteOverlayProgress(player, partialTicks));
            poseStack.pushPose();
            this.worldRenderMat.set(poseStack.last().pose());
            this.modelProvider.setTexture(player);
            defaultRender(poseStack, geckoPlayer, multiBufferSource, renderType, vertexConsumer, yaw, partialTicks, packedLight);
            poseStack.popPose();
            this.model.setupAnim(player, f6, f5, f4, f2, f3);
        }

        if (!player.isSpectator()) {
            for (RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayer : this.layers) {
                renderLayer.render(poseStack, multiBufferSource, packedLight, player, f6, f5, partialTicks, f4, f2, f3);
            }
        }

        poseStack.popPose();
        MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre<>(player, this, partialTicks, poseStack, multiBufferSource, packedLight));
    }

    protected void applyRotations(AbstractClientPlayer player, PoseStack poseStack, float yaw, float headYaw, float partialTicks) {
        // TODO modelProvider's rotation
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - Mth.rotLerp(partialTicks, headYaw, yaw)));
        // TODO dinnerbone
    }

    @Override
    public GeoModel<GeckoPlayer> getGeoModel() {
        return this.modelProvider;
    }

    @Override
    public GeckoPlayer getAnimatable() {
        return this.geckoPlayer;
    }

    @Override
    public void fireCompileRenderLayersEvent() {

    }

    @Override
    public boolean firePreRenderEvent(PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
        return false;
    }

    @Override
    public void firePostRenderEvent(PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {

    }

}
