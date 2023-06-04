package com.leecrafts.thingamabobs.event;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import com.leecrafts.thingamabobs.capability.ModCapabilities;
import com.leecrafts.thingamabobs.capability.entity.EntityStickyBoxingGloveCapProvider;
import com.leecrafts.thingamabobs.capability.entity.IEntityStickyBoxingGloveCap;
import com.leecrafts.thingamabobs.capability.player.IPlayerMalletCap;
import com.leecrafts.thingamabobs.capability.player.PlayerMalletCap;
import com.leecrafts.thingamabobs.capability.player.PlayerMalletCapProvider;
import com.leecrafts.thingamabobs.entity.custom.BoxingGloveEntity;
import com.leecrafts.thingamabobs.item.ModItems;
import com.leecrafts.thingamabobs.packet.PacketHandler;
import com.leecrafts.thingamabobs.packet.ServerboundComicallyLargeMalletAnimationPacket;
import com.leecrafts.thingamabobs.packet.ServerboundComicallyLargeMalletAttackPacket;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.data.AnimationBinary;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.nio.ByteBuffer;
import java.util.List;

import static com.leecrafts.thingamabobs.item.custom.ComicallyLargeMalletItem.*;

public class ModEvents {

    @Mod.EventBusSubscriber(modid = ThingamabobsAndDoohickeys.MODID)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.register(IEntityStickyBoxingGloveCap.class);
        }

        @SubscribeEvent
        public static void attachCapabilitiesEventEntity(AttachCapabilitiesEvent<Entity> event) {
            Entity entity = event.getObject();
            if (entity != null && !entity.getCommandSenderWorld().isClientSide) {
                if (!entity.getCapability(ModCapabilities.ENTITY_STICKY_BOXING_GLOVE_CAPABILITY).isPresent()) {
                    event.addCapability(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "entity_sticky_boxing_glove"), new EntityStickyBoxingGloveCapProvider());
                }
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
                offhandItem.shrink(offhandItem1.getCount());
                player.addItem(offhandItem1);
            }
        }

        // Boxing glove projectiles hit by an explosion are deflected towards the shooter
        @SubscribeEvent
        public static void explosionEvent(ExplosionEvent.Detonate event) {
            if (!event.getLevel().isClientSide) {
                for (Entity entity : event.getAffectedEntities()) {
                    if (entity instanceof BoxingGloveEntity boxingGloveEntity) {
                        boxingGloveEntity.setDeflected(true);
                    }
                }
            }
        }

        // TODO where is a tick event for any entity?
        @SubscribeEvent
        public static void livingTickEvent(LivingEvent.LivingTickEvent event) {
        }

    }

    @Mod.EventBusSubscriber(modid = ThingamabobsAndDoohickeys.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents {

        public static final ResourceLocation ANIMATION = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "animation");
        public static final ResourceLocation MALLET_IDLE = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "animation.mallet.idle");
        public static final ResourceLocation MALLET_CHARGE = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "animation.mallet.charge");
        public static final ResourceLocation MALLET_SWING = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "animation.mallet.swing");
        public static final ByteBuf EMPTY_BYTES = Unpooled.wrappedBuffer(ByteBuffer.allocate(0));
        public static final MirrorModifier MIRROR = new MirrorModifier();

        @SubscribeEvent
        public static void registerClientCapabilities(RegisterCapabilitiesEvent event) {
            event.register(IPlayerMalletCap.class);
        }

        @SubscribeEvent
        public static void attachClientCapabilitiesEventPlayer(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof LocalPlayer localPlayer) {
                if (!localPlayer.getCapability(ModCapabilities.PLAYER_MALLET_CAPABILITY).isPresent()) {
                    event.addCapability(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "player_mallet"), new PlayerMalletCapProvider());
                }
            }
        }

        // This event listener handles the attack and (1st and 3rd person) animation logic of the comically large mallet weapon.
        // The player must hold left click to charge the mallet, and then release to swing it down.
        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.START) {
                LocalPlayer localPlayer = Minecraft.getInstance().player;
                if (localPlayer != null) {
                    localPlayer.getCapability(ModCapabilities.PLAYER_MALLET_CAPABILITY).ifPresent(iPlayerCap -> {
                        PlayerMalletCap playerMalletCap = (PlayerMalletCap) iPlayerCap;
                        boolean leftMouse = Minecraft.getInstance().mouseHandler.isLeftPressed();
                        boolean holdingMallet = localPlayer.getMainHandItem().getItem() == ModItems.COMICALLY_LARGE_MALLET_ITEM.get();
                        boolean mirrored = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.LEFT;

                        if (!holdingMallet || !leftMouse) {
                            if (!leftMouse) {
                                // The mallet, when at least 50% charged, will deal damage (0 damage otherwise).
                                // If the mallet is 100% charged, it deals damage to all entities in an area between the player and its reach distance.
                                if (playerMalletCap.malletCharge > CHARGE_TIME / 2) {
                                    // called so that the player will face where it attacks
                                    localPlayer.swing(InteractionHand.MAIN_HAND);

                                    if (playerMalletCap.malletCharge >= CHARGE_TIME) {
                                        double radius = localPlayer.getEntityReach() / 2;
                                        StringBuilder listString = getHitEntities(localPlayer, radius);
                                        PacketHandler.INSTANCE.sendToServer(
                                                new ServerboundComicallyLargeMalletAttackPacket(playerMalletCap.malletCharge, listString.toString()));
                                    }
                                    else {
                                        HitResult hitResult = Minecraft.getInstance().hitResult;
                                        if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                                            int entityId = ((EntityHitResult) hitResult).getEntity().getId();
                                            PacketHandler.INSTANCE.sendToServer(
                                                    new ServerboundComicallyLargeMalletAttackPacket(playerMalletCap.malletCharge, String.valueOf(entityId)));
                                        }
                                    }

                                    // first person swing animation
                                    playerMalletCap.firstPersonMalletSwingAnim = 0;

                                    // third person swing animation
                                    var animation = getAnimation(localPlayer);
                                    KeyframeAnimation keyframeAnimation = PlayerAnimationRegistry.getAnimation(MALLET_SWING);
                                    if (animation != null && keyframeAnimation != null) {
                                        KeyframeAnimation modifiedAnimation = setEnabledHeadMovement(keyframeAnimation, false, true);
                                        ByteBuf animBytes = convertToBytes(modifiedAnimation);
                                        PacketHandler.INSTANCE.sendToServer(
                                                new ServerboundComicallyLargeMalletAnimationPacket(1.0f, mirrored, false, animBytes));
                                        playerMalletCap.thirdPersonMalletSwingAnim = 0;
                                    }
                                }
                            }

                            // if player switches weapon, the mallet animation no longer applies
                            if (!holdingMallet) {
                                playerMalletCap.resetAnim();

                                if (!playerMalletCap.thirdPersonMalletAnimWasStopped) {
                                    var animation = getAnimation(localPlayer);
                                    if (animation != null) {
                                        PacketHandler.INSTANCE.sendToServer(
                                                new ServerboundComicallyLargeMalletAnimationPacket(-1.0f, mirrored, false, EMPTY_BYTES));
                                    }
                                    playerMalletCap.thirdPersonMalletAnimWasStopped = true;
                                }
                            }
                            playerMalletCap.malletCharge = 0;
                            playerMalletCap.firstPersonMalletChargeOffset = 0;
                            playerMalletCap.thirdPersonMalletWasCharging = false;
                            playerMalletCap.thirdPersonMalletAnimWasPaused = false;

                            if (holdingMallet) {
                                // When the charge is 0, the attack indicator will not appear
                                localPlayer.attackStrengthTicker = CHARGE_TIME;

                                // third person idle animation
                                var animation = getAnimation(localPlayer);
                                KeyframeAnimation keyframeAnimation = PlayerAnimationRegistry.getAnimation(MALLET_IDLE);
                                if (animation != null && keyframeAnimation != null) {

                                    if ((animation.getAnimation() == null || !playerMalletCap.thirdPersonMalletAnimWasIdle) &&
                                            playerMalletCap.thirdPersonMalletSwingAnim == -1) {

                                        KeyframeAnimation modifiedAnimation = setEnabledLegMovement(keyframeAnimation, false);
                                        KeyframeAnimation modifiedAnimation1 = setEnabledHeadMovement(modifiedAnimation, false, false);

                                        ByteBuf animBytes = convertToBytes(modifiedAnimation1);
                                        PacketHandler.INSTANCE.sendToServer(
                                                new ServerboundComicallyLargeMalletAnimationPacket(1.0f, mirrored, true, animBytes));
                                        playerMalletCap.thirdPersonMalletAnimWasIdle = true;
                                    }
                                }
                            }
                        }
                        else {
                            // third person charge animation
                            if (playerMalletCap.malletCharge < CHARGE_TIME && !playerMalletCap.thirdPersonMalletWasCharging) {
                                playerMalletCap.thirdPersonMalletAnimWasIdle = false;
                                var animation = getAnimation(localPlayer);
                                if (animation != null) {
                                    if (playerMalletCap.thirdPersonMalletSwingAnim == -1) {
                                        KeyframeAnimation keyframeAnimation = PlayerAnimationRegistry.getAnimation(MALLET_CHARGE);
                                        if (keyframeAnimation != null) {
                                            KeyframeAnimation modifiedAnimation = setEnabledLegMovement(keyframeAnimation, false);
                                            KeyframeAnimation modifiedAnimation1 = setEnabledHeadMovement(modifiedAnimation, false, false);
                                            ByteBuf animBytes = convertToBytes(modifiedAnimation1);
                                            PacketHandler.INSTANCE.sendToServer(
                                                    new ServerboundComicallyLargeMalletAnimationPacket(
                                                            1.0f * CHARGE_TIME / (CHARGE_TIME - playerMalletCap.malletCharge),
                                                            mirrored, true, animBytes
                                                    )
                                            );
                                            playerMalletCap.thirdPersonMalletWasCharging = true;
                                        }
                                    }
                                }
                            }
                            else if (playerMalletCap.malletCharge == CHARGE_TIME && !playerMalletCap.thirdPersonMalletAnimWasPaused) {
                                var animation = getAnimation(localPlayer);
                                if (animation != null) {
                                    PacketHandler.INSTANCE.sendToServer(
                                            new ServerboundComicallyLargeMalletAnimationPacket(0.0f, mirrored, false, EMPTY_BYTES));
                                }
                                playerMalletCap.thirdPersonMalletAnimWasPaused = true;
                            }

                            playerMalletCap.malletCharge = Math.min(CHARGE_TIME, playerMalletCap.malletCharge + 1);
                            localPlayer.attackStrengthTicker = playerMalletCap.malletCharge;
                        }

                        if (holdingMallet) {
                            playerMalletCap.thirdPersonMalletAnimWasStopped = false;
                            if (playerMalletCap.thirdPersonMalletSwingAnim > -1) {
                                playerMalletCap.thirdPersonMalletSwingAnim++;
                                if (playerMalletCap.thirdPersonMalletSwingAnim >= 13) {
                                    playerMalletCap.thirdPersonMalletSwingAnim = -1;
                                }
                            }

                            // update first person animation ticks
                            if (playerMalletCap.firstPersonMalletEquipAnim < EQUIP_TIME) {
                                playerMalletCap.firstPersonMalletEquipAnim++;
                            }
                            else if (playerMalletCap.firstPersonMalletSwingAnim > -1) {
                                if (playerMalletCap.firstPersonMalletSwingAnim < SWING_TIME + 10) {
                                    playerMalletCap.firstPersonMalletSwingAnim++;
                                }
                                else if (playerMalletCap.firstPersonMalletPickupAnim < PICKUP_TIME) {
                                    playerMalletCap.firstPersonMalletPickupAnim++;
                                }
                                else {
                                    playerMalletCap.firstPersonMalletSwingAnim = -1;
                                    playerMalletCap.firstPersonMalletPickupAnim = 0;
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

        private static ByteBuf convertToBytes(KeyframeAnimation keyframeAnimation) {
            int numBytes = AnimationBinary.calculateSize(keyframeAnimation, AnimationBinary.getCurrentVersion());
            ByteBuffer animBytes = ByteBuffer.allocate(numBytes);
            animBytes = ByteBuffer.wrap(AnimationBinary.write(keyframeAnimation, animBytes).array());
            return Unpooled.wrappedBuffer(animBytes);
        }

        public static ModifierLayer<IAnimation> getAnimation(AbstractClientPlayer abstractClientPlayer) {
            return (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData(abstractClientPlayer).get(ANIMATION);
        }

        public static void setSpeed(ModifierLayer<IAnimation> animation, float speed) {
            if (animation.size() > 0) {
                animation.removeModifier(0);
            }
            animation.addModifierBefore(new SpeedModifier(speed));
        }

        public static KeyframeAnimation setEnabledLegMovement(KeyframeAnimation keyframeAnimation, boolean enabled) {
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

        public static KeyframeAnimation setEnabledHeadMovement(KeyframeAnimation keyframeAnimation, boolean enabled, boolean enableHeadPitch) {
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
