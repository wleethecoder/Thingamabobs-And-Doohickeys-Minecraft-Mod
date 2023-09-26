package com.leecrafts.thingamabobs.item;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import com.leecrafts.thingamabobs.item.custom.ComicallyLargeMalletItem;
import com.leecrafts.thingamabobs.item.custom.ExplosiveCakeItem;
import com.leecrafts.thingamabobs.item.custom.ExplosivePumpkinPieItem;
import com.leecrafts.thingamabobs.item.custom.SpringLoadedBoxingGloveItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
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

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
