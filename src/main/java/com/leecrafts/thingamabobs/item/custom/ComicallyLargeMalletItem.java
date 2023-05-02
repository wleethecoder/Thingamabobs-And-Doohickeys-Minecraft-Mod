package com.leecrafts.thingamabobs.item.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraftforge.common.extensions.IForgeItem;
import org.jetbrains.annotations.NotNull;

public class ComicallyLargeMalletItem extends Item implements Vanishable, IForgeItem {

    public static final double BASE_DAMAGE = 25.0;
    public static final double BASE_SPEED = 0.5;
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

    // might remove
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

}
