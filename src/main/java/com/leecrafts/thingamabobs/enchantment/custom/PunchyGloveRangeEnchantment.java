package com.leecrafts.thingamabobs.enchantment.custom;

import com.leecrafts.thingamabobs.enchantment.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

public class PunchyGloveRangeEnchantment extends Enchantment {

    public PunchyGloveRangeEnchantment(Rarity pRarity, EquipmentSlot... pApplicableSlots) {
        super(pRarity, ModEnchantments.PUNCHY_GLOVE_ENCHANTMENT_CATEGORY, pApplicableSlots);
    }

    @Override
    public int getMinCost(int pEnchantmentLevel) {
        return 1 + (pEnchantmentLevel - 1) * 10;
    }

    @Override
    public int getMaxCost(int pEnchantmentLevel) {
        return this.getMinCost(pEnchantmentLevel) + 15;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

}
