package com.leecrafts.thingamabobs.item;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import com.leecrafts.thingamabobs.item.custom.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ThingamabobsAndDoohickeys.MODID);

    public static final RegistryObject<Item> COMICALLY_LARGE_MALLET_ITEM = ITEMS.register("mallet",
            () -> new ComicallyLargeMalletItem((new Item.Properties()).durability(250)));

    public static final RegistryObject<Item> SPRING_LOADED_BOXING_GLOVE_ITEM = ITEMS.register("punchy_glove",
            () -> new SpringLoadedBoxingGloveItem((new Item.Properties()).durability(465)));

    public static final RegistryObject<Item> EXPLOSIVE_PUMPKIN_PIE_ITEM = ITEMS.register("explosive_pumpkin_pie",
            () -> new ExplosivePumpkinPieItem((new Item.Properties()).stacksTo(16)));

    public static final RegistryObject<Item> EXPLOSIVE_CAKE_ITEM = ITEMS.register("explosive_cake",
            () -> new ExplosiveCakeItem((new Item.Properties()).stacksTo(16)));

    public static final RegistryObject<Item> COMICALLY_LARGE_MAGNET_ITEM = ITEMS.register("magnet",
            () -> new ComicallyLargeMagnetItem((new Item.Properties()).durability(250)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    @Mod.EventBusSubscriber(modid = ThingamabobsAndDoohickeys.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModItemsEvents {

        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                DispenserBlock.registerBehavior(ModItems.SPRING_LOADED_BOXING_GLOVE_ITEM.get(),
                        new SpringLoadedBoxingGloveItem.SpringLoadedBoxingGloveDispenseItemBehavior());
                DispenserBlock.registerBehavior(ModItems.EXPLOSIVE_PUMPKIN_PIE_ITEM.get(),
                        new ExplosivePumpkinPieItem.ExplosivePumpkinPieDispenseItemBehavior());
                DispenserBlock.registerBehavior(ModItems.EXPLOSIVE_CAKE_ITEM.get(),
                        new ExplosiveCakeItem.ExplosiveCakeDispenseItemBehavior());
            });
        }

    }

}
