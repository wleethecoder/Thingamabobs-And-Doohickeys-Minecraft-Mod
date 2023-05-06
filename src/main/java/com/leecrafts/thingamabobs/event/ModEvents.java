package com.leecrafts.thingamabobs.event;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import com.leecrafts.thingamabobs.capability.ModCapabilities;
import com.leecrafts.thingamabobs.capability.player.IPlayerCap;
import com.leecrafts.thingamabobs.capability.player.PlayerCap;
import com.leecrafts.thingamabobs.capability.player.PlayerCapProvider;
import com.leecrafts.thingamabobs.item.ModItems;
import com.leecrafts.thingamabobs.packet.PacketHandler;
import com.leecrafts.thingamabobs.packet.ServerboundComicallyLargeMalletAttackPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

import static com.leecrafts.thingamabobs.item.custom.ComicallyLargeMalletItem.CHARGE_TIME;

public class ModEvents {

    @Mod.EventBusSubscriber(modid = ThingamabobsAndDoohickeys.MODID)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.register(IPlayerCap.class);
        }

        @SubscribeEvent
        public static void attachCapabilitiesEventPlayer(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof LocalPlayer) {
                PlayerCapProvider playerCapProvider = new PlayerCapProvider();
                event.addCapability(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "mallet_charge"), playerCapProvider);
                event.addCapability(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "mallet_equip_animation"), playerCapProvider);
                event.addCapability(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "mallet_swing_animation"), playerCapProvider);
                event.addCapability(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "mallet_pickup_animation"), playerCapProvider);
                event.addCapability(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "was_holding_mallet"), playerCapProvider);
            }
        }

        @SubscribeEvent
        public static void damageTest(LivingDamageEvent event) {
            if (!event.getEntity().level.isClientSide) {
                System.out.println(event.getAmount() + " damage done to " + event.getEntity().getType().toShortString() + " via " + event.getSource().getMsgId());
            }
        }

        // TODO fix server-client networking issues
        // TODO fix stackable items disappearing
        // TODO maybe add capability that measures how much time item is in offhand slot while mallet is in main hand slot?
        @SubscribeEvent
        public static void holdingMalletTick(LivingEvent.LivingTickEvent event) {
            if (event.getEntity() instanceof Player player && player.getMainHandItem().getItem() == ModItems.COMICALLY_LARGE_MALLET_ITEM.get()) {
                ItemStack offhandItem = player.getOffhandItem();
                ItemStack offhandItem1 = offhandItem.copy();
                offhandItem.shrink(offhandItem.getCount());
                player.addItem(offhandItem1);
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
                    // case 1, player is not pressing left click: charge resets
                    // case 2, player is not holding mallet: charge resets
                    // case 3, player is holding mallet and is pressing left click: charge increments
                    // case 4, charge >= charge limit and player keeps pressing left click while holding mallet: charge no longer increments
                    // case 5, charge >= charge limit and player lets go of left click while holding mallet: player swings down mallet; charge is reset

                    localPlayer.getCapability(ModCapabilities.PLAYER_CAPABILITY).ifPresent(iPlayerCap -> {
                        PlayerCap playerCap = (PlayerCap) iPlayerCap;
                        boolean leftMouse = Minecraft.getInstance().mouseHandler.isLeftPressed();
                        boolean holdingMallet = localPlayer.getMainHandItem().getItem() == ModItems.COMICALLY_LARGE_MALLET_ITEM.get();

//                        if (holdingMallet) {
//                            playerCap.wasHoldingMallet = true;
//                        }

                        if (!holdingMallet || !leftMouse) {
                            if (!leftMouse) {
                                if (playerCap.malletCharge >= CHARGE_TIME) {
                                    // TODO test swinging on server?
                                    localPlayer.swing(InteractionHand.MAIN_HAND);

                                    double radius = localPlayer.getEntityReach() / 2;
                                    Vec3 vec3 = localPlayer.getViewVector(1).scale(radius).add(localPlayer.getEyePosition());
                                    AABB aabb = new AABB(vec3.x - radius, vec3.y - radius, vec3.z - radius, vec3.x + radius, vec3.y + radius, vec3.z + radius);
                                    List<Entity> list = localPlayer.level.getEntities(localPlayer, aabb);
                                    StringBuilder listString = new StringBuilder();
                                    for (Entity entity : list) {
                                        listString.append(entity.getId()).append(",");
                                    }
                                    PacketHandler.INSTANCE.sendToServer(new ServerboundComicallyLargeMalletAttackPacket(listString.toString()));
//                                    HitResult hitResult = Minecraft.getInstance().hitResult;
//                                    if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
//                                        int entityId = ((EntityHitResult) hitResult).getEntity().getId();
//                                        PacketHandler.INSTANCE.sendToServer(new ServerboundComicallyLargeMalletAttackPacket(entityId));
//                                    }
                                    playerCap.malletSwingAnim = 0;
                                }
                            }
                            if (!holdingMallet) {
                                playerCap.malletEquipAnim = 0;
                                playerCap.malletSwingAnim = -1;
                                playerCap.malletPickupAnim = 0;
                                playerCap.wasHoldingMallet = false;
                            }
                            playerCap.malletCharge = 0;

                            // When the charge is 0, the attack indicator will not appear
                            if (holdingMallet) {
                                localPlayer.attackStrengthTicker = CHARGE_TIME;
                            }
                        }
                        else {
                            playerCap.malletCharge = Math.min(CHARGE_TIME, playerCap.malletCharge + 1);
                            localPlayer.attackStrengthTicker = playerCap.malletCharge;
                        }
                    });
                }
            }
        }

        // cancels vanilla mining and swinging mechanics when player presses left click while holding mallet
        @SubscribeEvent
        public static void inputEvent(InputEvent.InteractionKeyMappingTriggered event) {
            if (event.isAttack()) {
                LocalPlayer localPlayer = Minecraft.getInstance().player;
                if (localPlayer != null && localPlayer.getMainHandItem().getItem() == ModItems.COMICALLY_LARGE_MALLET_ITEM.get()) {
                    event.setSwingHand(false);
                    event.setCanceled(true);
                }
            }
        }

