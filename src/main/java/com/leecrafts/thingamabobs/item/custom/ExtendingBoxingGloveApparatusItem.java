package com.leecrafts.thingamabobs.item.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeItem;
import org.jetbrains.annotations.NotNull;

public class ExtendingBoxingGloveApparatusItem extends Item implements Vanishable, IForgeItem {

    private static final double PROJECTILE_DAMAGE = 10.0;
    private static final int MAX_CHARGE_DURATION = 25;

    public ExtendingBoxingGloveApparatusItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);
        if (isCharged(itemStack)) {
            System.out.println("get walloped!");
            shootProjectile(pLevel, pPlayer, pUsedHand, itemStack);
            setCharged(itemStack, false);
            return InteractionResultHolder.consume(itemStack);
        }
        pPlayer.startUsingItem(pUsedHand);
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public void releaseUsing(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull LivingEntity pLivingEntity, int pTimeCharged) {
        if (pTimeCharged > MAX_CHARGE_DURATION && !isCharged(pStack)) {
            System.out.println("punchy glove is now charged");
            setCharged(pStack, true);
        }
    }

    public static boolean isCharged(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        return compoundTag != null && compoundTag.getBoolean("Charged");
    }

    public static void setCharged(ItemStack itemStack, boolean isCharged) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        compoundTag.putBoolean("Charged", isCharged);
    }

    private static void shootProjectile(Level level, LivingEntity shooter, InteractionHand interactionHand, ItemStack itemStack) {
        if (!level.isClientSide) {
            // TODO extending boxing glove apparatus projectile
        }
    }

}
