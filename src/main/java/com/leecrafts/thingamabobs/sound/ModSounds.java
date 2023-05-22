package com.leecrafts.thingamabobs.sound;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ThingamabobsAndDoohickeys.MODID);

    public static final RegistryObject<SoundEvent> COMICALLY_LARGE_MALLET_WHAM = registerSoundEvent("item.mallet.wham");

    public static final RegistryObject<SoundEvent> SPRING_LOADED_BOXING_GLOVE_BOING = registerSoundEvent("item.punchy_glove.boing");

    public static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

}
