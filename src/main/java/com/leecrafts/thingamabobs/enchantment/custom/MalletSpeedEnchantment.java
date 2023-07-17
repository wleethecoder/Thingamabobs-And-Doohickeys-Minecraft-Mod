package com.leecrafts.thingamabobs.enchantment.custom;

import com.leecrafts.thingamabobs.enchantment.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

public class MalletSpeedEnchantment extends Enchantment {

    public MalletSpeedEnchantment(Rarity pRarity, EquipmentSlot... pApplicableSlots) {
        super(pRarity, ModEnchantments.MALLET_ENCHANTMENT_CATEGORY, pApplicableSlots);
    }

    public int getMinCost(int pEnchantmentLevel) {
        return 12 + (pEnchantmentLevel - 1) * 20;
    }

    public int getMaxCost(int pEnchantmentLevel) {
        return this.getMinCost(pEnchantmentLevel) + 25;
    }

    public int getMaxLevel() {
        return 2;
    }

}
