package com.leecrafts.thingamabobs.enchantment;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import com.leecrafts.thingamabobs.enchantment.custom.MalletSpeedEnchantment;
import com.leecrafts.thingamabobs.enchantment.custom.PunchyGloveRangeEnchantment;
import com.leecrafts.thingamabobs.enchantment.custom.MalletDamageEnchantment;
import com.leecrafts.thingamabobs.enchantment.custom.PunchyGloveStickinessEnchantment;
import com.leecrafts.thingamabobs.item.custom.ComicallyLargeMalletItem;
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

    public static final EnchantmentCategory MALLET_ENCHANTMENT_CATEGORY =
            EnchantmentCategory.create("mallet", item -> item instanceof ComicallyLargeMalletItem);

    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, ThingamabobsAndDoohickeys.MODID);

    public static final RegistryObject<Enchantment> BOING =
            ENCHANTMENTS.register("boing",
                    () -> new PunchyGloveRangeEnchantment(Enchantment.Rarity.COMMON, EquipmentSlot.MAINHAND));

    public static final RegistryObject<Enchantment> STICKY =
            ENCHANTMENTS.register("sticky",
                    () -> new PunchyGloveStickinessEnchantment(Enchantment.Rarity.VERY_RARE, EquipmentSlot.MAINHAND));

    public static final RegistryObject<Enchantment> WHAM =
            ENCHANTMENTS.register("wham",
                    () -> new MalletDamageEnchantment(Enchantment.Rarity.COMMON, EquipmentSlot.MAINHAND));

    public static final RegistryObject<Enchantment> HANDLING =
            ENCHANTMENTS.register("handling",
                    () -> new MalletSpeedEnchantment(Enchantment.Rarity.COMMON, EquipmentSlot.MAINHAND));

    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }

}
