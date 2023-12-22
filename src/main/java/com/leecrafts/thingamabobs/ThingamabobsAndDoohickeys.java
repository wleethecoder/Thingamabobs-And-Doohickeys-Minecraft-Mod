package com.leecrafts.thingamabobs;

import com.leecrafts.thingamabobs.config.ThingamabobsAndDoohickeysCommonConfigs;
import com.leecrafts.thingamabobs.config.ThingamabobsAndDoohickeysServerConfigs;
import com.leecrafts.thingamabobs.enchantment.ModEnchantments;
import com.leecrafts.thingamabobs.entity.ModEntityTypes;
import com.leecrafts.thingamabobs.entity.client.BoxingGloveRenderer;
import com.leecrafts.thingamabobs.entity.client.ExplosiveCakeRenderer;
import com.leecrafts.thingamabobs.entity.client.ExplosivePumpkinPieRenderer;
import com.leecrafts.thingamabobs.item.ModItems;
import com.leecrafts.thingamabobs.packet.PacketHandler;
import com.leecrafts.thingamabobs.sound.ModSounds;
import com.mojang.logging.LogUtils;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ThingamabobsAndDoohickeys.MODID)
public class ThingamabobsAndDoohickeys
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "thingamabobs";

    public ThingamabobsAndDoohickeys()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModSounds.register(modEventBus);
        ModEntityTypes.register(modEventBus);
        ModEnchantments.register(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        GeckoLib.initialize();

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ThingamabobsAndDoohickeysServerConfigs.SPEC, "thingamabobs-server.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ThingamabobsAndDoohickeysCommonConfigs.SPEC, "thingamabobs-common.toml");

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);


        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(PacketHandler::init);
    }

    private void addCreative(CreativeModeTabEvent.BuildContents event) {
        if (event.getTab() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.COMICALLY_LARGE_MALLET_ITEM);
            event.accept(ModItems.SPRING_LOADED_BOXING_GLOVE_ITEM);
            event.accept(ModItems.EXPLOSIVE_PUMPKIN_PIE_ITEM);
            event.accept(ModItems.EXPLOSIVE_CAKE_ITEM);
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            EntityRenderers.register(ModEntityTypes.BOXING_GLOVE.get(), BoxingGloveRenderer::new);
            EntityRenderers.register(ModEntityTypes.EXPLOSIVE_PUMPKIN_PIE.get(), ExplosivePumpkinPieRenderer::new);
            EntityRenderers.register(ModEntityTypes.EXPLOSIVE_CAKE.get(), ExplosiveCakeRenderer::new);

            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(new ResourceLocation(MODID, "animation"), 42, (player) -> new ModifierLayer<>());
        }
    }
}
