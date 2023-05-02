package com.leecrafts.thingamabobs.event;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import com.leecrafts.thingamabobs.capability.player.IPlayerCap;
import com.leecrafts.thingamabobs.capability.player.PlayerCapProvider;
import com.leecrafts.thingamabobs.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ModEvents {

    @Mod.EventBusSubscriber(modid = ThingamabobsAndDoohickeys.MODID)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.register(IPlayerCap.class);
        }

        @SubscribeEvent
        public static void attachCapabilitiesEventPlayer(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player player && !player.getCommandSenderWorld().isClientSide) {
                PlayerCapProvider playerCapProvider = new PlayerCapProvider();
                event.addCapability(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "left_click"), playerCapProvider);
            }
        }

    }

    @Mod.EventBusSubscriber(modid = ThingamabobsAndDoohickeys.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents {

        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                LocalPlayer localPlayer = Minecraft.getInstance().player;
                if (localPlayer != null) {
                    Item mainHandItem = localPlayer.getMainHandItem().getItem();
                    if (localPlayer.tickCount % 10 == 0) {
                        if (mainHandItem == ModItems.COMICALLY_LARGE_MALLET_ITEM.get()) System.out.println("holding the mallet");
                    }
                    boolean leftMouse = Minecraft.getInstance().mouseHandler.isLeftPressed();
                    if (leftMouse) {
                        System.out.println("left mouse button pressed " + localPlayer.getRandom().nextInt(10));
                    }
                }
            }
        }

    }

//    @Mod.EventBusSubscriber(modid = ThingamabobsAndDoohickeys.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
//    public static class ModEventBusEvents {
//    }

}
