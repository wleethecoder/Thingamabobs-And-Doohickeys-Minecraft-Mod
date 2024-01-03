package com.leecrafts.thingamabobs.packet;

import com.leecrafts.thingamabobs.criterion.ModCriteria;
import com.leecrafts.thingamabobs.damage.ModDamageSources;
import com.leecrafts.thingamabobs.sound.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundComicallyLargeMalletAttackPacket {

    public final int strength;
    public final String targetsString;
    public final boolean indiscriminate;

    public ServerboundComicallyLargeMalletAttackPacket(int strength, String targetsString, boolean indiscriminate) {
        this.strength = strength;
        this.targetsString = targetsString;
        this.indiscriminate = indiscriminate;
    }

    public ServerboundComicallyLargeMalletAttackPacket(FriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readUtf(), buffer.readBoolean());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.strength);
        buffer.writeUtf(this.targetsString);
        buffer.writeBoolean(this.indiscriminate);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
//                DamageSource sampleDamageSource = sender.damageSources().playerAttack(sender);
                DamageSource damageSource = new ModDamageSources(sender.level.registryAccess()).wham(sender);
                String[] list = this.targetsString.split(",");
                boolean anythingHit = false;
                int numberHitLiving = 0;
                int numberHitMob = 0;
                for (String str : list) {
                    Entity entity = ((ServerLevel) sender.level).getEntityOrPart(Integer.parseInt(str));
                    if (entity != null) {
                        sender.attackStrengthTicker = this.strength;
                        if (!sender.isPassengerOfSameVehicle(entity) &&
                                !entity.hasPassenger(sender) &&
                                (this.indiscriminate || isTouchable(entity))) {
//                            sender.attack(entity);
                            this.attack(sender, damageSource, entity);
                            if (entity.isAttackable() && !entity.skipAttackInteraction(sender)) {
                                anythingHit = true;
                                if (entity instanceof LivingEntity livingEntity && !livingEntity.isInvulnerableTo(damageSource)) {
                                    numberHitLiving++;
                                    if (livingEntity instanceof Mob) {
                                        numberHitMob++;
                                    }
                                }
                            }
                        }
                    }
                }

                // Calling hurtAndBreak manually only once for optimization purposes
                sender.getMainHandItem().hurtAndBreak(numberHitLiving, sender, (livingEntity -> livingEntity.broadcastBreakEvent(EquipmentSlot.MAINHAND)));

                if (anythingHit) {
                    sender.level.playSound(null, sender, ModSounds.COMICALLY_LARGE_MALLET_WHAM.get(), SoundSource.PLAYERS, 1.0f, (sender.getRandom().nextFloat() - sender.getRandom().nextFloat()) * 0.2f + 1.0f);
                }
                ModCriteria.HIT_BY_AOE_WEAPON.trigger(sender, numberHitMob);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static boolean isTouchable(Entity entity) {
        return !(entity instanceof Player) &&
                !(entity instanceof AbstractVillager) &&
                !(entity instanceof IronGolem) &&
                (!(entity instanceof TamableAnimal tamableAnimal) || !tamableAnimal.isTame());
    }

    private void attack(Player attacker, DamageSource damageSource, Entity pTarget) {
        if (!net.minecraftforge.common.ForgeHooks.onPlayerAttackTarget(attacker, pTarget)) return;
        if (pTarget.isAttackable()) {
            if (!pTarget.skipAttackInteraction(attacker)) {
                float f = (float)attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
                float f1;
                if (pTarget instanceof LivingEntity) {
                    f1 = EnchantmentHelper.getDamageBonus(attacker.getMainHandItem(), ((LivingEntity)pTarget).getMobType());
                } else {
                    f1 = EnchantmentHelper.getDamageBonus(attacker.getMainHandItem(), MobType.UNDEFINED);
                }

                float f2 = attacker.getAttackStrengthScale(0.5F);
                f *= 0.2F + f2 * f2 * 0.8F;
                f1 *= f2;
                if (f > 0.0F || f1 > 0.0F) {
                    boolean flag = f2 > 0.9F;
                    float i = (float)attacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK); // Forge: Initialize attacker value to the attack knockback attribute of the player, which is by default 0
//                    i += EnchantmentHelper.getKnockbackBonus(attacker);
                    if (attacker.isSprinting() && flag) {
                        attacker.level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, attacker.getSoundSource(), 1.0F, 1.0F);
                        ++i;
                    }

                    boolean flag2 = flag && attacker.fallDistance > 0.0F && !attacker.onGround && !attacker.onClimbable() && !attacker.isInWater() && !attacker.hasEffect(MobEffects.BLINDNESS) && !attacker.isPassenger() && pTarget instanceof LivingEntity;
                    flag2 = flag2 && !attacker.isSprinting();
                    net.minecraftforge.event.entity.player.CriticalHitEvent hitResult = net.minecraftforge.common.ForgeHooks.getCriticalHit(attacker, pTarget, flag2, flag2 ? 1.5F : 1.0F);
                    flag2 = hitResult != null;
                    if (flag2) {
                        f *= hitResult.getDamageModifier();
                    }

                    f += f1;

                    float f4 = 0.0F;
                    boolean flag4 = false;
                    int j = EnchantmentHelper.getFireAspect(attacker);
                    if (pTarget instanceof LivingEntity) {
                        f4 = ((LivingEntity)pTarget).getHealth();
                        if (j > 0 && !pTarget.isOnFire()) {
                            flag4 = true;
                            pTarget.setSecondsOnFire(1);
                        }
                    }

                    Vec3 vec3 = pTarget.getDeltaMovement();
//                    boolean flag5 = pTarget.hurt(attacker.damageSources().playerAttack(attacker), f);
                    boolean flag5 = pTarget.hurt(damageSource, f);
                    if (flag5) {
                        if (i > 0) {
                            if (pTarget instanceof LivingEntity) {
                                ((LivingEntity)pTarget).knockback(i * 0.5F, Mth.sin(attacker.getYRot() * ((float)Math.PI / 180F)), -Mth.cos(attacker.getYRot() * ((float)Math.PI / 180F)));
                            } else {
                                pTarget.push(-Mth.sin(attacker.getYRot() * ((float)Math.PI / 180F)) * i * 0.5F, 0.1D, Mth.cos(attacker.getYRot() * ((float)Math.PI / 180F)) * i * 0.5F);
                            }

                            attacker.setDeltaMovement(attacker.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                            attacker.setSprinting(false);
                        }

                        if (pTarget instanceof ServerPlayer && pTarget.hurtMarked) {
                            ((ServerPlayer)pTarget).connection.send(new ClientboundSetEntityMotionPacket(pTarget));
                            pTarget.hurtMarked = false;
                            pTarget.setDeltaMovement(vec3);
                        }

                        if (flag2) {
                            attacker.level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, attacker.getSoundSource(), 1.0F, 1.0F);
                            attacker.crit(pTarget);
                        }

                        if (!flag2) {
                            if (flag) {
                                attacker.level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, attacker.getSoundSource(), 1.0F, 1.0F);
                            } else {
                                attacker.level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, attacker.getSoundSource(), 1.0F, 1.0F);
                            }
                        }

                        if (f1 > 0.0F) {
                            attacker.magicCrit(pTarget);
                        }

                        attacker.setLastHurtMob(pTarget);
                        if (pTarget instanceof LivingEntity) {
                            EnchantmentHelper.doPostHurtEffects((LivingEntity)pTarget, attacker);
                        }

                        EnchantmentHelper.doPostDamageEffects(attacker, pTarget);
                        ItemStack itemStack1 = attacker.getMainHandItem();
                        Entity entity = pTarget;
                        if (pTarget instanceof net.minecraftforge.entity.PartEntity) {
                            entity = ((net.minecraftforge.entity.PartEntity<?>) pTarget).getParent();
                        }

                        if (!attacker.level.isClientSide && !itemStack1.isEmpty() && entity instanceof LivingEntity) {
                            ItemStack copy = itemStack1.copy();
                            itemStack1.hurtEnemy((LivingEntity)entity, attacker);
                            if (itemStack1.isEmpty()) {
                                net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(attacker, copy, InteractionHand.MAIN_HAND);
                                attacker.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                            }
                        }

                        if (pTarget instanceof LivingEntity) {
                            float f5 = f4 - ((LivingEntity)pTarget).getHealth();
                            attacker.awardStat(Stats.DAMAGE_DEALT, Math.round(f5 * 10.0F));
                            if (j > 0) {
                                pTarget.setSecondsOnFire(j * 4);
                            }

                            if (attacker.level instanceof ServerLevel && f5 > 2.0F) {
                                int k = (int)((double)f5 * 0.5D);
                                ((ServerLevel)attacker.level).sendParticles(ParticleTypes.DAMAGE_INDICATOR, pTarget.getX(), pTarget.getY(0.5D), pTarget.getZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
                            }
                        }

                        attacker.causeFoodExhaustion(0.1F);
                    } else {
                        attacker.level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, attacker.getSoundSource(), 1.0F, 1.0F);
                        if (flag4) {
                            pTarget.clearFire();
                        }
                    }
                }
                attacker.resetAttackStrengthTicker(); // FORGE: Moved from beginning of attack() so that getAttackStrengthScale() returns an accurate value during all attack events

            }
        }
    }

}
