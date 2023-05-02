package com.leecrafts.thingamabobs.item;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import com.leecrafts.thingamabobs.item.custom.ComicallyLargeMalletItem;
import com.leecrafts.thingamabobs.item.custom.ExtendingBoxingGloveApparatusItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ThingamabobsAndDoohickeys.MODID);

    public static final RegistryObject<Item> COMICALLY_LARGE_MALLET_ITEM = ITEMS.register("mallet",
            () -> new ComicallyLargeMalletItem((new Item.Properties()).durability(100)));

    public static final RegistryObject<Item> EXTENDING_BOXING_GLOVE_APPARATUS_ITEM = ITEMS.register("punchy_glove",
            () -> new ExtendingBoxingGloveApparatusItem((new Item.Properties()).durability(100)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
