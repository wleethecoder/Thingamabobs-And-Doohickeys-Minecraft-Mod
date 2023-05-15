package com.leecrafts.thingamabobs.item.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.leecrafts.thingamabobs.capability.ModCapabilities;
import com.leecrafts.thingamabobs.capability.player.PlayerMalletCap;
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

    private static final double EQUIP_X = 1;
    private static final double EQUIP_Y = -1.5;
    private static final double EQUIP_Z = -1;
    private static final double EQUIP_X_ROT = 0;
    private static final double EQUIP_Z_ROT = -45;
    private static final double IDLE_X = 1;
    private static final double IDLE_Y = -0.5;
    private static final double IDLE_Z = -1;
    private static final double IDLE_X_ROT = 0;
    private static final double IDLE_Z_ROT = -45;
    private static final double CHARGE_X = 0;
    private static final double CHARGE_Y = 1;
    private static final double CHARGE_Z = 0;
    private static final double CHARGE_X_ROT = 60;
    private static final double CHARGE_Z_ROT = 0;
    private static final double SWING_X = 0;
    private static final double SWING_Y = -1;
    private static final double SWING_Z = -1;
    private static final double SWING_X_ROT = -90;
    private static final double SWING_Z_ROT = 0;
    private double x = EQUIP_X;
    private double y = EQUIP_Y;
    private double z = EQUIP_Z;
    private double xRot = EQUIP_X_ROT;
    private double zRot = EQUIP_Z_ROT;
    private double xO = EQUIP_X;
    private double yO = EQUIP_Y;
    private double zO = EQUIP_Z;
    private double xRotO = EQUIP_X_ROT;
    private double zRotO = EQUIP_Z_ROT;
    public static final int EQUIP_TIME = 5;
    public static final int SWING_TIME = 3;
    public static final int PICKUP_TIME = 7;

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

            // This method does not fire if the camera is in third person.
            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
                if (player.getOffhandItem() == itemInHand) return false;
                player.getCapability(ModCapabilities.PLAYER_MALLET_CAPABILITY).ifPresent(iPlayerCap -> {
                    PlayerMalletCap playerMalletCap = (PlayerMalletCap) iPlayerCap;
                    int multiplier = arm == HumanoidArm.RIGHT ? 1 : -1;
                    if (playerMalletCap.wasHoldingMallet) {
                        if (playerMalletCap.firstPersonMalletEquipAnim < EQUIP_TIME) {
                            double equipProgress = Math.min(1.0, (playerMalletCap.firstPersonMalletEquipAnim + partialTick) / EQUIP_TIME);
                            this.nextPosAndRot(EQUIP_X, EQUIP_Y, EQUIP_Z, IDLE_X, IDLE_Y, IDLE_Z, EQUIP_X_ROT, EQUIP_Z_ROT, IDLE_X_ROT, IDLE_Z_ROT, equipProgress, multiplier);
                        }
                        else if (playerMalletCap.firstPersonMalletSwingAnim == -1) {
                            boolean progressing = playerMalletCap.malletCharge > 0;
                            double chargeProgress = Math.min(1.0, (playerMalletCap.malletCharge + (progressing ? partialTick : 0) - playerMalletCap.firstPersonMalletChargeOffset) / (CHARGE_TIME - playerMalletCap.firstPersonMalletChargeOffset));
                            this.nextPosAndRot(IDLE_X, IDLE_Y, IDLE_Z, CHARGE_X, CHARGE_Y, CHARGE_Z, IDLE_X_ROT, IDLE_Z_ROT, CHARGE_X_ROT, CHARGE_Z_ROT, chargeProgress, multiplier);
                        }
                        else if (playerMalletCap.firstPersonMalletSwingAnim < SWING_TIME + 10) {
                            double swingProgress = Math.min(1.0, (playerMalletCap.firstPersonMalletSwingAnim + partialTick) / SWING_TIME);
                            this.nextPosAndRot(CHARGE_X, CHARGE_Y, CHARGE_Z, SWING_X, SWING_Y, SWING_Z, CHARGE_X_ROT, CHARGE_Z_ROT, SWING_X_ROT, SWING_Z_ROT, swingProgress, multiplier);
                        }
                        else if (playerMalletCap.firstPersonMalletPickupAnim < PICKUP_TIME) {
                            double pickupProgress = (playerMalletCap.firstPersonMalletPickupAnim + partialTick) / PICKUP_TIME;
                            this.nextPosAndRot(SWING_X, SWING_Y, SWING_Z, IDLE_X, IDLE_Y, IDLE_Z, SWING_X_ROT, SWING_Z_ROT, IDLE_X_ROT, IDLE_Z_ROT, pickupProgress, multiplier);
                        }
                        else {
                            // malletCharge may be > 0 if player tries to attack consecutively, so I try to animate that smoothly
                            playerMalletCap.firstPersonMalletChargeOffset = playerMalletCap.malletCharge;
                        }
                    }
                    else {
                        x = EQUIP_X * multiplier;
                        y = EQUIP_Y;
                        z = EQUIP_Z;
                        xRot = EQUIP_X_ROT;
                        zRot = EQUIP_Z_ROT * multiplier;
                        xO = EQUIP_X * multiplier;
                        yO = EQUIP_Y;
                        zO = EQUIP_Z;
                        xRotO = EQUIP_X_ROT;
                        zRotO = EQUIP_Z_ROT * multiplier;
                        playerMalletCap.wasHoldingMallet = true;
                    }
                    poseStack.translate(
                            Mth.lerp(partialTick, xO, x),
                            Mth.lerp(partialTick, yO, y),
                            Mth.lerp(partialTick, zO, z)
                    );
                    poseStack.mulPose(Axis.XP.rotationDegrees((float) Mth.lerp(partialTick, xRotO, xRot)));
                    poseStack.mulPose(Axis.ZP.rotationDegrees((float) Mth.lerp(partialTick, zRotO, zRot)));
                    xO = x;
                    yO = y;
                    zO = z;
                    xRotO = xRot;
                    zRotO = zRot;
                });
                return true;
            }

            private void nextPosAndRot(double x1, double y1, double z1, double x2, double y2, double z2, double xRot1, double zRot1, double xRot2, double zRot2, double progress, int multiplier) {
                x = Mth.lerp(progress, x1 * multiplier, x2 * multiplier);
                y = Mth.lerp(progress, y1, y2);
                z = Mth.lerp(progress, z1, z2);
                xRot = Mth.lerp(progress, xRot1, xRot2);
                zRot = Mth.lerp(progress, zRot1 * multiplier, zRot2 * multiplier);
            }

        });
    }

}
