package com.leecrafts.thingamabobs.entity.client;

import com.leecrafts.thingamabobs.entity.custom.BoxingGloveEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import static net.minecraft.client.renderer.entity.EnderDragonRenderer.CRYSTAL_BEAM_LOCATION;

public class BoxingGloveRenderer extends GeoEntityRenderer<BoxingGloveEntity> {

    private static final RenderType BEAM = RenderType.entitySmoothCutout(CRYSTAL_BEAM_LOCATION);

    public BoxingGloveRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BoxingGloveModel());
        this.shadowRadius = 0.25f;
    }

    @Override
    public void actuallyRender(PoseStack poseStack, BoxingGloveEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        poseStack.mulPose(Axis.YP.rotationDegrees(animatable.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(-animatable.getXRot()));
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void render(@NotNull BoxingGloveEntity entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        Entity shooter = entity.getOwner();
        renderSpring(entity, shooter, partialTick, poseStack, bufferSource, packedLight);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    // TODO change color of spring
    // TODO decrease width of spring
    public static void renderSpring(BoxingGloveEntity entity, Entity shooter, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float gloveHalfWidth = entity.getBbWidth() / 2;
        double x;
        double y;
        double z;
        if (shooter != null) {
            x = shooter.getX();
            y = shooter.getY(2.0/3);
            z = shooter.getZ();
        }
        else {
            x = entity.getInitialPosX();
            y = entity.getInitialPosY();
            z = entity.getInitialPosZ();
        }
        float xDist = (float) (x - Mth.lerp(partialTick, entity.xo, entity.getX()));
        float yDist = (float) (y - Mth.lerp(partialTick, entity.yo, entity.getY()) - gloveHalfWidth);
        float zDist = (float) (z - Mth.lerp(partialTick, entity.zo, entity.getZ()));
//        EnderDragonRenderer.renderCrystalBeams(xDist, yDist, zDist, partialTick, entity.tickCount, poseStack, bufferSource, packedLight);
        float f = Mth.sqrt(xDist * xDist + zDist * zDist);
        float f1 = Mth.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);
        poseStack.pushPose();
        poseStack.translate(0.0F, gloveHalfWidth, 0.0F);
        poseStack.mulPose(Axis.YP.rotation((float)(-Math.atan2(zDist, xDist)) - ((float)Math.PI / 2F)));
        poseStack.mulPose(Axis.XP.rotation((float)(-Math.atan2(f, yDist)) - ((float)Math.PI / 2F)));
        VertexConsumer vertexConsumer = bufferSource.getBuffer(BEAM);
        float f2 = 0.0F - ((float)entity.tickCount + partialTick) * 0.01F;
        float f3 = f1 / 32.0F - ((float)entity.tickCount + partialTick) * 0.01F;
        int i = 8;
        float f4 = 0.0F;
        float f5 = 0.25F;
        float f6 = 0.0F;
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        for(int j = 1; j <= i; ++j) {
            float f7 = Mth.sin((float)j * ((float)Math.PI * 2F) / i) * 0.25F;
            float f8 = Mth.cos((float)j * ((float)Math.PI * 2F) / i) * 0.25F;
            float f9 = (float)j / i;
            vertexConsumer.vertex(matrix4f, f4 * 0.2F, f5 * 0.2F, 0.0F).color(0, 0, 0, 255).uv(f6, f2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, f4, f5, f1).color(255, 255, 255, 255).uv(f6, f3).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, f7, f8, f1).color(255, 255, 255, 255).uv(f9, f3).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, f7 * 0.2F, f8 * 0.2F, 0.0F).color(0, 0, 0, 255).uv(f9, f2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            f4 = f7;
            f5 = f8;
            f6 = f9;
        }

        poseStack.popPose();
    }

}
