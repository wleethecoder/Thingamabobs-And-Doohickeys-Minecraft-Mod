package com.leecrafts.thingamabobs.enchantment.custom;

import com.leecrafts.thingamabobs.enchantment.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;

public class StickyBoxingGloveEnchantment extends Enchantment {

    public StickyBoxingGloveEnchantment(Rarity pRarity, EquipmentSlot... pApplicableSlots) {
        super(pRarity, ModEnchantments.PUNCHY_GLOVE_ENCHANTMENT_CATEGORY, pApplicableSlots);
    }

    public int getMinCost(int pEnchantmentLevel) {
        return 20;
    }

    public int getMaxCost(int pEnchantmentLevel) {
        return 50;
    }

    @Override
    protected boolean checkCompatibility(@NotNull Enchantment pOther) {
        return super.checkCompatibility(pOther) && pOther != Enchantments.PUNCH_ARROWS;
    }
}
