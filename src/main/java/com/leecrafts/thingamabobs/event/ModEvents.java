package com.leecrafts.thingamabobs.event;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import com.leecrafts.thingamabobs.capability.ModCapabilities;
import com.leecrafts.thingamabobs.capability.entity.*;
import com.leecrafts.thingamabobs.capability.player.IPlayerMalletCap;
import com.leecrafts.thingamabobs.capability.player.PlayerMalletCap;
import com.leecrafts.thingamabobs.capability.player.PlayerMalletCapProvider;
import com.leecrafts.thingamabobs.enchantment.ModEnchantments;
import com.leecrafts.thingamabobs.enchantment.custom.MalletDamageEnchantment;
import com.leecrafts.thingamabobs.entity.custom.BoxingGloveEntity;
import com.leecrafts.thingamabobs.item.ModItems;
import com.leecrafts.thingamabobs.item.custom.ComicallyLargeMalletItem;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.nio.ByteBuffer;

import static com.leecrafts.thingamabobs.item.custom.ComicallyLargeMalletItem.*;
import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class ModEvents {

    @Mod.EventBusSubscriber(modid = ThingamabobsAndDoohickeys.MODID)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.register(IEntityExplosivePastryCap.class);
            event.register(IEntityStickyBoxingGloveCap.class);
        }

        @SubscribeEvent
        public static void attachCapabilitiesEventEntity(AttachCapabilitiesEvent<Entity> event) {
            Entity entity = event.getObject();
            if (entity != null && !entity.getCommandSenderWorld().isClientSide) {
                if (!entity.getCapability(ModCapabilities.ENTITY_EXPLOSIVE_PASTRY_CAPABILITY).isPresent()) {
                    EntityExplosivePastryCapProvider entityExplosivePastryCapProvider = new EntityExplosivePastryCapProvider();
                    event.addCapability(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "entity_explosive_pastry"), entityExplosivePastryCapProvider);
                    if (!(entity instanceof Player)) {
                        event.addListener(entityExplosivePastryCapProvider::invalidate);
                    }
                }
                if (entity instanceof LivingEntity livingEntity &&
                        !livingEntity.getCapability(ModCapabilities.ENTITY_STICKY_BOXING_GLOVE_CAPABILITY).isPresent()) {
                    EntityStickyBoxingGloveCapProvider entityStickyBoxingGloveCapProvider = new EntityStickyBoxingGloveCapProvider();
                    event.addCapability(new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "entity_sticky_boxing_glove"), entityStickyBoxingGloveCapProvider);
                    if (!(livingEntity instanceof Player)) {
                        event.addListener(entityStickyBoxingGloveCapProvider::invalidate);
                    }
                }
            }
        }

        // TODO delete at very end
        @SubscribeEvent
        public static void damageTest(LivingDamageEvent event) {
            if (!event.getEntity().level.isClientSide) {
                System.out.println(event.getAmount() + " damage done to " + event.getEntity().getType().toShortString() + " via " + event.getSource().getMsgId());
            }
        }

        // Mobs (not players) attacking with the mallet are too strong, especially when the damage is scaled with the game's difficulty.
        // I cannot easily adjust the hard-coded attack speed of a mob, so I just decrease the amount of damage.
        // Since mobs attack once per second, I made the damage the same amount as the DPS of the mallet.
        // Damage is not scaled by difficulty.
        @SubscribeEvent
        public static void mobAttackWithMallet(LivingHurtEvent event) {
            if (event.getSource().getEntity() instanceof LivingEntity attacker &&
                    !attacker.level.isClientSide &&
                    !(attacker instanceof Player)) {
                ItemStack mainHand = attacker.getMainHandItem();
                if (mainHand.getItem() == ModItems.COMICALLY_LARGE_MALLET_ITEM.get()) {
                    double numerator = BASE_DAMAGE +
                            MalletDamageEnchantment.DAMAGE_MULTIPLIER * mainHand.getEnchantmentLevel(ModEnchantments.WHAM.get());
                    double denominator = getChargeTime(mainHand) / (1.0 * TICKS_PER_SECOND);
                    double attackDamage = attacker.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) - 1 +
                            (attacker.hasEffect(MobEffects.DAMAGE_BOOST) ? 3 * (attacker.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() + 1) : 0);
                    float DPS = (float) ((attackDamage + numerator) / denominator);
                    event.setAmount(DPS);
                }
            }
        }

        @SubscribeEvent
        public static void malletAttributeModifierEvent(ItemAttributeModifierEvent event) {
            ItemStack itemStack = event.getItemStack();
            if (itemStack.getItem() == ModItems.COMICALLY_LARGE_MALLET_ITEM.get() && event.getSlotType() == EquipmentSlot.MAINHAND) {
                double attackSpeed = 1.0 * TICKS_PER_SECOND / getChargeTime(itemStack);
                event.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(INCREASED_ATTACK_SPEED_UUID, "Tool modifier", attackSpeed - BASE_SPEED, AttributeModifier.Operation.ADDITION));
//                System.out.println(event.getModifiers().get(Attributes.ATTACK_SPEED));
            }
        }

        // The comically large mallet is a two-handed weapon.
        // When the (non-creative) player is holding a comically large mallet on their main hand, they are not allowed
        // to equip any item in the offhand.
        @SubscribeEvent
        public static void livingEquipmentChangeEvent(LivingEquipmentChangeEvent event) {
            if (event.getEntity() instanceof Player player &&
                    !player.isCreative() &&
                    !player.level.isClientSide &&
                    player.getMainHandItem().getItem() == ModItems.COMICALLY_LARGE_MALLET_ITEM.get()) {
                ItemStack offhandItem = player.getOffhandItem();
                if (!offhandItem.isEmpty()) {
                    ItemEntity itemEntity = player.drop(offhandItem.copy(), true);
                    offhandItem.shrink(offhandItem.getCount());
                    if (itemEntity != null) {
                        itemEntity.setPickUpDelay(0);
                    }
                }
            }
        }

        // Mallets damage entities through shields, disables shields, and damages shields.
        @SubscribeEvent
        public static void shieldBlockEvent(ShieldBlockEvent event) {
            if (!event.getEntity().level.isClientSide &&
                    event.getDamageSource().getEntity() instanceof LivingEntity attacker &&
                    attacker.getMainHandItem().getItem() == ModItems.COMICALLY_LARGE_MALLET_ITEM.get()) {
                event.setBlockedDamage(0f);
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

        // Entities cannot dismount sticky boxing glove projectiles
        @SubscribeEvent
        public static void entityMountEvent(EntityMountEvent event) {
            if (!event.getLevel().isClientSide &&
                    event.getEntityBeingMounted() instanceof BoxingGloveEntity boxingGloveEntity &&
                    !boxingGloveEntity.isRemoved() &&
                    event.isDismounting()) {
                event.setCanceled(true);
            }
        }

        // Item drops are teleported to the user if it kills an entity with a sticky spring-loaded boxing glove.
        @SubscribeEvent
        public static void livingDropsEvent(LivingDropsEvent event) {
            if (!event.getEntity().level.isClientSide &&
                    event.getSource().getDirectEntity() instanceof BoxingGloveEntity boxingGloveEntity &&
                    boxingGloveEntity.isSticky()) {
                Entity shooter = boxingGloveEntity.getOwner();
                if (shooter != null) {
                    for (ItemEntity itemEntity : event.getDrops()) {
                        itemEntity.teleportTo(shooter.getX(), shooter.getY(), shooter.getZ());
                    }
                }
            }
        }

        @SubscribeEvent
        public static void livingDeathEvent(LivingDeathEvent event) {
            LivingEntity livingEntity = event.getEntity();
            DamageSource damageSource = event.getSource();
            if (!livingEntity.level.isClientSide && damageSource != null) {
                livingEntity.getCapability(ModCapabilities.ENTITY_STICKY_BOXING_GLOVE_CAPABILITY).ifPresent(iEntityStickyBoxingGloveCap -> {
                    EntityStickyBoxingGloveCap entityStickyBoxingGloveCap = (EntityStickyBoxingGloveCap) iEntityStickyBoxingGloveCap;
                    entityStickyBoxingGloveCap.diedFromStickyBoxingGlove =
                            damageSource.getDirectEntity() instanceof BoxingGloveEntity boxingGloveEntity &&
                                    boxingGloveEntity.isSticky();
                });
            }
        }

        // XP drops are teleported to the user if it kills an entity with a sticky spring-loaded boxing glove.
        @SubscribeEvent
        public static void livingExperienceDropEvent(LivingExperienceDropEvent event) {
            LivingEntity livingEntity = event.getEntity();
            Player attackingPlayer = event.getAttackingPlayer();
            if (!livingEntity.level.isClientSide && attackingPlayer != null) {
                livingEntity.getCapability(ModCapabilities.ENTITY_STICKY_BOXING_GLOVE_CAPABILITY).ifPresent(iEntityStickyBoxingGloveCap -> {
                    EntityStickyBoxingGloveCap entityStickyBoxingGloveCap = (EntityStickyBoxingGloveCap) iEntityStickyBoxingGloveCap;
                    if (entityStickyBoxingGloveCap.diedFromStickyBoxingGlove) {
                        event.setCanceled(true);
                        livingEntity.level.addFreshEntity(new ExperienceOrb(
                                livingEntity.level,
                                attackingPlayer.getX(),
                                attackingPlayer.getY(),
                                attackingPlayer.getZ(),
                                event.getDroppedExperience()));
                    }
                });
            }

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
                Minecraft minecraft = Minecraft.getInstance();
                LocalPlayer localPlayer = minecraft.player;
                if (localPlayer != null) {
                    localPlayer.getCapability(ModCapabilities.PLAYER_MALLET_CAPABILITY).ifPresent(iPlayerCap -> {
                        PlayerMalletCap playerMalletCap = (PlayerMalletCap) iPlayerCap;
                        boolean leftMouse = minecraft.mouseHandler.isLeftPressed();
                        boolean rightMouse = minecraft.mouseHandler.isRightPressed();
                        ItemStack mainHand = localPlayer.getMainHandItem();
                        boolean holdingMallet = mainHand.getItem() == ModItems.COMICALLY_LARGE_MALLET_ITEM.get();
                        boolean mirrored = minecraft.options.mainHand().get() == HumanoidArm.LEFT;
                        int chargeTime = ComicallyLargeMalletItem.getChargeTime(mainHand);

                        if (!holdingMallet || !leftMouse) {
                            if (!leftMouse) {
                                // The mallet, when at least 50% charged, will deal damage (0 damage otherwise).
                                // If the mallet is 100% charged, it deals damage to all entities in an area between the player and its reach distance.
                                if (playerMalletCap.malletCharge > chargeTime / 2) {
                                    // called so that the player will face where it attacks
                                    localPlayer.swing(InteractionHand.MAIN_HAND);

                                    if (playerMalletCap.malletCharge >= chargeTime) {
                                        double radius = localPlayer.getEntityReach() / 2;
                                        StringBuilder listString = getHitEntities(localPlayer, radius);
                                        PacketHandler.INSTANCE.sendToServer(
                                                new ServerboundComicallyLargeMalletAttackPacket(playerMalletCap.malletCharge, listString.toString(), rightMouse));
                                    }
                                    else {
                                        HitResult hitResult = minecraft.hitResult;
                                        if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                                            int entityId = ((EntityHitResult) hitResult).getEntity().getId();
                                            PacketHandler.INSTANCE.sendToServer(
                                                    new ServerboundComicallyLargeMalletAttackPacket(playerMalletCap.malletCharge, String.valueOf(entityId), rightMouse));
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
                                localPlayer.attackStrengthTicker = chargeTime;

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
                            if (playerMalletCap.malletCharge < chargeTime && !playerMalletCap.thirdPersonMalletWasCharging) {
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
                                                            1.0f * BASE_CHARGE_TIME / (chargeTime - playerMalletCap.malletCharge),
                                                            mirrored, true, animBytes
                                                    )
                                            );
                                            playerMalletCap.thirdPersonMalletWasCharging = true;
                                        }
                                    }
                                }
                            }
                            else if (playerMalletCap.malletCharge == chargeTime && !playerMalletCap.thirdPersonMalletAnimWasPaused) {
                                var animation = getAnimation(localPlayer);
                                if (animation != null) {
                                    PacketHandler.INSTANCE.sendToServer(
                                            new ServerboundComicallyLargeMalletAnimationPacket(0.0f, mirrored, false, EMPTY_BYTES));
                                }
                                playerMalletCap.thirdPersonMalletAnimWasPaused = true;
                            }

                            playerMalletCap.malletCharge = Math.min(chargeTime, playerMalletCap.malletCharge + 1);
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
            StringBuilder listString = new StringBuilder();
            for (Entity entity : localPlayer.level.getEntities(localPlayer, aabb)) {
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
