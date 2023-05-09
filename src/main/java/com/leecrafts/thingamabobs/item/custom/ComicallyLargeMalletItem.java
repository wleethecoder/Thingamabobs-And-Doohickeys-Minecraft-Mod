package com.leecrafts.thingamabobs.item.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.leecrafts.thingamabobs.capability.ModCapabilities;
import com.leecrafts.thingamabobs.capability.player.PlayerCap;
import com.leecrafts.thingamabobs.item.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.extensions.IForgeItem;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class ComicallyLargeMalletItem extends Item implements Vanishable, IForgeItem {

    public static final double BASE_DAMAGE = 25.0;
    public static final int CHARGE_TIME = (int) (1.75 * TICKS_PER_SECOND);
    public static final double BASE_SPEED = 1.0 * TICKS_PER_SECOND / CHARGE_TIME;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public ComicallyLargeMalletItem(Properties pProperties) {
        super(pProperties);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        // TODO check if you need to subtract by 1
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", BASE_DAMAGE - 1, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", BASE_SPEED - 4, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @Override
    public boolean canAttackBlock(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, Player pPlayer) {
        return !pPlayer.isCreative();
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack pStack, @NotNull LivingEntity pTarget, @NotNull LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, (livingEntity -> livingEntity.broadcastBreakEvent(EquipmentSlot.MAINHAND)));
        return true;
    }

    // TODO remove
    // not sure if you can mine with this weapon
    @Override
    public boolean mineBlock(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockPos pPos, @NotNull LivingEntity pMiningEntity) {
        if (pState.getDestroySpeed(pLevel, pPos) != 0) {
            pStack.hurtAndBreak(2, pMiningEntity, (livingEntity -> livingEntity.broadcastBreakEvent(EquipmentSlot.MAINHAND)));
        }
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getAttributeModifiers(slot, stack);
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 1;
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private static final double EQUIP_X_FP = 1;
            private static final double EQUIP_Y_FP = -1.5;
            private static final double EQUIP_Z_FP = -1;
            private static final double EQUIP_X_ROT_FP = 0;
            private static final double EQUIP_Z_ROT_FP = -45;
            private static final double IDLE_X_FP = 1;
            private static final double IDLE_Y_FP = -0.5;
            private static final double IDLE_Z_FP = -1;
            private static final double IDLE_X_ROT_FP = 0;
            private static final double IDLE_Z_ROT_FP = -45;
            private static final double CHARGE_X_FP = 0;
            private static final double CHARGE_Y_FP = 1;
            private static final double CHARGE_Z_FP = 0;
            private static final double CHARGE_X_ROT_FP = 60;
            private static final double CHARGE_Z_ROT_FP = 0;
            private static final double SWING_X_FP = 0;
            private static final double SWING_Y_FP = -1;
            private static final double SWING_Z_FP = -1;
            private static final double SWING_X_ROT_FP = -90;
            private static final double SWING_Z_ROT_FP = 0;
            private double xFP = EQUIP_X_FP;
            private double yFP = EQUIP_Y_FP;
            private double zFP = EQUIP_Z_FP;
            private double xRotFP = EQUIP_X_ROT_FP;
            private double zRotFP = EQUIP_Z_ROT_FP;
            private double xOFP = EQUIP_X_FP;
            private double yOFP = EQUIP_Y_FP;
            private double zOFP = EQUIP_Z_FP;
            private double xRotOFP = EQUIP_X_ROT_FP;
            private double zRotOFP = EQUIP_Z_ROT_FP;
            private static final int EQUIP_TIME = 5;
            private static final int SWING_TIME = 10;
            private static final int PICKUP_TIME = 15;

//            private HumanoidModel.ArmPose POSE = HumanoidModel.ArmPose.create("mallet", false, (model, entity, arm) -> {
//                entity.getCapability(ModCapabilities.PLAYER_CAPABILITY).ifPresent(iPlayerCap -> {
//                    PlayerCap playerCap = (PlayerCap) iPlayerCap;
//                    double chargeProgress = 1.0 * playerCap.malletCharge / CHARGE_LIMIT;
//                    model.rightArm.zRot = (float) (-20 * Math.PI / 180);
//                    model.rightArm.yRot = (float) Mth.lerp(chargeProgress, 0, 90 * Math.PI / 180);
//                    model.leftArm.zRot = (float) (20 * Math.PI / 180);
//                    model.leftArm.yRot = (float) Mth.lerp(chargeProgress, 0, 90 * Math.PI / 180);
//                });
//            });

            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
                if (player.getOffhandItem() == itemInHand) return false;
                player.getCapability(ModCapabilities.PLAYER_CAPABILITY).ifPresent(iPlayerCap -> {
                    PlayerCap playerCap = (PlayerCap) iPlayerCap;
                    // TODO find a better way (please)
                    int multiplier = arm == HumanoidArm.RIGHT ? 1 : -1;
                    if (playerCap.wasHoldingMallet) {
                        if (playerCap.malletEquipAnim < EQUIP_TIME) {
                            double equipProgress = 1.0 * playerCap.malletEquipAnim / EQUIP_TIME;
                            this.nextPosAndRot(EQUIP_X_FP, EQUIP_Y_FP, EQUIP_Z_FP, IDLE_X_FP, IDLE_Y_FP, IDLE_Z_FP, EQUIP_X_ROT_FP, EQUIP_Z_ROT_FP, IDLE_X_ROT_FP, IDLE_Z_ROT_FP, equipProgress, multiplier);
                            playerCap.malletEquipAnim++;
                        }
                        else if (playerCap.malletSwingAnim == -1) {
                            double chargeProgress = 1.0 * playerCap.malletCharge / CHARGE_TIME;
                            this.nextPosAndRot(IDLE_X_FP, IDLE_Y_FP, IDLE_Z_FP, CHARGE_X_FP, CHARGE_Y_FP, CHARGE_Z_FP, IDLE_X_ROT_FP, IDLE_Z_ROT_FP, CHARGE_X_ROT_FP, CHARGE_Z_ROT_FP, chargeProgress, multiplier);
                        }
                        else if (playerCap.malletSwingAnim < SWING_TIME + 15) {
                            double swingProgress = Math.min(1.0, 1.0 * playerCap.malletSwingAnim / SWING_TIME);
                            this.nextPosAndRot(CHARGE_X_FP, CHARGE_Y_FP, CHARGE_Z_FP, SWING_X_FP, SWING_Y_FP, SWING_Z_FP, CHARGE_X_ROT_FP, CHARGE_Z_ROT_FP, SWING_X_ROT_FP, SWING_Z_ROT_FP, swingProgress, multiplier);
                            playerCap.malletSwingAnim++;
                        }
                        else if (playerCap.malletPickupAnim < PICKUP_TIME) {
                            double pickupProgress = 1.0 * playerCap.malletPickupAnim / PICKUP_TIME;
                            this.nextPosAndRot(SWING_X_FP, SWING_Y_FP, SWING_Z_FP, IDLE_X_FP, IDLE_Y_FP, IDLE_Z_FP, SWING_X_ROT_FP, SWING_Z_ROT_FP, IDLE_X_ROT_FP, IDLE_Z_ROT_FP, pickupProgress, multiplier);
                            playerCap.malletPickupAnim++;
                        }
                        else {
                            playerCap.malletSwingAnim = -1;
                            playerCap.malletPickupAnim = 0;
                        }
                    }
                    else {
                        xFP = EQUIP_X_FP * multiplier;
                        yFP = EQUIP_Y_FP;
                        zFP = EQUIP_Z_FP;
                        xRotFP = EQUIP_X_ROT_FP;
                        zRotFP = EQUIP_Z_ROT_FP * multiplier;
                        xOFP = EQUIP_X_FP * multiplier;
                        yOFP = EQUIP_Y_FP;
                        zOFP = EQUIP_Z_FP;
                        xRotOFP = EQUIP_X_ROT_FP;
                        zRotOFP = EQUIP_Z_ROT_FP * multiplier;
                        playerCap.wasHoldingMallet = true;
                    }
                    poseStack.translate(
                            Mth.lerp(partialTick, xOFP, xFP),
                            Mth.lerp(partialTick, yOFP, yFP),
                            Mth.lerp(partialTick, zOFP, zFP)
                    );
                    poseStack.mulPose(Axis.XP.rotationDegrees((float) Mth.lerp(partialTick, xRotOFP, xRotFP)));
                    poseStack.mulPose(Axis.ZP.rotationDegrees((float) Mth.lerp(partialTick, zRotOFP, zRotFP)));
                    xOFP = xFP;
                    yOFP = yFP;
                    zOFP = zFP;
                    xRotOFP = xRotFP;
                    zRotOFP = zRotFP;
                });
                return true;
            }

            private void nextPosAndRot(double x1, double y1, double z1, double x2, double y2, double z2, double xRot1, double zRot1, double xRot2, double zRot2, double progress, int multiplier) {
                xFP = Mth.lerp(progress, x1 * multiplier, x2);
                yFP = Mth.lerp(progress, y1, y2);
                zFP = Mth.lerp(progress, z1, z2);
                xRotFP = Mth.lerp(progress, xRot1, xRot2);
                zRotFP = Mth.lerp(progress, zRot1 * multiplier, zRot2);
            }

//            @Override
//            public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
//                if (entityLiving instanceof LocalPlayer && entityLiving.getMainHandItem().getItem() == ModItems.COMICALLY_LARGE_MALLET_ITEM.get()) {
//                    return POSE;
//                }
//                return HumanoidModel.ArmPose.EMPTY;
//            }

        });
    }

}