//        // Attack bar appears when player is either:
//        // Charging the mallet
//        // Having their crosshair pointed at an entity
//        @SubscribeEvent
//        public static void renderCrosshairEvent(RenderGuiOverlayEvent.Pre event) {
//            Minecraft minecraft = Minecraft.getInstance();
//            LocalPlayer localPlayer = minecraft.player;
//            if (localPlayer != null && localPlayer.getMainHandItem().getItem() == ModItems.COMICALLY_LARGE_MALLET_ITEM.get()) {
//                localPlayer.getCapability(ModCapabilities.PLAYER_CAPABILITY).ifPresent(iPlayerCap -> {
//                    PlayerCap playerCap = (PlayerCap) iPlayerCap;
//                    if (localPlayer.attackStrengthTicker > 0) System.out.println(event.getOverlay().id().toShortLanguageKey());
//                    if (playerCap.malletCharge == 0 && event.getOverlay().id().toShortLanguageKey().equals("crosshair")) {
//                        Options options = minecraft.options;
//                        if (options.getCameraType().isFirstPerson() &&
//                                !localPlayer.isSpectator() &&
//                                (!options.renderDebug || options.hideGui || localPlayer.isReducedDebugInfo() || options.reducedDebugInfo().get())) {
//                            event.setCanceled(true);
//
//                            PoseStack poseStack = event.getPoseStack();
//                            int screenWidth = minecraft.getWindow().getGuiScaledWidth();
//                            int screenHeight = minecraft.getWindow().getGuiScaledHeight();
//                            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
//                            RenderSystem.enableBlend();
//                            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
////                            System.out.println("screen: [" + screenWidth + ", " + screenHeight + "]; crosshair should be at (" + ((screenWidth - 15) / 2) + ", " + ((screenHeight - 15) / 2) + ")");
//                            GuiComponent.blit(poseStack, (screenWidth - 15) / 2, (screenHeight - 15) / 2, 0, 0, 15, 15);
////                            GuiComponent.blit(poseStack, (screenWidth - 15) / 2, (screenHeight - 15) / 2, 0, 0, 0, 15, 15, 256, 256);
//                            if (options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
//                                int j = screenHeight / 2 - 7 + 16;
//                                int k = screenWidth / 2 - 8;
//                                if (minecraft.crosshairPickEntity instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
//                                    GuiComponent.blit(poseStack, k, j, 68, 94, 16, 16);
//                                }
//                            }
//
//                            RenderSystem.defaultBlendFunc();
//                            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
//                        }
//                    }
//                });
//            }
//        }

    }

//    @Mod.EventBusSubscriber(modid = ThingamabobsAndDoohickeys.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
//    public static class ModEventBusEvents {
//    }

}
