package com.leecrafts.thingamabobs.enchantment;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import com.leecrafts.thingamabobs.enchantment.custom.LongBoingEnchantment;
import com.leecrafts.thingamabobs.enchantment.custom.StickyBoxingGloveEnchantment;
import com.leecrafts.thingamabobs.item.custom.SpringLoadedBoxingGloveItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments {

    public static final EnchantmentCategory PUNCHY_GLOVE_ENCHANTMENT_CATEGORY =
            EnchantmentCategory.create("punchy_glove", item -> item instanceof SpringLoadedBoxingGloveItem);

    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, ThingamabobsAndDoohickeys.MODID);

    public static final RegistryObject<Enchantment> BOING =
            ENCHANTMENTS.register("boing",
                    () -> new LongBoingEnchantment(Enchantment.Rarity.COMMON, EquipmentSlot.MAINHAND));

    public static final RegistryObject<Enchantment> STICKY =
            ENCHANTMENTS.register("sticky",
                    () -> new StickyBoxingGloveEnchantment(Enchantment.Rarity.VERY_RARE, EquipmentSlot.MAINHAND));

    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }

}
