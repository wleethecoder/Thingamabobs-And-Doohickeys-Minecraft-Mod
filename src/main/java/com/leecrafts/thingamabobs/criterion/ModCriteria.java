package com.leecrafts.thingamabobs.criterion;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import com.leecrafts.thingamabobs.criterion.custom.HitByAOEWeaponTrigger;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModCriteria {

    public static HitByAOEWeaponTrigger HIT_BY_AOE_WEAPON;

    @Mod.EventBusSubscriber(modid = ThingamabobsAndDoohickeys.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModCriteriaEvents {

        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                HIT_BY_AOE_WEAPON = CriteriaTriggers.register(new HitByAOEWeaponTrigger());
            });
        }

    }

}
