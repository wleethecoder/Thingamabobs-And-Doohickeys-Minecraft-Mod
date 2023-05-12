package com.leecrafts.thingamabobs.event;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import com.leecrafts.thingamabobs.capability.ModCapabilities;
import com.leecrafts.thingamabobs.capability.player.IPlayerCap;
import com.leecrafts.thingamabobs.capability.player.PlayerCap;
import com.leecrafts.thingamabobs.capability.player.PlayerCapProvider;
import com.leecrafts.thingamabobs.item.ModItems;
import com.leecrafts.thingamabobs.packet.PacketHandler;
import com.leecrafts.thingamabobs.packet.ServerboundComicallyLargeMalletAttackPacket;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
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

import static com.leecrafts.thingamabobs.item.custom.ComicallyLargeMalletItem.*;

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
                event.addCapability(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "first_person_mallet_charge_offset"), playerCapProvider);
                event.addCapability(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "first_person_mallet_equip_animation"), playerCapProvider);
                event.addCapability(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "first_person_mallet_swing_animation"), playerCapProvider);
                event.addCapability(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "first_person_mallet_pickup_animation"), playerCapProvider);
                event.addCapability(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "was_holding_mallet"), playerCapProvider);
                event.addCapability(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "third_person_mallet_animation_was_reset"), playerCapProvider);
                event.addCapability(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "third_person_mallet_swing_animation"), playerCapProvider);
                event.addCapability(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "third_person_mallet_was_swinging"), playerCapProvider);
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

        public static final ResourceLocation ANIMATION = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "animation");
        public static final ResourceLocation MALLET_CHARGE = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "mallet_charge");
        public static final ResourceLocation MALLET_SWING = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "mallet_swing");
        public static final SpeedModifier PAUSE = new SpeedModifier(0.0f);
        public static final AbstractFadeModifier FADE = AbstractFadeModifier.standardFadeIn(5, Ease.OUTSINE);

        // TODO left hand
        // TODO fade transition
        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.START) {
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

                        if (!holdingMallet || !leftMouse) {
                            if (!leftMouse) {
                                if (playerCap.malletCharge >= CHARGE_TIME) {
                                    // TODO test animations on server?
                                    // TODO do we need this line?
//                                    localPlayer.swing(InteractionHand.MAIN_HAND);

                                    double radius = localPlayer.getEntityReach() / 2;
                                    StringBuilder listString = getHitEntities(localPlayer, radius);
                                    PacketHandler.INSTANCE.sendToServer(new ServerboundComicallyLargeMalletAttackPacket(listString.toString()));
                                    playerCap.firstPersonMalletSwingAnim = 0;

                                    var animation = getAnimation(localPlayer);
                                    KeyframeAnimation keyframeAnimation = PlayerAnimationRegistry.getAnimation(MALLET_SWING);
                                    if (animation != null && keyframeAnimation != null) {
                                        KeyframeAnimation modifiedAnimation = setEnabledHeadMovement(keyframeAnimation, false, true);
                                        animation.setAnimation(new KeyframeAnimationPlayer(modifiedAnimation));
                                        continueAnimation(animation, 1.0f);
                                        playerCap.thirdPersonMalletSwingAnim = 0;
                                    }
                                }
                            }

                            if (!holdingMallet) {
                                playerCap.firstPersonMalletEquipAnim = 0;
                                playerCap.wasHoldingMallet = false;
                                playerCap.thirdPersonMalletAnimWasReset = false;
                                playerCap.resetAnim();

                                // if player switches weapon, the mallet animation no longer applies
                                var animation = getAnimation(localPlayer);
                                if (animation != null) {
                                    animation.setAnimation(null);
                                }
                            }
                            playerCap.malletCharge = 0;
                            playerCap.firstPersonMalletChargeOffset = 0;
                            playerCap.thirdPersonMalletWasSwinging = false;

                            // When the charge is 0, the attack indicator will not appear
                            if (holdingMallet) {
                                localPlayer.attackStrengthTicker = CHARGE_TIME;

                                var animation = getAnimation(localPlayer);
                                KeyframeAnimation keyframeAnimation = PlayerAnimationRegistry.getAnimation(MALLET_CHARGE);
                                if (animation != null && keyframeAnimation != null) {

                                    if ((animation.getAnimation() == null || !playerCap.thirdPersonMalletAnimWasReset) &&
                                            playerCap.thirdPersonMalletSwingAnim == -1) {

                                        KeyframeAnimation modifiedAnimation = setEnabledLegMovement(keyframeAnimation, false);
                                        KeyframeAnimation modifiedAnimation1 = setEnabledHeadMovement(modifiedAnimation, false, false);

//                                        animation.replaceAnimationWithFade(FADE, new KeyframeAnimationPlayer(modifiedAnimation));
                                        animation.setAnimation(new KeyframeAnimationPlayer(modifiedAnimation1));
                                        pauseAnimation(animation);
                                        playerCap.thirdPersonMalletAnimWasReset = true;
                                    }
                                }
                            }
                        }
                        else {
                            if (playerCap.malletCharge < CHARGE_TIME && !playerCap.thirdPersonMalletWasSwinging) {
                                playerCap.thirdPersonMalletAnimWasReset = false;
                                var animation = getAnimation(localPlayer);
                                if (animation != null) {
                                    if (playerCap.thirdPersonMalletSwingAnim == -1) {
                                        KeyframeAnimation keyframeAnimation = PlayerAnimationRegistry.getAnimation(MALLET_CHARGE);
                                        if (keyframeAnimation != null) {
                                            KeyframeAnimation modifiedAnimation = setEnabledLegMovement(keyframeAnimation, false);
                                            KeyframeAnimation modifiedAnimation1 = setEnabledHeadMovement(modifiedAnimation, false, false);
                                            animation.setAnimation(new KeyframeAnimationPlayer(modifiedAnimation1));
                                            continueAnimation(animation, 1.0f * CHARGE_TIME / (CHARGE_TIME - playerCap.malletCharge));
                                            playerCap.thirdPersonMalletWasSwinging = true;
                                        }
                                    }
                                }
                            }
                            else if (playerCap.malletCharge == CHARGE_TIME) {
                                var animation = getAnimation(localPlayer);
                                if (animation != null) {
                                    pauseAnimation(animation);
                                }
                            }
                            playerCap.malletCharge = Math.min(CHARGE_TIME, playerCap.malletCharge + 1);
                            localPlayer.attackStrengthTicker = playerCap.malletCharge;
                        }

                        if (holdingMallet) {
                            if (playerCap.thirdPersonMalletSwingAnim > -1) {
                                playerCap.thirdPersonMalletSwingAnim++;
                                if (playerCap.thirdPersonMalletSwingAnim >= 13) {
                                    playerCap.thirdPersonMalletSwingAnim = -1;
                                }
                            }
                            // Update first person animation ticks
                            if (playerCap.firstPersonMalletEquipAnim < EQUIP_TIME) {
                                playerCap.firstPersonMalletEquipAnim++;
                            }
                            else if (playerCap.firstPersonMalletSwingAnim > -1) {
                                if (playerCap.firstPersonMalletSwingAnim < SWING_TIME + 10) {
                                    playerCap.firstPersonMalletSwingAnim++;
                                }
                                else if (playerCap.firstPersonMalletPickupAnim < PICKUP_TIME) {
                                    playerCap.firstPersonMalletPickupAnim++;
                                }
                                else {
                                    playerCap.firstPersonMalletSwingAnim = -1;
                                    playerCap.firstPersonMalletPickupAnim = 0;
                                }
                            }
                        }
                    });
                }
            }
        }

        private static StringBuilder getHitEntities(LocalPlayer localPlayer, double radius) {
            Vec3 vec3 = localPlayer.getViewVector(1).scale(radius).add(localPlayer.getEyePosition());
            AABB aabb = new AABB(vec3.x - radius, vec3.y - radius, vec3.z - radius, vec3.x + radius, vec3.y + radius, vec3.z + radius);
            List<Entity> list = localPlayer.level.getEntities(localPlayer, aabb);
            StringBuilder listString = new StringBuilder();
            for (Entity entity : list) {
                listString.append(entity.getId()).append(",");
            }
            return listString;
        }

        private static ModifierLayer<IAnimation> getAnimation(LocalPlayer localPlayer) {
            return (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData(localPlayer).get(ANIMATION);
        }

        private static void pauseAnimation(ModifierLayer<IAnimation> animation) {
            if (animation.size() > 0) {
                animation.removeModifier(0);
            }
            animation.addModifierBefore(PAUSE);
        }

        private static void continueAnimation(ModifierLayer<IAnimation> animation, float speed) {
            if (animation.size() > 0) {
                animation.removeModifier(0);
            }
            animation.addModifierBefore(new SpeedModifier(speed));
        }

        private static KeyframeAnimation setEnabledLegMovement(KeyframeAnimation keyframeAnimation, boolean enabled) {
            var builder = keyframeAnimation.mutableCopy();
            var leftLeg = builder.getPart("leftLeg");
            if (leftLeg != null) {
                leftLeg.x.setEnabled(enabled);
                leftLeg.y.setEnabled(enabled);
                leftLeg.z.setEnabled(enabled);
                leftLeg.pitch.setEnabled(enabled);
                leftLeg.yaw.setEnabled(enabled);
                leftLeg.roll.setEnabled(enabled);
            }

            var rightLeg = builder.getPart("rightLeg");
            if (rightLeg != null) {
                rightLeg.x.setEnabled(enabled);
                rightLeg.y.setEnabled(enabled);
                rightLeg.z.setEnabled(enabled);
                rightLeg.pitch.setEnabled(enabled);
                rightLeg.yaw.setEnabled(enabled);
                rightLeg.roll.setEnabled(enabled);
            }
            return builder.build();
        }

        private static KeyframeAnimation setEnabledHeadMovement(KeyframeAnimation keyframeAnimation, boolean enabled, boolean enableHeadPitch) {
            var builder = keyframeAnimation.mutableCopy();
            var head = builder.getPart("head");
            if (head != null) {
                head.x.setEnabled(enabled);
                head.y.setEnabled(enabled);
                head.z.setEnabled(enabled);
                head.pitch.setEnabled(enableHeadPitch);
                head.yaw.setEnabled(enabled);
                head.roll.setEnabled(enabled);
            }
            return builder.build();
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

    }

}
