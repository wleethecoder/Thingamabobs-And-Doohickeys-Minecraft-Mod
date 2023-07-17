package com.leecrafts.thingamabobs.entity;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import com.leecrafts.thingamabobs.entity.custom.BoxingGloveEntity;
import com.leecrafts.thingamabobs.entity.custom.ExplosivePumpkinPieEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ThingamabobsAndDoohickeys.MODID);

    public static final RegistryObject<EntityType<BoxingGloveEntity>> BOXING_GLOVE =
            ENTITY_TYPES.register("punchy_glove_projectile",
                    () -> EntityType.Builder.of((EntityType.EntityFactory<BoxingGloveEntity>) BoxingGloveEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .build(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "punchy_glove_projectile").toString()));

    public static final RegistryObject<EntityType<ExplosivePumpkinPieEntity>> EXPLOSIVE_PUMPKIN_PIE =
            ENTITY_TYPES.register("explosive_pumpkin_pie",
                    () -> EntityType.Builder.of((EntityType.EntityFactory<ExplosivePumpkinPieEntity>) ExplosivePumpkinPieEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .build(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "explosive_pumpkin_pie").toString()));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

}
